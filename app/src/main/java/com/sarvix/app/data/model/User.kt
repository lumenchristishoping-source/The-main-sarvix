package com.sarvix.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val username: String = "", // @handle format
    val displayName: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val mood: MoodStatus = MoodStatus.NEUTRAL,
    val interests: List<String> = emptyList(),
    val country: String = "",
    val countryCode: String = "",
    val language: String = "",
    val languageCode: String = "",
    val isProfileComplete: Boolean = false,
    val isOnline: Boolean = false,
    @ServerTimestamp
    val lastSeen: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val fcmToken: String = "",
    val clarifyCountResetTime: Date? = null,
    val dailyClarifyCount: Int = 0
)

enum class MoodStatus(val displayName: String, val emoji: String) {
    NEUTRAL("Neutral", "😐"),
    HAPPY("Happy", "😊"),
    EXCITED("Excited", "🤩"),
    CALM("Calm", "😌"),
    THOUGHTFUL("Thoughtful", "🤔"),
    TIRED("Tired", "😴"),
    STRESSED("Stressed", "😰"),
    INSPIRED("Inspired", "✨"),
    FOCUSED("Focused", "🎯"),
    SOCIAL("Social", "🗣️"),
    CREATIVE("Creative", "🎨"),
    REFLECTIVE("Reflective", "🌙")
}

data class UserProfile(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val mood: MoodStatus = MoodStatus.NEUTRAL,
    val interests: List<String> = emptyList(),
    val country: String = "",
    val language: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Date? = null
)

fun User.toUserProfile(): UserProfile = UserProfile(
    id = id,
    username = username,
    displayName = displayName,
    bio = bio,
    profilePictureUrl = profilePictureUrl,
    mood = mood,
    interests = interests,
    country = country,
    language = language,
    isOnline = isOnline,
    lastSeen = lastSeen
)