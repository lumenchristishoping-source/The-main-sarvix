package com.sarvix.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Match(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val matchedUserId: String = "",
    val matchType: MatchType = MatchType.GLOBAL,
    val sharedInterests: List<String> = emptyList(),
    val totalUniqueInterests: Int = 0,
    val matchPercentage: Double = 0.0,
    val isMutual: Boolean = false,
    val status: MatchStatus = MatchStatus.PENDING,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val matchedAt: Date? = null
)

enum class MatchType {
    GLOBAL,
    LOCAL
}

enum class MatchStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    BLOCKED
}

data class MatchSuggestion(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val mood: MoodStatus = MoodStatus.NEUTRAL,
    val interests: List<String> = emptyList(),
    val country: String = "",
    val language: String = "",
    val sharedInterests: List<String> = emptyList(),
    val matchPercentage: Double = 0.0,
    val matchType: MatchType = MatchType.GLOBAL,
    val isOnline: Boolean = false
)

data class MatchFilter(
    val type: MatchType = MatchType.GLOBAL,
    val minMatchPercentage: Double = 0.0,
    val maxResults: Int = 15
)