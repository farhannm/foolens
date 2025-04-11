package com.proyek.foolens.ui.auth.register

import com.proyek.foolens.domain.model.User

/**
 * State class untuk RegisterScreen
 * Memuat semua state yang dibutuhkan pada layar register
 */
data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
    val user: User? = null,
    val showSuccessMessage: Boolean = false,
    val validationErrors: Map<Field, String> = emptyMap()
) {
    enum class Field {
        NAME, EMAIL, PASSWORD, PHONE
    }

    /**
     * Check if the form has any validation errors
     */
    fun hasErrors(): Boolean = validationErrors.isNotEmpty()

    /**
     * Check if a specific field has an error
     */
    fun hasError(field: Field): Boolean = validationErrors.containsKey(field)

    /**
     * Get error message for a specific field
     */
    fun getErrorFor(field: Field): String? = validationErrors[field]
}