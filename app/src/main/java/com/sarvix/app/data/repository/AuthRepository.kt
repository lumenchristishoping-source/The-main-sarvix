package com.sarvix.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
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
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // FIXED: Never returns null - returns empty string if not logged in
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Save FCM token on login
                saveFcmToken(user.uid)
                // Update online status
                updateOnlineStatus(user.uid, true)
                emit(Resource.Success(user))
            } ?: emit(Resource.Error("Login failed"))
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // FIXED: Creates complete user document with all fields on signup
    fun signup(email: String, password: String, username: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            // Validate username format (@handle)
            if (!username.startsWith("@")) {
                emit(Resource.Error("Username must start with @"))
                return@flow
            }
            if (username.length < 3) {
                emit(Resource.Error("Username must be at least 3 characters"))
                return@flow
            }

            // Check if username is already taken
            val usernameQuery = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            if (!usernameQuery.isEmpty) {
                emit(Resource.Error("Username already taken"))
                return@flow
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                try {
                    // Update Firebase Auth profile
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user.updateProfile(profileUpdates).await()

                    // FIXED: Create COMPLETE user document with ALL fields
                    val newUser = User(
                        id = user.uid,
                        email = email,
                        username = username,
                        displayName = "",
                        bio = "",
                        mood = com.sarvix.app.data.model.MoodStatus.NEUTRAL,
                        interests = emptyList(),
                        country = "",
                        countryCode = "",
                        language = "",
                        languageCode = "",
                        profilePictureUrl = "",
                        fcmToken = "",
                        createdAt = Date(),
                        isActive = true,
                        isProfileComplete = false
                    )
                    firestore.collection("users")
                        .document(user.uid)
                        .set(newUser)
                        .await()

                    // Save FCM token
                    saveFcmToken(user.uid)

                    // Initialize clarify limits
                    firestore.collection("clarify_limits")
                        .document(user.uid)
                        .set(
                            hashMapOf(
                                "userId" to user.uid,
                                "dailyCount" to 0,
                                "maxDaily" to 5,
                                "resetTime" to Date()
                            )
                        )
                        .await()

                    emit(Resource.Success(user))
                } catch (dbError: Exception) {
                    Timber.e(dbError, "Firestore signup error")
                    // Cleanup: delete auth user if Firestore fails
                    try {
                        user.delete().await()
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to cleanup auth user after Firestore error")
                    }
                    emit(Resource.Error("Account setup failed: ${dbError.message}"))
                }
            } else {
                emit(Resource.Error("Signup failed: Could not create user"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Signup error")
            val message = when {
                e.message?.contains("email address is already in use") == true ->
                    "This email is already registered."
                e.message?.contains("password") == true ->
                    "Invalid password format. Must be at least 8 characters."
                e.message?.contains(" badly formatted") == true ->
                    "Invalid email format."
                else -> e.message ?: "Unknown error occurred during signup"
            }
            emit(Resource.Error(message))
        }
    }

    fun logout() {
        auth.currentUser?.let { user ->
            updateOnlineStatus(user.uid, false)
        }
        auth.signOut()
    }

    fun sendPasswordResetEmail(email: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            auth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Password reset error")
            emit(Resource.Error(e.message ?: "Failed to send reset email"))
        }
    }

    // Change password - requires recent authentication
    fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val user = auth.currentUser
            val email = user?.email
            if (user == null || email == null) {
                emit(Resource.Error("Not authenticated"))
                return@flow
            }
            // Re-authenticate
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            // Change password
            user.updatePassword(newPassword).await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Password change error")
            val message = when {
                e.message?.contains("password is invalid") == true -> "Current password is incorrect"
                e.message?.contains("weak") == true -> "New password is too weak. Use at least 8 characters."
                else -> e.message ?: "Failed to change password"
            }
            emit(Resource.Error(message))
        }
    }

    private fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        firestore.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            .addOnFailureListener { Timber.e(it, "Failed to update online status") }
    }

    private fun saveFcmToken(userId: String) {
        // Token is saved by SarvixMessagingService, but we ensure it's set here too
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                firestore.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener { Timber.d("FCM token saved on login/signup") }
                    .addOnFailureListener { e -> Timber.e(e, "Failed to save FCM token") }
            }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID: $userId")
            null
        }
    }
}
