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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>?>(null)
    val profileState: StateFlow<Resource<User>?> = _profileState

    private val _updateState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateState: StateFlow<Resource<Boolean>?> = _updateState

    private val _uploadState = MutableStateFlow<Resource<String>?>(null)
    val uploadState: StateFlow<Resource<String>?> = _uploadState

    private val _moodState = MutableStateFlow<Resource<Boolean>?>(null)
    val moodState: StateFlow<Resource<Boolean>?> = _moodState

    private val _availableInterests = MutableStateFlow<List<String>>(emptyList())
    val availableInterests: StateFlow<List<String>> = _availableInterests

    private val _availableCountries = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableCountries: StateFlow<List<Pair<String, String>>> = _availableCountries

    private val _availableLanguages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableLanguages: StateFlow<List<Pair<String, String>>> = _availableLanguages

    init {
        _availableInterests.value = profileRepository.getAvailableInterests()
        _availableCountries.value = profileRepository.getAvailableCountries()
        _availableLanguages.value = profileRepository.getAvailableLanguages()
    }

    fun loadProfile(userId: String = authRepository.getCurrentUserId()) {
        profileRepository.getUserProfile(userId)
            .onEach { _profileState.value = it }
            .launchIn(viewModelScope)
    }

    fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        mood: MoodStatus? = null,
        interests: List<String>? = null,
        country: String? = null,
        countryCode: String? = null,
        language: String? = null,
        languageCode: String? = null
    ) {
        profileRepository.updateProfile(
            authRepository.getCurrentUserId(),
            displayName,
            bio,
            mood,
            interests,
            country,
            countryCode,
            language,
            languageCode
        )
            .onEach { _updateState.value = it }
            .launchIn(viewModelScope)
    }

    fun uploadProfilePicture(imageUri: Uri) {
        profileRepository.uploadProfilePicture(authRepository.getCurrentUserId(), imageUri)
            .onEach { _uploadState.value = it }
            .launchIn(viewModelScope)
    }

    fun updateMood(mood: MoodStatus) {
        profileRepository.updateMood(authRepository.getCurrentUserId(), mood)
            .onEach { _moodState.value = it }
            .launchIn(viewModelScope)
    }

    fun isProfileComplete(): Boolean {
        return (_profileState.value as? Resource.Success)?.data?.isProfileComplete == true
    }

    fun clearStates() {
        _updateState.value = null
        _uploadState.value = null
        _moodState.value = null
    }
}