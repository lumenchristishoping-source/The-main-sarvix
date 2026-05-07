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

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Update online status
                updateOnlineStatus(user.uid, true)
                emit(Resource.Success(user))
            } ?: emit(Resource.Error("Login failed"))
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun signup(email: String, password: String, username: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            // Validate username format (@handle)
            if (!username.startsWith("@")) {
                emit(Resource.Error("Username must start with @"))
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
            result.user?.let { user ->
                // Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Create user document in Firestore
                val newUser = User(
                    id = user.uid,
                    email = email,
                    username = username,
                    isProfileComplete = false
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(newUser)
                    .await()
                
                emit(Resource.Success(user))
            } ?: emit(Resource.Error("Signup failed"))
        } catch (e: Exception) {
            Timber.e(e, "Signup error")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
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

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user")
            null
        }
    }
}