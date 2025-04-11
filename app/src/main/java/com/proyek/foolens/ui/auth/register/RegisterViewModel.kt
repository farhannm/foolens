package com.proyek.foolens.ui.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.preferences.PreferencesManager
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.util.Constants
import com.proyek.foolens.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    /**
     * Update name state saat pengguna mengetik
     */
    fun onNameChange(name: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(RegisterState.Field.NAME)
            it.copy(name = name, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Update email state saat pengguna mengetik
     */
    fun onEmailChange(email: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(RegisterState.Field.EMAIL)
            it.copy(email = email, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Update password state saat pengguna mengetik
     */
    fun onPasswordChange(password: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(RegisterState.Field.PASSWORD)
            it.copy(password = password, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Update phone number state saat pengguna mengetik
     */
    fun onPhoneChange(phone: String) {
        _state.update {
            val errors = it.validationErrors.toMutableMap()
            errors.remove(RegisterState.Field.PHONE)
            it.copy(phone = phone, validationErrors = errors, errorMessage = null)
        }
    }

    /**
     * Validate input fields
     *
     * @return true if all fields are valid
     */
    private fun validateInput(): Boolean {
        val errors = mutableMapOf<RegisterState.Field, String>()

        // Validate name
        if (state.value.name.isBlank()) {
            errors[RegisterState.Field.NAME] = "Nama tidak boleh kosong"
        }

        // Validate email
        if (state.value.email.isBlank()) {
            errors[RegisterState.Field.EMAIL] = "Email tidak boleh kosong"
        } else {
            val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
            if (!emailRegex.matches(state.value.email)) {
                errors[RegisterState.Field.EMAIL] = "Format email tidak valid"
            }
        }

        // Validate password
        if (state.value.password.isBlank()) {
            errors[RegisterState.Field.PASSWORD] = "Password tidak boleh kosong"
        } else if (state.value.password.length < 6) {
            errors[RegisterState.Field.PASSWORD] = "Password minimal 6 karakter"
        }

        // Validate phone
        if (state.value.phone.isBlank()) {
            errors[RegisterState.Field.PHONE] = "Nomor telepon tidak boleh kosong"
        } else {
            val phoneRegex = Regex("^(08|\\+62)[0-9]{8,12}$")
            if (!phoneRegex.matches(state.value.phone)) {
                errors[RegisterState.Field.PHONE] = "Format nomor telepon tidak valid"
            }
        }

        // Update state with validation errors
        _state.update { it.copy(validationErrors = errors) }

        return errors.isEmpty()
    }

    /**
     * Proses registrasi dengan validasi komprehensif
     */
    fun register() {
        // Validate input locally first
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            // Update state to show loading
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Perform registration
                authUseCase.register(
                    name = state.value.name,
                    email = state.value.email,
                    password = state.value.password,
                    phoneNumber = state.value.phone
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            // Log registration success
                            Log.d("RegisterViewModel", "Registration successful")

                            // Save token
                            val token = result.data.token
                            if (token.isNotEmpty()) {
                                // Save token using TokenManager
                                tokenManager.saveToken(token)

                                // Optional: Save additional user details in preferences
                                preferencesManager.saveString(Constants.PREF_USER_ID, result.data.id.toString())
                                preferencesManager.saveString(Constants.PREF_AUTH_TOKEN, token)
                                preferencesManager.saveBoolean(Constants.PREF_IS_LOGGED_IN, true)

                                Log.d("RegisterViewModel", "Token and user details saved successfully")
                            }

                            // Update state
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isRegistered = true,
                                    user = result.data,
                                    errorMessage = null,
                                    showSuccessMessage = true
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            // Parse error message to see if it's a field-specific error
                            val fieldErrors = mutableMapOf<RegisterState.Field, String>()
                            when {
                                result.errorMessage.contains("email", ignoreCase = true) -> {
                                    if (result.errorMessage.contains("sudah terdaftar", ignoreCase = true) ||
                                        result.errorMessage.contains("already exists", ignoreCase = true)) {
                                        fieldErrors[RegisterState.Field.EMAIL] = "Email sudah terdaftar"
                                    } else {
                                        fieldErrors[RegisterState.Field.EMAIL] = "Format email tidak valid"
                                    }
                                }
                                result.errorMessage.contains("password", ignoreCase = true) -> {
                                    fieldErrors[RegisterState.Field.PASSWORD] = "Password minimal 6 karakter"
                                }
                                result.errorMessage.contains("telepon", ignoreCase = true) ||
                                        result.errorMessage.contains("phone", ignoreCase = true) -> {
                                    fieldErrors[RegisterState.Field.PHONE] = "Format nomor telepon tidak valid"
                                }
                            }

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isRegistered = false,
                                    errorMessage = result.errorMessage,
                                    validationErrors = fieldErrors
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
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
                Log.e("RegisterViewModel", "Unexpected error during registration", e)
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