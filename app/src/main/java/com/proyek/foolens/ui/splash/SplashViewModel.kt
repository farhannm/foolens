package com.proyek.foolens.ui.splash

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
class SplashViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * Memeriksa status login pengguna
     * untuk menentukan navigasi awal
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            authUseCase.isLoggedIn().collect { isLoggedIn ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }
}