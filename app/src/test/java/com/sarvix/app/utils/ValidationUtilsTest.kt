package com.sarvix.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun isValidEmail_returnsTrueForValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"))
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.uk"))
    }

    @Test
    fun isValidEmail_returnsFalseForInvalidEmail() {
        assertFalse(ValidationUtils.isValidEmail("invalid-email"))
        assertFalse(ValidationUtils.isValidEmail("test@"))
        assertFalse(ValidationUtils.isValidEmail("@example.com"))
    }

    @Test
    fun getUsernameError_returnsNullForValidUsername() {
        assertTrue(ValidationUtils.getUsernameError("@user123") == null)
    }

    @Test
    fun getUsernameError_returnsErrorForMissingAt() {
        assertTrue(ValidationUtils.getUsernameError("user123") != null)
    }
}
