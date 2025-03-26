package com.proyek.foolens.ui.home

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
class HomeViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Memuat data user yang sedang login
     */
    private fun loadUserData() {
        viewModelScope.launch {
            authUseCase.getCurrentUser().collect { user ->
                _state.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        errorMessage = if (user == null) "Tidak dapat memuat data pengguna" else null
                    )
                }
            }
        }
    }

    /**
     * Melakukan logout
     */
    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authUseCase.logout()
            _state.update {
                it.copy(
                    user = null,
                    isLoading = false
                )
            }
        }
    }
}
