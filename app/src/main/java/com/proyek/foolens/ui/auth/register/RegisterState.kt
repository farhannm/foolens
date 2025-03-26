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
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
    val user: User? = null
)