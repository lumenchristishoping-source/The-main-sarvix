package com.sarvix.app.utils

object ValidationUtils {

    fun validateLogin(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isBlank()) errors["email"] = "Email is required"
        else if (!isValidEmail(email)) errors["email"] = "Invalid email format"
        if (password.isBlank()) errors["password"] = "Password is required"
        return errors
    }

    fun validateSignup(email: String, password: String, username: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isBlank()) errors["email"] = "Email is required"
        else if (!isValidEmail(email)) errors["email"] = "Invalid email format"
        if (password.isBlank()) errors["password"] = "Password is required"
        else if (password.length < 8) errors["password"] = "Must be at least 8 characters"
        if (username.isBlank()) errors["username"] = "Username is required"
        else if (!username.startsWith("@")) errors["username"] = "Must start with @"
        else if (username.length < 3) errors["username"] = "Must be at least 3 characters"
        return errors
    }

    fun validateEmail(email: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isBlank()) errors["email"] = "Email is required"
        else if (!isValidEmail(email)) errors["email"] = "Invalid email format"
        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
