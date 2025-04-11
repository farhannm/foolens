package com.proyek.foolens.ui.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.preferences.PreferencesManager
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.util.Constants
import com.proyek.foolens.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /**
     * Update email state saat pengguna mengetik
     */
    fun onEmailChange(email: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(LoginState.Field.EMAIL)
            it.copy(email = email, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Update password state saat pengguna mengetik
     */
    fun onPasswordChange(password: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(LoginState.Field.PASSWORD)
            it.copy(password = password, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Validate input fields
     *
     * @return true if all fields are valid
     */
    private fun validateInput(): Boolean {
        val errors = mutableMapOf<LoginState.Field, String>()

        // Validate email
        if (state.value.email.isBlank()) {
            errors[LoginState.Field.EMAIL] = "Email tidak boleh kosong"
        } else {
            val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
            if (!emailRegex.matches(state.value.email)) {
                errors[LoginState.Field.EMAIL] = "Format email tidak valid"
            }
        }

        // Validate password
        if (state.value.password.isBlank()) {
            errors[LoginState.Field.PASSWORD] = "Password tidak boleh kosong"
        }

        // Update state with validation errors
        _state.update { it.copy(validationErrors = errors) }

        return errors.isEmpty()
    }

    /**
     * Proses login dengan validasi komprehensif
     */
    /**
     * Proses login dengan validasi komprehensif
     */
    fun login() {
        // Validate input locally first
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            // Update state to show loading
            _state.update { it.copy(isLoading = true, errorMessage = null, validationErrors = emptyMap()) }

            try {
                Log.d("LoginViewModel", "Memulai proses login untuk email: ${state.value.email}")

                // Perform login
                authUseCase.login(
                    email = state.value.email,
                    password = state.value.password
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            Log.d("LoginViewModel", "Login berhasil")

                            // Save token
                            val token = result.data.token
                            if (token.isNotEmpty()) {
                                // Save token using TokenManager
                                tokenManager.saveToken(token)

                                // Optional: Save additional user details in preferences
                                preferencesManager.saveString(Constants.PREF_USER_ID, result.data.id.toString())
                                preferencesManager.saveString(Constants.PREF_AUTH_TOKEN, token)
                                preferencesManager.saveBoolean(Constants.PREF_IS_LOGGED_IN, true)

                                Log.d("LoginViewModel", "Token dan data user tersimpan")
                            }

                            // Update state
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = result.data,
                                    errorMessage = null,
                                    validationErrors = emptyMap()
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.d("LoginViewModel", "Login error: ${result.errorMessage}, field error: ${result.fieldError}")

                            val fieldErrors = mutableMapOf<LoginState.Field, String>()

                            // Jika backend memberikan info field yang error
                            if (result.fieldError != null) {
                                fieldErrors[result.fieldError] = result.errorMessage
                                Log.d("LoginViewModel", "Menambahkan error ke field ${result.fieldError}: ${result.errorMessage}")
                            } else {
                                // Parsing specific error messages berdasarkan konten
                                when {
                                    result.errorMessage.contains("Email tidak terdaftar", ignoreCase = true) -> {
                                        fieldErrors[LoginState.Field.EMAIL] = "Email tidak terdaftar"
                                        Log.d("LoginViewModel", "Mendeteksi error email tidak terdaftar")
                                    }
                                    result.errorMessage.contains("Password salah", ignoreCase = true) ||
                                            result.errorMessage.contains("Password yang Anda masukkan salah", ignoreCase = true) -> {
                                        fieldErrors[LoginState.Field.PASSWORD] = "Password salah"
                                        Log.d("LoginViewModel", "Mendeteksi error password salah")
                                    }
                                    result.errorMessage.contains("email", ignoreCase = true) -> {
                                        fieldErrors[LoginState.Field.EMAIL] = result.errorMessage
                                        Log.d("LoginViewModel", "Mendeteksi error yang berkaitan dengan email")
                                    }
                                    result.errorMessage.contains("password", ignoreCase = true) -> {
                                        fieldErrors[LoginState.Field.PASSWORD] = result.errorMessage
                                        Log.d("LoginViewModel", "Mendeteksi error yang berkaitan dengan password")
                                    }
                                    else -> {
                                        // Error umum yang tidak terkait dengan field tertentu
                                        Log.d("LoginViewModel", "Error umum: ${result.errorMessage}")
                                    }
                                }
                            }

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = false,
                                    errorMessage = if (fieldErrors.isEmpty()) result.errorMessage else null,
                                    validationErrors = fieldErrors
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            Log.d("LoginViewModel", "Loading...")
                            _state.update {
                                it.copy(
                                    isLoading = true,
                                    errorMessage = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle unexpected errors
                Log.e("LoginViewModel", "Error tidak terduga saat login", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Reset error state
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null, validationErrors = emptyMap()) }
    }
}