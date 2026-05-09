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
class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getPosts(userId: String, scope: PostScope): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())

        // Get current user for local filtering
        val userDoc = try {
            firestore.collection("users").document(userId).get().await()
        } catch (e: Exception) {
            null
        }
        val userCountry = userDoc?.getString("country") ?: ""

        val query = firestore.collection("posts")
            .whereEqualTo("scope", scope.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error loading posts")
                trySend(Resource.Error(error.message ?: "Failed to load posts"))
                return@addSnapshotListener
            }
            val posts = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val scopeStr = doc.getString("scope") ?: "INTERNATIONAL"
                    val postScope = try { PostScope.valueOf(scopeStr) } catch (e: Exception) { PostScope.INTERNATIONAL }

                    val moodStr = doc.getString("userMood")
                    val mood = moodStr?.let {
                        try { MoodStatus.valueOf(it) } catch (e: Exception) { null }
                    }

                    // For local scope, filter by country
                    if (scope == PostScope.LOCAL) {
                        val postCountry = doc.getString("userCountry") ?: ""
                        if (postCountry.isNotEmpty() && postCountry != userCountry) {
                            return@mapNotNull null
                        }
                    }

                    Post(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "",
                        userProfilePicture = doc.getString("userProfilePicture") ?: "",
                        userMood = mood,
                        userCountry = doc.getString("userCountry") ?: "",
                        content = doc.getString("content") ?: "",
                        translatedContent = doc.getString("translatedContent") ?: "",
                        sourceLanguage = doc.getString("sourceLanguage") ?: "",
                        scope = postScope,
                        tags = (doc.get("tags") as? List<String>) ?: emptyList(),
                        videoUrl = doc.getString("videoUrl") ?: "",
                        createdAt = doc.getDate("createdAt")
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing post")
                    null
                }
            } ?: emptyList()
            trySend(Resource.Success(posts))
        }
        awaitClose { listener.remove() }
    }

    fun createPost(
        userId: String,
        username: String,
        userProfilePicture: String,
        userMood: MoodStatus?,
        userCountry: String,
        content: String,
        scope: PostScope,
        videoUrl: String? = null
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val postData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "userProfilePicture" to userProfilePicture,
                "userMood" to userMood?.name,
                "userCountry" to userCountry,
                "content" to content,
                "scope" to scope.name,
                "tags" to emptyList<String>(),
                "videoUrl" to (videoUrl ?: ""),
                "createdAt" to Date()
            )

            val docRef = firestore.collection("posts")
                .add(postData)
                .await()

            val post = Post(
                id = docRef.id,
                userId = userId,
                username = username,
                userProfilePicture = userProfilePicture,
                userMood = userMood,
                userCountry = userCountry,
                content = content,
                scope = scope,
                videoUrl = videoUrl ?: "",
                createdAt = Date()
            )
            emit(Resource.Success(post))
        } catch (e: Exception) {
            Timber.e(e, "Error creating post")
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    fun translatePost(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            // Mock translation
            val doc = firestore.collection("posts").document(postId).get().await()
            emit(Resource.Success(Post(id = postId)))
        } catch (e: Exception) {
            Timber.e(e, "Error translating post")
            emit(Resource.Error(e.message ?: "Translation failed"))
        }
    }
}
