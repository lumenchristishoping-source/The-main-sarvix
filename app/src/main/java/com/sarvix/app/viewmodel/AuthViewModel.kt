package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.utils.Resource
import com.sarvix.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<com.google.firebase.auth.FirebaseUser>>(Resource.Idle())
    val loginState: StateFlow<Resource<com.google.firebase.auth.FirebaseUser>> = _loginState

    private val _signupState = MutableStateFlow<Resource<com.google.firebase.auth.FirebaseUser>>(Resource.Idle())
    val signupState: StateFlow<Resource<com.google.firebase.auth.FirebaseUser>> = _signupState

    private val _passwordResetState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val passwordResetState: StateFlow<Resource<Boolean>> = _passwordResetState

    private val _changePasswordState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val changePasswordState: StateFlow<Resource<Boolean>> = _changePasswordState

    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    init {
        _currentUser.value = authRepository.currentUser
    }

    fun login(email: String, password: String) {
        val errors = ValidationUtils.validateLogin(email, password)
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }
        _validationErrors.value = emptyMap()
        viewModelScope.launch {
            authRepository.login(email, password).collectLatest { _loginState.value = it }
        }
    }

    fun signup(email: String, password: String, username: String) {
        val errors = ValidationUtils.validateSignup(email, password, username)
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }
        _validationErrors.value = emptyMap()
        viewModelScope.launch {
            authRepository.signup(email, password, username).collectLatest { _signupState.value = it }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        val errors = ValidationUtils.validateEmail(email)
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }
        _validationErrors.value = emptyMap()
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).collectLatest { _passwordResetState.value = it }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isEmpty() || newPassword.length < 8) {
            _changePasswordState.value = Resource.Error("Please fill all fields correctly")
            return
        }
        viewModelScope.launch {
            authRepository.changePassword(currentPassword, newPassword).collectLatest { _changePasswordState.value = it }
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = Resource.Idle()
        _signupState.value = Resource.Idle()
        _currentUser.value = null
    }

    fun clearErrors() {
        _validationErrors.value = emptyMap()
    }

    fun clearStates() {
        _loginState.value = Resource.Idle()
        _signupState.value = Resource.Idle()
        _passwordResetState.value = Resource.Idle()
        _changePasswordState.value = Resource.Idle()
        _validationErrors.value = emptyMap()
    }
}
