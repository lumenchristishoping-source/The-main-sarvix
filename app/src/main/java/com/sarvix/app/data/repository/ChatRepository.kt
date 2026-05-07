package com.sarvix.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sarvix.app.data.model.Chat
import com.sarvix.app.data.model.ChatPreview
import com.sarvix.app.data.model.Clarification
import com.sarvix.app.data.model.ClarifyLimit
import com.sarvix.app.data.model.IntentTag
import com.sarvix.app.data.model.Message
import com.sarvix.app.data.model.toUserProfile
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val translationRepository: TranslationRepository
) {
    fun getChats(userId: String): Flow<Resource<List<ChatPreview>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting chats")
                    trySend(Resource.Error(error.message ?: "Failed to load chats"))
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    val chats = querySnapshot.toObjects(Chat::class.java)
                    
                    // Map to ChatPreview
                    val chatPreviews = chats.map { chat ->
                        val otherUserId = chat.participants.first { it != userId }
                        val otherUsername = chat.participantUsernames[otherUserId] ?: ""
                        val otherPhoto = chat.participantPhotos[otherUserId] ?: ""
                        val unreadCount = chat.unreadCount[userId] ?: 0
                        
                        ChatPreview(
                            chatId = chat.id,
                            otherUser = com.sarvix.app.data.model.UserProfile(
                                id = otherUserId,
                                username = otherUsername,
                                profilePictureUrl = otherPhoto
                            ),
                            lastMessage = chat.lastMessage,
                            lastMessageTimestamp = chat.lastMessageTimestamp,
                            unreadCount = unreadCount
                        )
                    }
                    
                    trySend(Resource.Success(chatPreviews))
                }
            }
        
        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting messages")
                    trySend(Resource.Error(error.message ?: "Failed to load messages"))
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    val messages = querySnapshot.toObjects(Message::class.java)
                    trySend(Resource.Success(messages))
                }
            }
        
        awaitClose { listener.remove() }
    }

    fun sendMessage(
        chatId: String,
        receiverId: String,
        content: String,
        intentTag: IntentTag? = null
    ): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        try {
            val senderId = authRepository.getCurrentUserId()
            val messageId = UUID.randomUUID().toString()
            
            val message = Message(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                intentTag = intentTag,
                timestamp = Date()
            )
            
            // Save message
            firestore.collection("messages")
                .document(messageId)
                .set(message)
                .await()
            
            // Update chat with last message
            val chatUpdates = hashMapOf(
                "lastMessage" to content,
                "lastMessageTimestamp" to Date(),
                "lastMessageSenderId" to senderId,
                "unreadCount.$receiverId" to com.google.firebase.firestore.FieldValue.increment(1)
            )
            
            firestore.collection("chats")
                .document(chatId)
                .update(chatUpdates)
                .await()
            
            emit(Resource.Success(message))
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            emit(Resource.Error(e.message ?: "Failed to send message"))
        }
    }

    fun createChat(otherUserId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Check if chat already exists
            val existingChat = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()
                .documents
                .find { doc ->
                    val participants = doc.get("participants") as? List<String>
                    participants?.contains(otherUserId) == true
                }
            
            if (existingChat != null) {
                emit(Resource.Success(existingChat.id))
                return@flow
            }
            
            // Get user details
            val currentUser = authRepository.getUserById(currentUserId)
            val otherUser = authRepository.getUserById(otherUserId)
            
            val chatId = UUID.randomUUID().toString()
            val chat = Chat(
                id = chatId,
                participants = listOf(currentUserId, otherUserId),
                participantUsernames = mapOf(
                    currentUserId to (currentUser?.username ?: ""),
                    otherUserId to (otherUser?.username ?: "")
                ),
                participantPhotos = mapOf(
                    currentUserId to (currentUser?.profilePictureUrl ?: ""),
                    otherUserId to (otherUser?.profilePictureUrl ?: "")
                ),
                createdAt = Date()
            )
            
            firestore.collection("chats")
                .document(chatId)
                .set(chat)
                .await()
            
            emit(Resource.Success(chatId))
        } catch (e: Exception) {
            Timber.e(e, "Error creating chat")
            emit(Resource.Error(e.message ?: "Failed to create chat"))
        }
    }

    fun markMessagesAsRead(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Update unread count in chat
            firestore.collection("chats")
                .document(chatId)
                .update("unreadCount.$currentUserId", 0)
                .await()
            
            // Mark individual messages as read
            val unreadMessages = firestore.collection("messages")
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error marking messages as read")
            emit(Resource.Error(e.message ?: "Failed to mark as read"))
        }
    }

    fun requestClarification(messageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Check clarify limit
            val limitDoc = firestore.collection("clarify_limits")
                .document(currentUserId)
                .get()
                .await()
            
            val clarifyLimit = limitDoc.toObject(ClarifyLimit::class.java)
                ?: ClarifyLimit(userId = currentUserId)
            
            // Reset if 24 hours passed
            val effectiveLimit = if (clarifyLimit.shouldReset()) {
                ClarifyLimit(userId = currentUserId, resetTime = Date())
            } else {
                clarifyLimit
            }
            
            if (effectiveLimit.isLimitReached()) {
                emit(Resource.Error("Daily clarification limit reached (5/5). Try again in 24 hours."))
                return@flow
            }
            
            // Get message details for clarification
            val messageDoc = firestore.collection("messages")
                .document(messageId)
                .get()
                .await()
            
            val message = messageDoc.toObject(Message::class.java)
            if (message == null) {
                emit(Resource.Error("Message not found"))
                return@flow
            }
            
            // Get sender's profile for context
            val sender = authRepository.getUserById(message.senderId)
            
            // Create clarification request (in a real app, this might use AI)
            // For MVP, we'll create a simple clarification based on intent tag
            val clarificationResponse = generateClarification(message, sender)
            
            val clarification = Clarification(
                id = UUID.randomUUID().toString(),
                messageId = messageId,
                requestedBy = currentUserId,
                response = clarificationResponse.first,
                tone = clarificationResponse.second,
                intent = clarificationResponse.third,
                createdAt = Date()
            )
            
            // Save clarification
            firestore.collection("clarifications")
                .document(clarification.id)
                .set(clarification)
                .await()
            
            // Update message with clarification
            firestore.collection("messages")
                .document(messageId)
                .update("clarifications", com.google.firebase.firestore.FieldValue.arrayUnion(clarification))
                .await()
            
            // Update clarify limit
            val newCount = effectiveLimit.dailyCount + 1
            firestore.collection("clarify_limits")
                .document(currentUserId)
                .set(
                    mapOf(
                        "userId" to currentUserId,
                        "dailyCount" to newCount,
                        "resetTime" to (effectiveLimit.resetTime ?: Date())
                    )
                )
                .await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error requesting clarification")
            emit(Resource.Error(e.message ?: "Failed to request clarification"))
        }
    }

    fun getClarifyLimit(userId: String): Flow<Resource<ClarifyLimit>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("clarify_limits")
                .document(userId)
                .get()
                .await()
            
            val limit = doc.toObject(ClarifyLimit::class.java)
                ?: ClarifyLimit(userId = userId)
            
            // Reset if needed
            val effectiveLimit = if (limit.shouldReset()) {
                ClarifyLimit(userId = userId, resetTime = Date())
            } else {
                limit
            }
            
            emit(Resource.Success(effectiveLimit))
        } catch (e: Exception) {
            Timber.e(e, "Error getting clarify limit")
            emit(Resource.Error(e.message ?: "Failed to get clarify limit"))
        }
    }

    fun translateMessage(messageId: String, targetLanguage: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val messageDoc = firestore.collection("messages")
                .document(messageId)
                .get()
                .await()
            
            val message = messageDoc.toObject(Message::class.java)
            if (message == null) {
                emit(Resource.Error("Message not found"))
                return@flow
            }
            
            // Translate using ML Kit
            val translatedText = translationRepository.translateText(
                message.content,
                targetLanguage
            )
            
            // Update message with translation
            firestore.collection("messages")
                .document(messageId)
                .update(
                    mapOf(
                        "isTranslated" to true,
                        "translatedContent" to translatedText,
                        "targetLanguage" to targetLanguage,
                        "originalContent" to message.content
                    )
                )
                .await()
            
            emit(Resource.Success(translatedText))
        } catch (e: Exception) {
            Timber.e(e, "Error translating message")
            emit(Resource.Error(e.message ?: "Failed to translate message"))
        }
    }

    private fun generateClarification(
        message: Message,
        sender: com.sarvix.app.data.model.User?
    ): Triple<String, String, String> {
        // Simple clarification logic for MVP
        // In production, this would use AI/ML for more sophisticated analysis
        
        val intent = message.intentTag ?: IntentTag.SERIOUS
        
        val response = when (intent) {
            IntentTag.JOKE -> "This message appears to be intended as humor or a joke. The sender is likely trying to be lighthearted."
            IntentTag.SERIOUS -> "This message is intended to be taken seriously. The sender wants to communicate something important."
            IntentTag.ADVICE -> "The sender is offering advice or suggestions. They want to help or guide you."
            IntentTag.VENT -> "The sender is venting frustrations. They may need someone to listen and empathize."
            IntentTag.RANT -> "The sender is expressing strong emotions or opinions. They may be worked up about something."
        }
        
        val tone = when (intent) {
            IntentTag.JOKE -> "Playful, Lighthearted"
            IntentTag.SERIOUS -> "Serious, Sincere"
            IntentTag.ADVICE -> "Supportive, Helpful"
            IntentTag.VENT -> "Frustrated, Emotional"
            IntentTag.RANT -> "Passionate, Intense"
        }
        
        return Triple(response, tone, intent.displayName)
    }

    fun deleteMessage(messageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("messages")
                .document(messageId)
                .update("isDeleted", true)
                .await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error deleting message")
            emit(Resource.Error(e.message ?: "Failed to delete message"))
        }
    }
}