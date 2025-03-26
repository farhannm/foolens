package com.proyek.foolens.ui.auth.login

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
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

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
     * Proses login
     */
    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            authUseCase.login(state.value.email, state.value.password).collect { result ->
                result.fold(
                    onSuccess = { user ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = false,
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