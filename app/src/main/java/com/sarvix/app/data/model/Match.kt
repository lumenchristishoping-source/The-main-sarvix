package com.sarvix.app.data.model

import java.util.Date

enum class MatchScope(val displayName: String) {
    LOCAL("Local"),
    GLOBAL("Global")
}

data class Match(
    val id: String = "",
    val userId: String = "",
    val user: User = User(),
    val score: Int = 0,
    val sharedInterests: List<String> = emptyList(),
    val scope: MatchScope = MatchScope.LOCAL,
    val createdAt: Date? = null
)
