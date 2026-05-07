package com.sarvix.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sarvix.app.data.model.Match
import com.sarvix.app.data.model.MatchFilter
import com.sarvix.app.data.model.MatchStatus
import com.sarvix.app.data.model.MatchSuggestion
import com.sarvix.app.data.model.MatchType
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
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
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    companion object {
        const val MAX_SUGGESTIONS = 15
        const val MIN_LOCAL_MATCHES_THRESHOLD = 3
    }

    fun getMatchSuggestions(filter: MatchFilter): Flow<Resource<List<MatchSuggestion>>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            val currentUser = authRepository.getUserById(currentUserId)
            
            if (currentUser == null) {
                emit(Resource.Error("User not found"))
                return@flow
            }
            
            // Get existing matches to exclude
            val existingMatches = firestore.collection("matches")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
                .toObjects(Match::class.java)
                .map { it.matchedUserId }
                .toSet()
            
            // Get users to exclude (already matched or self)
            val excludeIds = existingMatches + currentUserId
            
            val suggestions = when (filter.type) {
                MatchType.LOCAL -> getLocalMatches(currentUser, excludeIds, filter)
                MatchType.GLOBAL -> getGlobalMatches(currentUser, excludeIds, filter)
            }
            
            // If local matches are less than threshold, suggest global matches
            val finalSuggestions = if (filter.type == MatchType.LOCAL && suggestions.size < MIN_LOCAL_MATCHES_THRESHOLD) {
                val globalSuggestions = getGlobalMatches(currentUser, excludeIds + suggestions.map { it.userId }.toSet(), filter)
                suggestions + globalSuggestions
            } else {
                suggestions
            }
            
            emit(Resource.Success(finalSuggestions.take(MAX_SUGGESTIONS)))
        } catch (e: Exception) {
            Timber.e(e, "Error getting match suggestions")
            emit(Resource.Error(e.message ?: "Failed to load suggestions"))
        }
    }

    private suspend fun getLocalMatches(
        currentUser: User,
        excludeIds: Set<String>,
        filter: MatchFilter
    ): List<MatchSuggestion> {
        // Get users from same country
        val snapshot = firestore.collection("users")
            .whereEqualTo("countryCode", currentUser.countryCode)
            .whereEqualTo("isProfileComplete", true)
            .limit(50)
            .get()
            .await()
        
        val users = snapshot.toObjects(User::class.java)
            .filter { it.id !in excludeIds }
        
        return calculateMatches(currentUser, users, MatchType.LOCAL)
            .filter { it.matchPercentage >= filter.minMatchPercentage }
            .sortedByDescending { it.matchPercentage }
            .take(MAX_SUGGESTIONS)
    }

    private suspend fun getGlobalMatches(
        currentUser: User,
        excludeIds: Set<String>,
        filter: MatchFilter
    ): List<MatchSuggestion> {
        // Get users from different countries
        val snapshot = firestore.collection("users")
            .whereNotEqualTo("countryCode", currentUser.countryCode)
            .whereEqualTo("isProfileComplete", true)
            .limit(50)
            .get()
            .await()
        
        val users = snapshot.toObjects(User::class.java)
            .filter { it.id !in excludeIds }
        
        return calculateMatches(currentUser, users, MatchType.GLOBAL)
            .filter { it.matchPercentage >= filter.minMatchPercentage }
            .sortedByDescending { it.matchPercentage }
            .take(MAX_SUGGESTIONS)
    }

    private fun calculateMatches(
        currentUser: User,
        potentialMatches: List<User>,
        matchType: MatchType
    ): List<MatchSuggestion> {
        return potentialMatches.map { otherUser ->
            val sharedInterests = currentUser.interests.intersect(otherUser.interests.toSet())
            val totalUniqueInterests = (currentUser.interests + otherUser.interests).toSet().size
            
            // Balanced mutual interest formula: Shared Interests / Total Unique Interests
            val matchPercentage = if (totalUniqueInterests > 0) {
                (sharedInterests.size.toDouble() / totalUniqueInterests) * 100
            } else {
                0.0
            }
            
            MatchSuggestion(
                userId = otherUser.id,
                username = otherUser.username,
                displayName = otherUser.displayName,
                profilePictureUrl = otherUser.profilePictureUrl,
                bio = otherUser.bio,
                mood = otherUser.mood,
                interests = otherUser.interests,
                country = otherUser.country,
                language = otherUser.language,
                sharedInterests = sharedInterests.toList(),
                matchPercentage = matchPercentage,
                matchType = matchType,
                isOnline = otherUser.isOnline
            )
        }
    }

    fun acceptMatch(matchedUserId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Check if match already exists
            val existingMatch = firestore.collection("matches")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("matchedUserId", matchedUserId)
                .get()
                .await()
                .documents
                .firstOrNull()
            
            if (existingMatch != null) {
                // Update existing match
                existingMatch.reference.update(
                    mapOf(
                        "status" to MatchStatus.ACCEPTED.name,
                        "isMutual" to true,
                        "matchedAt" to Date()
                    )
                ).await()
            } else {
                // Create new match
                val match = Match(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    matchedUserId = matchedUserId,
                    matchType = MatchType.GLOBAL,
                    status = MatchStatus.ACCEPTED,
                    isMutual = false,
                    createdAt = Date(),
                    matchedAt = Date()
                )
                
                firestore.collection("matches")
                    .document(match.id)
                    .set(match)
                    .await()
            }
            
            // Check for mutual match
            val mutualMatch = firestore.collection("matches")
                .whereEqualTo("userId", matchedUserId)
                .whereEqualTo("matchedUserId", currentUserId)
                .whereEqualTo("status", MatchStatus.ACCEPTED.name)
                .get()
                .await()
                .documents
                .firstOrNull()
            
            if (mutualMatch != null) {
                // Update both matches as mutual
                mutualMatch.reference.update("isMutual", true).await()
                
                existingMatch?.reference?.update("isMutual", true)?.await()
            }
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error accepting match")
            emit(Resource.Error(e.message ?: "Failed to accept match"))
        }
    }

    fun declineMatch(matchedUserId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Find and update match
            val existingMatch = firestore.collection("matches")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("matchedUserId", matchedUserId)
                .get()
                .await()
                .documents
                .firstOrNull()
            
            existingMatch?.reference?.update(
                mapOf(
                    "status" to MatchStatus.DECLINED.name,
                    "isMutual" to false
                )
            )?.await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error declining match")
            emit(Resource.Error(e.message ?: "Failed to decline match"))
        }
    }

    fun getMatches(): Flow<Resource<List<MatchSuggestion>>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            val matches = firestore.collection("matches")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", MatchStatus.ACCEPTED.name)
                .orderBy("matchedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Match::class.java)
            
            val matchSuggestions = matches.mapNotNull { match ->
                val user = authRepository.getUserById(match.matchedUserId)
                user?.let {
                    MatchSuggestion(
                        userId = it.id,
                        username = it.username,
                        displayName = it.displayName,
                        profilePictureUrl = it.profilePictureUrl,
                        bio = it.bio,
                        mood = it.mood,
                        interests = it.interests,
                        country = it.country,
                        language = it.language,
                        sharedInterests = match.sharedInterests,
                        matchPercentage = match.matchPercentage,
                        matchType = match.matchType,
                        isOnline = it.isOnline
                    )
                }
            }
            
            emit(Resource.Success(matchSuggestions))
        } catch (e: Exception) {
            Timber.e(e, "Error getting matches")
            emit(Resource.Error(e.message ?: "Failed to load matches"))
        }
    }

    fun blockUser(userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = authRepository.getCurrentUserId()
            
            // Find existing match and block
            val existingMatch = firestore.collection("matches")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("matchedUserId", userId)
                .get()
                .await()
                .documents
                .firstOrNull()
            
            if (existingMatch != null) {
                existingMatch.reference.update("status", MatchStatus.BLOCKED.name).await()
            } else {
                // Create blocked match
                val match = Match(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    matchedUserId = userId,
                    status = MatchStatus.BLOCKED,
                    createdAt = Date()
                )
                firestore.collection("matches")
                    .document(match.id)
                    .set(match)
                    .await()
            }
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error blocking user")
            emit(Resource.Error(e.message ?: "Failed to block user"))
        }
    }
}