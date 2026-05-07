package com.sarvix.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

sealed class Post {
    abstract val id: String
    abstract val authorId: String
    abstract val authorUsername: String
    abstract val authorProfilePicture: String
    abstract val content: String
    abstract val timestamp: Date?
    abstract val type: PostType
    abstract val isDeleted: Boolean
}

data class TextPost(
    @DocumentId
    override val id: String = "",
    override val authorId: String = "",
    override val authorUsername: String = "",
    override val authorProfilePicture: String = "",
    override val content: String = "",
    val translatedContent: String? = null,
    val sourceLanguage: String = "",
    override val timestamp: Date? = null,
    override val type: PostType = PostType.TEXT,
    override val isDeleted: Boolean = false,
    val readSpace: ReadSpace = ReadSpace.INTERNATIONAL
) : Post()

data class VideoPost(
    @DocumentId
    override val id: String = "",
    override val authorId: String = "",
    override val authorUsername: String = "",
    override val authorProfilePicture: String = "",
    override val content: String = "", // Caption
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val duration: Int = 0, // in seconds, max 30
    val translatedContent: String? = null,
    val sourceLanguage: String = "",
    override val timestamp: Date? = null,
    override val type: PostType = PostType.VIDEO,
    override val isDeleted: Boolean = false,
    val readSpace: ReadSpace = ReadSpace.INTERNATIONAL
) : Post()

enum class PostType {
    TEXT,
    VIDEO
}

enum class ReadSpace {
    INTERNATIONAL, // Global text dialogue with auto translation
    LOCAL // Country-based community
}

data class PostInteraction(
    val postId: String = "",
    val userId: String = "",
    val interactionType: InteractionType = InteractionType.VIEW,
    @ServerTimestamp
    val timestamp: Date? = null
)

enum class InteractionType {
    VIEW,
    SHARE
}

// No public like counts or follower counts as per requirements