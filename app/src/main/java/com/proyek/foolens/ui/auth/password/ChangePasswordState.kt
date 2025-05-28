package com.proyek.foolens.ui.auth.password

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val email: String = "",
    val verificationCode: String = "",
    val newPassword: String = "",
    val errorMessage: String? = null,
    val validationErrors: Map<Field, String> = emptyMap(),
    val otpExpiresIn: Int? = null
) {
    enum class Field {
        EMAIL, VERIFICATION_CODE, NEW_PASSWORD, CONFIRM_PASSWORD
    }

    fun hasError(field: Field): Boolean = validationErrors.containsKey(field)
    fun getErrorFor(field: Field): String? = validationErrors[field]
}