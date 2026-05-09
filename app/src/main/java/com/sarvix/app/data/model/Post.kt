package com.sarvix.app.data.model

import java.util.Date

enum class PostScope(val displayName: String) {
    INTERNATIONAL("International"),
    LOCAL("Local")
}

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String = "",
    val userMood: MoodStatus? = null,
    val userCountry: String = "",
    val content: String = "",
    val translatedContent: String = "",
    val sourceLanguage: String = "",
    val scope: PostScope = PostScope.INTERNATIONAL,
    val tags: List<String> = emptyList(),
    val videoUrl: String = "",
    val createdAt: Date? = null
)
