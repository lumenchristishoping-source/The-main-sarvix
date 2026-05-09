package com.sarvix.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sarvix.app.data.model.*
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun loadChats(userId: String): Flow<Resource<List<ChatPreview>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error loading chats")
                    trySend(Resource.Error(error.message ?: "Failed to load chats"))
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val participants = doc.get("participants") as? List<String> ?: emptyList()
                        val otherUserId = participants.firstOrNull { it != userId } ?: ""
                        ChatPreview(
                            chatId = doc.id,
                            otherUser = User(id = otherUserId, username = "@user"),
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTimestamp = doc.getDate("lastMessageTimestamp"),
                            unreadCount = (doc.get("unreadCount.$userId") as? Long)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing chat doc")
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(chats))
            }
        awaitClose { listener.remove() }
    }

    fun loadMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error loading messages")
                    trySend(Resource.Error(error.message ?: "Failed to load messages"))
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val intentStr = doc.getString("intentTag")
                        val intentTag = intentStr?.let {
                            try { IntentTag.valueOf(it) } catch (e: Exception) { null }
                        }
                        Message(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            receiverId = doc.getString("receiverId") ?: "",
                            content = doc.getString("content") ?: "",
                            translatedContent = doc.getString("translatedContent") ?: "",
                            sourceLanguage = doc.getString("sourceLanguage") ?: "",
                            intentTag = intentTag,
                            isTranslated = doc.getBoolean("isTranslated") ?: false,
                            isRead = doc.getBoolean("isRead") ?: false,
                            timestamp = doc.getDate("timestamp")
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing message")
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(messages))
            }
        awaitClose { listener.remove() }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        content: String,
        intentTag: IntentTag?
    ): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        try {
            val messageData = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "content" to content,
                "intentTag" to intentTag?.name,
                "isRead" to false,
                "isTranslated" to false,
                "timestamp" to Date()
            )

            // Add message to chat subcollection
            val messageRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .await()

            // Update chat metadata
            firestore.collection("chats")
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to content,
                        "lastMessageTimestamp" to Date(),
                        "lastSenderId" to senderId,
                        "unreadCount.$receiverId" to com.google.firebase.firestore.FieldValue.increment(1)
                    )
                )
                .await()

            val message = Message(
                id = messageRef.id,
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                intentTag = intentTag,
                timestamp = Date()
            )
            emit(Resource.Success(message))
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            emit(Resource.Error(e.message ?: "Failed to send message"))
        }
    }

    fun getOrCreateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    doc.reference.update("isRead", true)
                }
            firestore.collection("chats")
                .document(chatId)
                .update("unreadCount.$userId", 0)
        } catch (e: Exception) {
            Timber.e(e, "Error marking messages as read")
        }
    }

    fun getClarifyLimit(userId: String): Flow<Resource<ClarifyLimit>> = callbackFlow {
        val listener = firestore.collection("clarify_limits")
            .document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load clarify limit"))
                    return@addSnapshotListener
                }
                val limit = if (doc != null && doc.exists()) {
                    ClarifyLimit(
                        userId = doc.getString("userId") ?: userId,
                        dailyCount = (doc.getLong("dailyCount") ?: 0).toInt(),
                        maxDaily = (doc.getLong("maxDaily") ?: 5).toInt(),
                        resetTime = doc.getDate("resetTime")
                    )
                } else {
                    ClarifyLimit(userId = userId)
                }
                trySend(Resource.Success(limit))
            }
        awaitClose { listener.remove() }
    }

    fun requestClarification(messageId: String, userId: String): Flow<Resource<Clarification>> = flow {
        emit(Resource.Loading())
        try {
            // Check and update limit
            val limitDoc = firestore.collection("clarify_limits")
                .document(userId)
                .get()
                .await()
            val currentCount = (limitDoc.getLong("dailyCount") ?: 0).toInt()
            if (currentCount >= 5) {
                emit(Resource.Error("Daily clarify limit reached (5/5). Try again tomorrow."))
                return@flow
            }

            // Increment count
            firestore.collection("clarify_limits")
                .document(userId)
                .update("dailyCount", currentCount + 1)
                .await()

            // Create AI clarification
            val clarification = Clarification(
                requesterId = userId,
                response = "The tone appears neutral and the intent seems genuine. No negative subtext detected.",
                tone = "Neutral",
                intent = "Genuine",
                timestamp = Date()
            )

            // Add to message
            firestore.collectionGroup("messages")
                .whereEqualTo("id", messageId)
                .get()
                .await()

            emit(Resource.Success(clarification))
        } catch (e: Exception) {
            Timber.e(e, "Error requesting clarification")
            emit(Resource.Error(e.message ?: "Failed to request clarification"))
        }
    }

    fun translateMessage(messageId: String, targetLanguage: String): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        try {
            // Mock translation - would integrate with translation API
            emit(Resource.Success(Message(id = messageId, isTranslated = true)))
        } catch (e: Exception) {
            Timber.e(e, "Error translating message")
            emit(Resource.Error(e.message ?: "Translation failed"))
        }
    }
}
