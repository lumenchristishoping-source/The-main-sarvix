package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.Match
import com.sarvix.app.data.model.MatchScope
import com.sarvix.app.data.model.User
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.MatchRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _matchesState = MutableStateFlow<Resource<List<Match>>>(Resource.Idle())
    val matchesState: StateFlow<Resource<List<Match>>> = _matchesState

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Idle())
    val userState: StateFlow<Resource<User>> = _userState

    private val _connectionState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val connectionState: StateFlow<Resource<String>> = _connectionState

    fun loadMatches(scope: MatchScope) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            matchRepository.findMatches(userId, scope).collectLatest { _matchesState.value = it }
        }
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            matchRepository.getUser(userId).collectLatest { _userState.value = it }
        }
    }

    fun connectWithUser(userId: String) {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            matchRepository.connectWithUser(currentUserId, userId).collectLatest { _connectionState.value = it }
        }
    }
}
