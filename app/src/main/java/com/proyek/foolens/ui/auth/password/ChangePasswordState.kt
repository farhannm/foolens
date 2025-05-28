package com.proyek.foolens.ui.auth.password

import java.util.Date

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val newPassword: String = "",
    val errorMessage: String? = null,
    val resetToken: String? = null,
    val validationErrors: Map<Field, String> = emptyMap(),
    val lastResetAttempt: Date? = null
) {
    enum class Field {
        PHONE, VERIFICATION_CODE, NEW_PASSWORD, CONFIRM_PASSWORD
    }

    fun hasError(field: Field): Boolean = validationErrors.containsKey(field)
    fun getErrorFor(field: Field): String? = validationErrors[field]

    fun isResetTokenValid(): Boolean {
        if (lastResetAttempt == null) return false
        val currentTime = Date()
        val diffInMillis = currentTime.time - lastResetAttempt.time
        val diffInMinutes = diffInMillis / (1000 * 60)
        return diffInMinutes <= 15
    }
}