package com.proyek.foolens.ui.auth.login

import com.proyek.foolens.domain.model.User

/**
 * State class untuk LoginScreen
 * Memuat semua state yang dibutuhkan pada layar login
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val validationErrors: Map<Field, String> = emptyMap()
) {
    enum class Field {
        EMAIL, PASSWORD
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