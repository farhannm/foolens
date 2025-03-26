package com.proyek.foolens.data.util

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val errorMessage: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}