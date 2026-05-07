package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.utils.Resource
import com.sarvix.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginState: StateFlow<Resource<FirebaseUser>?> = _loginState

    private val _signupState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signupState: StateFlow<Resource<FirebaseUser>?> = _signupState

    private val _passwordResetState = MutableStateFlow<Resource<Boolean>?>(null)
    val passwordResetState: StateFlow<Resource<Boolean>?> = _passwordResetState

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    fun login(email: String, password: String) {
        val errors = mutableMapOf<String, String>()
        
        if (!ValidationUtils.isValidEmail(email)) {
            errors["email"] = "Please enter a valid email"
        }
        if (password.isBlank()) {
            errors["password"] = "Password is required"
        }
        
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }
        
        _validationErrors.value = emptyMap()
        authRepository.login(email, password)
            .onEach { _loginState.value = it }
            .launchIn(viewModelScope)
    }

    fun signup(email: String, password: String, username: String) {
        val errors = mutableMapOf<String, String>()
        
        if (!ValidationUtils.isValidEmail(email)) {
            errors["email"] = "Please enter a valid email"
        }
        
        ValidationUtils.getPasswordError(password)?.let {
            errors["password"] = it
        }
        
        ValidationUtils.getUsernameError(username)?.let {
            errors["username"] = it
        }
        
        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }
        
        _validationErrors.value = emptyMap()
        authRepository.signup(email, password, username)
            .onEach { _signupState.value = it }
            .launchIn(viewModelScope)
    }

    fun sendPasswordResetEmail(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _validationErrors.value = mapOf("email" to "Please enter a valid email")
            return
        }
        
        _validationErrors.value = emptyMap()
        authRepository.sendPasswordResetEmail(email)
            .onEach { _passwordResetState.value = it }
            .launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = null
        _signupState.value = null
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    fun clearErrors() {
        _validationErrors.value = emptyMap()
        _loginState.value = null
        _signupState.value = null
        _passwordResetState.value = null
    }
}