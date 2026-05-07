package com.sarvix.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.PostType
import com.sarvix.app.data.model.ReadSpace
import com.sarvix.app.data.model.TextPost
import com.sarvix.app.data.model.VideoPost
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository,
    private val translationRepository: TranslationRepository
) {
    companion object {
        const val MAX_VIDEO_DURATION = 30 // seconds
    }

    fun getPosts(
        readSpace: ReadSpace,
        lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    ): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val currentUser = authRepository.getUserById(authRepository.getCurrentUserId())
            
            var query = firestore.collection("posts")
                .whereEqualTo("readSpace", readSpace.name)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
            
            lastDocument?.let {
                query = query.startAfter(it)
            }
            
            val snapshot = query.get().await()
            
            val posts = snapshot.documents.mapNotNull { doc ->
                val type = doc.getString("type")?.let { PostType.valueOf(it) }
                when (type) {
                    PostType.TEXT -> doc.toObject(TextPost::class.java)
                    PostType.VIDEO -> doc.toObject(VideoPost::class.java)
                    else -> null
                }
            }
            
            // Auto-translate for international read space
            val translatedPosts = if (readSpace == ReadSpace.INTERNATIONAL) {
                posts.map { post ->
                    translatePostIfNeeded(post, currentUser?.languageCode ?: "en")
                }
            } else {
                posts
            }
            
            emit(Resource.Success(translatedPosts))
        } catch (e: Exception) {
            Timber.e(e, "Error getting posts")
            emit(Resource.Error(e.message ?: "Failed to load posts"))
        }
    }

    private suspend fun translatePostIfNeeded(post: Post, targetLanguage: String): Post {
        // Skip if already in target language or no content to translate
        if (post.content.isBlank()) return post
        
        return try {
            val translatedContent = translationRepository.translateText(post.content, targetLanguage)
            
            when (post) {
                is TextPost -> post.copy(
                    translatedContent = translatedContent,
                    sourceLanguage = "auto-detected"
                )
                is VideoPost -> post.copy(
                    translatedContent = translatedContent,
                    sourceLanguage = "auto-detected"
                )
                else -> post
            }
        } catch (e: Exception) {
            Timber.e(e, "Error translating post")
            post
        }
    }

    fun createTextPost(content: String, readSpace: ReadSpace): Flow<Resource<TextPost>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            val currentUser = authRepository.getUserById(currentUserId)
            
            if (currentUser == null) {
                emit(Resource.Error("User not found"))
                return@flow
            }
            
            val postId = UUID.randomUUID().toString()
            val post = TextPost(
                id = postId,
                authorId = currentUserId,
                authorUsername = currentUser.username,
                authorProfilePicture = currentUser.profilePictureUrl,
                content = content,
                timestamp = Date(),
                readSpace = readSpace
            )
            
            firestore.collection("posts")
                .document(postId)
                .set(post)
                .await()
            
            emit(Resource.Success(post))
        } catch (e: Exception) {
            Timber.e(e, "Error creating text post")
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    fun createVideoPost(
        videoUri: Uri,
        caption: String,
        duration: Int,
        readSpace: ReadSpace
    ): Flow<Resource<VideoPost>> = flow {
        emit(Resource.Loading())
        try {
            if (duration > MAX_VIDEO_DURATION) {
                emit(Resource.Error("Video duration exceeds 30 seconds limit"))
                return@flow
            }
            
            val currentUserId = authRepository.getCurrentUserId()
            val currentUser = authRepository.getUserById(currentUserId)
            
            if (currentUser == null) {
                emit(Resource.Error("User not found"))
                return@flow
            }
            
            // Upload video
            val videoId = UUID.randomUUID().toString()
            val videoRef = storage.reference
                .child("videos")
                .child("$videoId.mp4")
            
            videoRef.putFile(videoUri).await()
            val videoUrl = videoRef.downloadUrl.await().toString()
            
            // Note: Thumbnail generation is typically handled server-side via Firebase Functions
            // for mobile clients to avoid heavy processing. Storing videoUrl as thumbnailUrl for now.
            val thumbnailUrl = videoUrl
            
            val post = VideoPost(
                id = videoId,
                authorId = currentUserId,
                authorUsername = currentUser.username,
                authorProfilePicture = currentUser.profilePictureUrl,
                content = caption,
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                duration = duration,
                timestamp = Date(),
                readSpace = readSpace
            )
            
            firestore.collection("posts")
                .document(videoId)
                .set(post)
                .await()
            
            emit(Resource.Success(post))
        } catch (e: Exception) {
            Timber.e(e, "Error creating video post")
            emit(Resource.Error(e.message ?: "Failed to create video post"))
        }
    }

    fun deletePost(postId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("posts")
                .document(postId)
                .update("isDeleted", true)
                .await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post")
            emit(Resource.Error(e.message ?: "Failed to delete post"))
        }
    }

    fun reportPost(postId: String, reason: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            val report = hashMapOf(
                "id" to UUID.randomUUID().toString(),
                "reporterId" to currentUserId,
                "contentId" to postId,
                "contentType" to "POST",
                "reason" to reason,
                "status" to "PENDING",
                "createdAt" to Date()
            )
            
            firestore.collection("reports")
                .add(report)
                .await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error reporting post")
            emit(Resource.Error(e.message ?: "Failed to report post"))
        }
    }

    fun getUserPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { doc ->
                val type = doc.getString("type")?.let { PostType.valueOf(it) }
                when (type) {
                    PostType.TEXT -> doc.toObject(TextPost::class.java)
                    PostType.VIDEO -> doc.toObject(VideoPost::class.java)
                    else -> null
                }
            }
            
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user posts")
            emit(Resource.Error(e.message ?: "Failed to load user posts"))
        }
    }
}