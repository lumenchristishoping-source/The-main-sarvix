package com.sarvix.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sarvix.app.data.model.Match
import com.sarvix.app.data.model.MatchScope
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun findMatches(userId: String, scope: MatchScope): Flow<Resource<List<Match>>> = flow {
        emit(Resource.Loading())
        try {
            // Get current user
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java) ?: User()

            // Get potential matches
            val existingMatches = firestore.collection("matches")
                .whereEqualTo("matcherId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("matchedUserId") }
                .toSet()

            // Query users
            var query = firestore.collection("users")
                .whereEqualTo("isActive", true)
                .whereEqualTo("isProfileComplete", true)
                .limit(30)

            if (scope == MatchScope.LOCAL && user.countryCode.isNotEmpty()) {
                query = query.whereEqualTo("countryCode", user.countryCode)
            }

            val users = query.get().await().documents
                .mapNotNull { doc ->
                    try {
                        val moodStr = doc.getString("mood")
                        val mood = moodStr?.let {
                            try { MoodStatus.valueOf(it) } catch (e: Exception) { null }
                        }
                        User(
                            id = doc.id,
                            username = doc.getString("username") ?: "",
                            displayName = doc.getString("displayName") ?: "",
                            bio = doc.getString("bio") ?: "",
                            mood = mood,
                            interests = (doc.get("interests") as? List<String>) ?: emptyList(),
                            country = doc.getString("country") ?: "",
                            countryCode = doc.getString("countryCode") ?: "",
                            language = doc.getString("language") ?: "",
                            languageCode = doc.getString("languageCode") ?: "",
                            profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                            isOnline = doc.getBoolean("isOnline") ?: false
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing user")
                        null
                    }
                }
                .filter { it.id != userId && it.id !in existingMatches }
                .map { otherUser ->
                    val shared = user.interests.intersect(otherUser.interests.toSet())
                    val total = user.interests.union(otherUser.interests.toSet())
                    val score = if (total.isNotEmpty()) (shared.size * 100 / total.size).coerceAtMost(100) else 0
                    Match(
                        user = otherUser,
                        score = score,
                        sharedInterests = shared.toList(),
                        scope = scope
                    )
                }
                .sortedByDescending { it.score }
                .take(15)

            emit(Resource.Success(users))
        } catch (e: Exception) {
            Timber.e(e, "Error finding matches")
            emit(Resource.Error(e.message ?: "Failed to find matches"))
        }
    }

    fun getUser(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            val moodStr = doc.getString("mood")
            val mood = moodStr?.let {
                try { MoodStatus.valueOf(it) } catch (e: Exception) { null }
            }
            val user = User(
                id = doc.id,
                username = doc.getString("username") ?: "",
                displayName = doc.getString("displayName") ?: "",
                bio = doc.getString("bio") ?: "",
                mood = mood,
                interests = (doc.get("interests") as? List<String>) ?: emptyList(),
                country = doc.getString("country") ?: "",
                language = doc.getString("language") ?: "",
                profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                isOnline = doc.getBoolean("isOnline") ?: false
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user")
            emit(Resource.Error(e.message ?: "User not found"))
        }
    }

    fun connectWithUser(currentUserId: String, userId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            // Create chat
            val chatId = if (currentUserId < userId) "${currentUserId}_${userId}" else "${userId}_${currentUserId}"
            val chatData = hashMapOf(
                "participants" to listOf(currentUserId, userId),
                "lastMessage" to "",
                "lastMessageTimestamp" to Date(),
                "createdAt" to Date(),
                "unreadCount" to hashMapOf(currentUserId to 0, userId to 0)
            )
            firestore.collection("chats")
                .document(chatId)
                .set(chatData)
                .await()

            // Create match record
            val matchData = hashMapOf(
                "matcherId" to currentUserId,
                "matchedUserId" to userId,
                "chatId" to chatId,
                "createdAt" to Date()
            )
            firestore.collection("matches")
                .add(matchData)
                .await()

            emit(Resource.Success(chatId))
        } catch (e: Exception) {
            Timber.e(e, "Error connecting with user")
            emit(Resource.Error(e.message ?: "Failed to connect"))
        }
    }
}
