package com.proyek.foolens.data.util

import com.proyek.foolens.ui.auth.login.LoginState

/**
 * Kelas untuk merepresentasikan hasil operasi network
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val errorMessage: String,
        val fieldError: LoginState.Field? = null
    ) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}