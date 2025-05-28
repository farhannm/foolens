package com.proyek.foolens.ui.auth.password

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.ui.auth.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {
    private var retryCount = 0
    private val maxRetries = 3

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun startEmailVerification(email: String, activity: Activity) {
        Timber.d("startEmailVerification: email=%s, retryCount=%d", email, retryCount)

        if (!isNetworkAvailable(activity)) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Tidak ada koneksi internet. Periksa koneksi Anda dan coba lagi."
                )
            }
            return
        }

        if (!validateEmail(email)) {
            Timber.e("Email validation failed: %s", _state.value.validationErrors)
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, email = email) }

        performEmailVerification(email, activity)
    }

    private fun performEmailVerification(email: String, activity: Activity) {
        Timber.d("Sending OTP for: %s (attempt %d)", email, retryCount + 1)

        viewModelScope.launch {
            authUseCase.sendOtp(email).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        retryCount = 0
                        Timber.d("OTP sent successfully")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isCodeSent = true,
                                email = email,
                                otpExpiresIn = result.data.expiresIn,
                                errorMessage = null,
                                validationErrors = emptyMap()
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        val shouldRetry = result.errorMessage.lowercase().let { msg ->
                            msg.contains("network") || msg.contains("timeout") || msg.contains("connect")
                        }
                        val errors = mutableMapOf<ChangePasswordState.Field, String>()
                        val errorMessage = when {
                            result.fieldError == LoginState.Field.EMAIL -> {
                                errors[ChangePasswordState.Field.EMAIL] = result.errorMessage
                                result.errorMessage
                            }
                            result.errorMessage.contains("not found", ignoreCase = true) ->
                                "Email tidak terdaftar."
                            result.errorMessage.contains("format", ignoreCase = true) ->
                                "Format email tidak valid."
                            result.errorMessage.contains("network", ignoreCase = true) ->
                                "Masalah koneksi jaringan. Periksa koneksi internet dan coba lagi."
                            else -> result.errorMessage
                        }

                        if (shouldRetry && retryCount < maxRetries) {
                            retryCount++
                            _state.update {
                                it.copy(errorMessage = "Mencoba ulang... ($retryCount/$maxRetries)")
                            }
                            kotlinx.coroutines.delay(3000L)
                            performEmailVerification(email, activity)
                        } else {
                            retryCount = 0
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = errorMessage,
                                    validationErrors = errors
                                )
                            }
                        }
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun resendVerificationCode(activity: Activity) {
        val email = _state.value.email
        Timber.d("Resending OTP for email: %s", email)

        if (!isNetworkAvailable(activity)) {
            _state.update {
                it.copy(errorMessage = "Tidak ada koneksi internet. Periksa koneksi Anda dan coba lagi.")
            }
            return
        }

        if (!_state.value.isCodeSent || email.isEmpty()) {
            _state.update {
                it.copy(errorMessage = "Tidak dapat mengirim ulang OTP. Silakan mulai dari awal.")
            }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, validationErrors = emptyMap()) }

        performEmailVerification(email, activity)
    }

    fun verifyCode(email: String, code: String) {
        Timber.d("Verifying OTP for email: %s, code: %s", email, code)

        if (!validateVerificationCode(code)) {
            return
        }

        viewModelScope.launch {
            authUseCase.verifyOtp(email, code).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isVerified = true,
                                errorMessage = null,
                                verificationCode = code,
                                validationErrors = emptyMap()
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        val errors = mutableMapOf<ChangePasswordState.Field, String>()
                        if (result.fieldError != null) {
                            errors[ChangePasswordState.Field.VERIFICATION_CODE] = result.errorMessage
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage,
                                isVerified = false,
                                validationErrors = errors
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun changePassword(email: String, newPassword: String, confirmPassword: String) {
        Timber.d("Changing password for: %s", email)

        if (!validatePasswords(newPassword, confirmPassword)) {
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, newPassword = newPassword, validationErrors = emptyMap()) }

        viewModelScope.launch {
            authUseCase.resetPassword(email, newPassword).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isPasswordChanged = true,
                                errorMessage = null,
                                validationErrors = emptyMap()
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        val errors = mutableMapOf<ChangePasswordState.Field, String>()
                        if (result.fieldError != null) {
                            when (result.fieldError) {
                                LoginState.Field.EMAIL -> errors[ChangePasswordState.Field.EMAIL] = result.errorMessage
                                LoginState.Field.PASSWORD -> errors[ChangePasswordState.Field.NEW_PASSWORD] = result.errorMessage
                                else -> {}
                            }
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage,
                                isPasswordChanged = false,
                                validationErrors = errors
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()
        if (email.isBlank()) {
            errors[ChangePasswordState.Field.EMAIL] = "Email tidak boleh kosong."
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors[ChangePasswordState.Field.EMAIL] = "Format email tidak valid."
        }
        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            false
        }
    }

    private fun validateVerificationCode(code: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()
        if (code.length != 6) {
            errors[ChangePasswordState.Field.VERIFICATION_CODE] = "Kode harus 6 digit."
        } else if (!code.all { it.isDigit() }) {
            errors[ChangePasswordState.Field.VERIFICATION_CODE] = "Kode harus berupa angka."
        }
        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            false
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()
        if (newPassword.length < 6) {
            errors[ChangePasswordState.Field.NEW_PASSWORD] = "Kata sandi harus minimal 6 karakter."
        }
        if (newPassword != confirmPassword) {
            errors[ChangePasswordState.Field.CONFIRM_PASSWORD] = "Kata sandi tidak cocok."
        }
        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            false
        }
    }

    fun resetRetryCount() {
        retryCount = 0
    }
}