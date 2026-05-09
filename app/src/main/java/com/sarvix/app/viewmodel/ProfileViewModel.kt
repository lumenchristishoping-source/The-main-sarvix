package com.sarvix.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.ProfileRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val profileState: StateFlow<Resource<User>> = _profileState

    private val _updateState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val updateState: StateFlow<Resource<Boolean>> = _updateState

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val uploadState: StateFlow<Resource<String>> = _uploadState

    private val _currentUserMood = MutableStateFlow<MoodStatus>(MoodStatus.NEUTRAL)
    val currentUserMood: StateFlow<MoodStatus> = _currentUserMood

    fun loadUserProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.getUserProfile(userId).collectLatest { _profileState.value = it }
        }
    }

    fun loadCurrentUserMood() {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.getUserProfile(userId).collectLatest { state ->
                if (state is Resource.Success) {
                    state.data?.mood?.let { _currentUserMood.value = it }
                }
            }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        mood: MoodStatus? = null
    ) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.updateProfile(
                userId = userId,
                displayName = displayName,
                bio = bio,
                mood = mood
            ).collectLatest { _updateState.value = it }
        }
    }

    fun updateMood(mood: MoodStatus) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        _currentUserMood.value = mood
        viewModelScope.launch {
            profileRepository.updateMood(userId, mood).collectLatest { _updateState.value = it }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.uploadProfilePicture(userId, uri).collectLatest { _uploadState.value = it }
        }
    }

    fun completeProfileSetup(
        displayName: String,
        bio: String,
        mood: MoodStatus,
        interests: List<String>,
        country: String,
        countryCode: String,
        language: String,
        languageCode: String
    ) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.updateProfile(
                userId = userId,
                displayName = displayName,
                bio = bio,
                mood = mood,
                interests = interests,
                country = country,
                countryCode = countryCode,
                language = language,
                languageCode = languageCode
            ).collectLatest { _updateState.value = it }
        }
    }

    fun getAvailableInterests(): List<String> = profileRepository.getAvailableInterests()
    fun getAvailableCountries(): List<Pair<String, String>> = profileRepository.getAvailableCountries()
    fun getAvailableLanguages(): List<Pair<String, String>> = profileRepository.getAvailableLanguages()
}
