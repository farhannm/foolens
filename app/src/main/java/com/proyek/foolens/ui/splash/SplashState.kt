package com.proyek.foolens.ui.splash

/**
 * State class untuk SplashScreen
 */
data class SplashState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)