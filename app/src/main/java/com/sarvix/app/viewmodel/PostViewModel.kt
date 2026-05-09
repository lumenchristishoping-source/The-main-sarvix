package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.PostScope
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.PostRepository
import com.sarvix.app.data.repository.ProfileRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<Resource<List<Post>>>(Resource.Idle())
    val postsState: StateFlow<Resource<List<Post>>> = _postsState

    private val _createState = MutableStateFlow<Resource<Post>>(Resource.Idle())
    val createState: StateFlow<Resource<Post>> = _createState

    fun loadPosts(scope: PostScope) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            postRepository.getPosts(userId, scope).collectLatest { _postsState.value = it }
        }
    }

    // FIXED: Fetch current user from Firestore before creating post
    fun createPost(content: String, scope: PostScope, videoUrl: String? = null) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) {
            _createState.value = Resource.Error("Not authenticated")
            return
        }
        viewModelScope.launch {
            // FIXED: Verify user document exists before creating post
            var userDocExists = false
            profileRepository.getUserProfile(userId).collectLatest { state ->
                if (state is Resource.Success) {
                    val user = state.data
                    if (user != null) {
                        userDocExists = true
                        // Now create the post with user data
                        postRepository.createPost(
                            userId = userId,
                            username = user.username,
                            userProfilePicture = user.profilePictureUrl,
                            userMood = user.mood,
                            userCountry = user.country,
                            content = content,
                            scope = scope,
                            videoUrl = videoUrl
                        ).collectLatest { _createState.value = it }
                    } else {
                        _createState.value = Resource.Error("User not found. Please complete profile setup.")
                    }
                } else if (state is Resource.Error && !userDocExists) {
                    _createState.value = Resource.Error("User not found. Please complete profile setup.")
                }
            }
        }
    }

    fun translatePost(postId: String) {
        viewModelScope.launch {
            postRepository.translatePost(postId).collectLatest { }
        }
    }

    fun clearCreateState() {
        _createState.value = Resource.Idle()
    }
}
