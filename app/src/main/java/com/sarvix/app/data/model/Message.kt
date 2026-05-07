package com.sarvix.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val originalContent: String? = null, // For storing original text before translation
    val intentTag: IntentTag? = null,
    val isTranslated: Boolean = false,
    val translatedContent: String? = null,
    val sourceLanguage: String = "",
    val targetLanguage: String = "",
    val clarifications: List<Clarification> = emptyList(),
    val isRead: Boolean = false,
    val isDeleted: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null
)

data class Clarification(
    val id: String = "",
    val messageId: String = "",
    val requestedBy: String = "",
    val response: String = "",
    val tone: String = "",
    val intent: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)

enum class IntentTag(val displayName: String, val emoji: String, val color: Long) {
    JOKE("Joke", "😂", 0xFFFFD700),
    SERIOUS("Serious", "😐", 0xFF708090),
    ADVICE("Advice", "💡", 0xFF32CD32),
    VENT("Vent", "😤", 0xFFFF6347),
    RANT("Rant", "😠", 0xFFFF4500);
    
    companion object {
        fun fromString(value: String): IntentTag? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

data class Chat(
    @DocumentId
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantUsernames: Map<String, String> = emptyMap(), // userId -> username
    val participantPhotos: Map<String, String> = emptyMap(), // userId -> photoUrl
    val lastMessage: String = "",
    val lastMessageTimestamp: Date? = null,
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> count
    val isActive: Boolean = true,
    val createdAt: Date? = null
)

data class ChatPreview(
    val chatId: String = "",
    val otherUser: UserProfile = UserProfile(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Date? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)