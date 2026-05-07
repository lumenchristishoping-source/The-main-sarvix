package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.MatchFilter
import com.sarvix.app.data.model.MatchSuggestion
import com.sarvix.app.data.model.MatchType
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.MatchRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _suggestionsState = MutableStateFlow<Resource<List<MatchSuggestion>>?>(null)
    val suggestionsState: StateFlow<Resource<List<MatchSuggestion>>?> = _suggestionsState

    private val _matchesState = MutableStateFlow<Resource<List<MatchSuggestion>>?>(null)
    val matchesState: StateFlow<Resource<List<MatchSuggestion>>?> = _matchesState

    private val _acceptMatchState = MutableStateFlow<Resource<Boolean>?>(null)
    val acceptMatchState: StateFlow<Resource<Boolean>?> = _acceptMatchState

    private val _declineMatchState = MutableStateFlow<Resource<Boolean>?>(null)
    val declineMatchState: StateFlow<Resource<Boolean>?> = _declineMatchState

    private val _blockUserState = MutableStateFlow<Resource<Boolean>?>(null)
    val blockUserState: StateFlow<Resource<Boolean>?> = _blockUserState

    private val _selectedMatchType = MutableStateFlow(MatchType.GLOBAL)
    val selectedMatchType: StateFlow<MatchType> = _selectedMatchType

    fun loadSuggestions(matchType: MatchType = _selectedMatchType.value) {
        _selectedMatchType.value = matchType
        val filter = MatchFilter(type = matchType)
        matchRepository.getMatchSuggestions(filter)
            .onEach { _suggestionsState.value = it }
            .launchIn(viewModelScope)
    }

    fun loadMatches() {
        matchRepository.getMatches()
            .onEach { _matchesState.value = it }
            .launchIn(viewModelScope)
    }

    fun acceptMatch(matchedUserId: String) {
        matchRepository.acceptMatch(matchedUserId)
            .onEach { 
                _acceptMatchState.value = it
                // Refresh suggestions after accepting
                if (it is Resource.Success) {
                    loadSuggestions()
                }
            }
            .launchIn(viewModelScope)
    }

    fun declineMatch(matchedUserId: String) {
        matchRepository.declineMatch(matchedUserId)
            .onEach { 
                _declineMatchState.value = it
                // Refresh suggestions after declining
                if (it is Resource.Success) {
                    loadSuggestions()
                }
            }
            .launchIn(viewModelScope)
    }

    fun blockUser(userId: String) {
        matchRepository.blockUser(userId)
            .onEach { 
                _blockUserState.value = it
                // Refresh suggestions after blocking
                if (it is Resource.Success) {
                    loadSuggestions()
                }
            }
            .launchIn(viewModelScope)
    }

    fun setMatchType(matchType: MatchType) {
        _selectedMatchType.value = matchType
        loadSuggestions(matchType)
    }

    fun clearStates() {
        _acceptMatchState.value = null
        _declineMatchState.value = null
        _blockUserState.value = null
    }
}