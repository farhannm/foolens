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
    val user: User? = null
)