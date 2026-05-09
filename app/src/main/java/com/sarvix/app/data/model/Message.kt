package com.sarvix.app.data.model

import java.util.Date

enum class IntentTag(val displayName: String, val emoji: String) {
    JOKE("Joke", "😂"),
    SERIOUS("Serious", "😐"),
    ADVICE("Advice", "💡"),
    VENT("Vent", "😤"),
    RANT("Rant", "😠")
}

data class Clarification(
    val requesterId: String = "",
    val response: String = "",
    val tone: String = "",
    val intent: String = "",
    val timestamp: Date? = null
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val translatedContent: String = "",
    val sourceLanguage: String = "",
    val targetLanguage: String = "",
    val intentTag: IntentTag? = null,
    val isTranslated: Boolean = false,
    val isRead: Boolean = false,
    val isDeleted: Boolean = false,
    val timestamp: Date? = null,
    val clarifications: List<Clarification> = emptyList()
)

data class ChatPreview(
    val chatId: String = "",
    val otherUser: User = User(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Date? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

data class ClarifyLimit(
    val userId: String = "",
    val dailyCount: Int = 0,
    val maxDaily: Int = 5,
    val resetTime: Date? = null
) {
    fun isLimitReached(): Boolean = dailyCount >= maxDaily
    fun getRemainingCount(): Int = maxOf(0, maxDaily - dailyCount)
}
