package com.sarvix.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.Post
import com.sarvix.app.data.model.ReadSpace
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.PostRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<Resource<List<Post>>?>(null)
    val postsState: StateFlow<Resource<List<Post>>?> = _postsState

    private val _createPostState = MutableStateFlow<Resource<Post>?>(null)
    val createPostState: StateFlow<Resource<Post>?> = _createPostState

    private val _deletePostState = MutableStateFlow<Resource<Boolean>?>(null)
    val deletePostState: StateFlow<Resource<Boolean>?> = _deletePostState

    private val _reportPostState = MutableStateFlow<Resource<Boolean>?>(null)
    val reportPostState: StateFlow<Resource<Boolean>?> = _reportPostState

    private val _currentReadSpace = MutableStateFlow(ReadSpace.INTERNATIONAL)
    val currentReadSpace: StateFlow<ReadSpace> = _currentReadSpace

    fun loadPosts(readSpace: ReadSpace = _currentReadSpace.value) {
        _currentReadSpace.value = readSpace
        postRepository.getPosts(readSpace)
            .onEach { _postsState.value = it }
            .launchIn(viewModelScope)
    }

    fun createTextPost(content: String, readSpace: ReadSpace = _currentReadSpace.value) {
        postRepository.createTextPost(content, readSpace)
            .onEach { resource ->
                _createPostState.value = when (resource) {
                    is Resource.Success -> Resource.Success(resource.data as Post)
                    is Resource.Error -> Resource.Error(resource.message ?: "Error")
                    is Resource.Loading -> Resource.Loading()
                }
            }
            .launchIn(viewModelScope)
    }

    fun createVideoPost(
        videoUri: Uri,
        caption: String,
        duration: Int,
        readSpace: ReadSpace = _currentReadSpace.value
    ) {
        postRepository.createVideoPost(videoUri, caption, duration, readSpace)
            .onEach { resource ->
                _createPostState.value = when (resource) {
                    is Resource.Success -> Resource.Success(resource.data as Post)
                    is Resource.Error -> Resource.Error(resource.message ?: "Error")
                    is Resource.Loading -> Resource.Loading()
                }
            }
            .launchIn(viewModelScope)
    }

    fun deletePost(postId: String) {
        postRepository.deletePost(postId)
            .onEach { _deletePostState.value = it }
            .launchIn(viewModelScope)
    }

    fun reportPost(postId: String, reason: String) {
        postRepository.reportPost(postId, reason)
            .onEach { _reportPostState.value = it }
            .launchIn(viewModelScope)
    }

    fun setReadSpace(readSpace: ReadSpace) {
        _currentReadSpace.value = readSpace
        loadPosts(readSpace)
    }

    fun clearStates() {
        _createPostState.value = null
        _deletePostState.value = null
        _reportPostState.value = null
    }
}
