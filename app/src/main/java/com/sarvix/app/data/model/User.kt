package com.sarvix.app.data.model

import java.util.Date

// User document in Firestore "users" collection
// Contains ALL fields requested by user
// - userId, username, email, displayName, bio, mood, interests, country, language
// - profilePictureUrl, fcmToken, createdAt, isActive, isProfileComplete

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val mood: MoodStatus? = MoodStatus.NEUTRAL,
    val interests: List<String> = emptyList(),
    val country: String = "",
    val countryCode: String = "",
    val language: String = "",
    val languageCode: String = "",
    val profilePictureUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Date? = null,
    val isActive: Boolean = true,
    val isOnline: Boolean = false,
    val isProfileComplete: Boolean = false,
    val lastSeen: Date? = null
)

// Mood enum with display properties
enum class MoodStatus(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    NEUTRAL("Neutral", "😐", "Feeling balanced"),
    HAPPY("Happy", "😊", "In a great mood"),
    EXCITED("Excited", "🤩", "Feeling enthusiastic"),
    CALM("Calm", "😌", "Peaceful and relaxed"),
    THOUGHTFUL("Thoughtful", "🤔", "In a reflective state"),
    TIRED("Tired", "😴", "Running low on energy"),
    STRESSED("Stressed", "😰", "Feeling the pressure"),
    INSPIRED("Inspired", "✨", "Full of ideas"),
    FOCUSED("Focused", "🎯", "In the zone"),
    SOCIAL("Social", "🗣️", "Ready to connect"),
    CREATIVE("Creative", "🎨", "Bursting with creativity"),
    REFLECTIVE("Reflective", "🌙", "Deep in thought")
}
