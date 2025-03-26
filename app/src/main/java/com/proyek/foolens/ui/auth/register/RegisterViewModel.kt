package com.proyek.foolens.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.domain.usecases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    /**
     * Update name state saat pengguna mengetik
     */
    fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    /**
     * Update email state saat pengguna mengetik
     */
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email) }
    }

    /**
     * Update password state saat pengguna mengetik
     */
    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password) }
    }

    /**
     * Update confirm password state saat pengguna mengetik
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword) }
    }

    /**
     * Proses registrasi
     */
    fun register() {
        // Validasi password dan confirm password sama
        if (state.value.password != state.value.confirmPassword) {
            _state.update {
                it.copy(errorMessage = "Password dan konfirmasi password tidak cocok")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            authUseCase.register(
                state.value.name,
                state.value.email,
                state.value.password
            ).collect { result ->
                result.fold(
                    onSuccess = { user ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                user = user,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * Reset error state
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}