package com.sarvix.app.utils

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 digit
        return password.length >= 8 &&
               password.any { it.isUpperCase() } &&
               password.any { it.isLowerCase() } &&
               password.any { it.isDigit() }
    }
    
    fun isValidUsername(username: String): Boolean {
        // Must start with @, followed by alphanumeric and underscores
        return username.startsWith("@") &&
               username.length >= 3 &&
               username.length <= 30 &&
               username.substring(1).all { it.isLetterOrDigit() || it == '_' }
    }
    
    fun isValidBio(bio: String): Boolean {
        return bio.length <= 500
    }
    
    fun getPasswordError(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one digit"
            else -> null
        }
    }
    
    fun getUsernameError(username: String): String? {
        return when {
            !username.startsWith("@") -> "Username must start with @"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 30 -> "Username must be at most 30 characters"
            !username.substring(1).all { it.isLetterOrDigit() || it == '_' } -> 
                "Username can only contain letters, numbers, and underscores"
            else -> null
        }
    }
}