package com.proyek.foolens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.ScanHistoryUseCase
import com.proyek.foolens.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val scanHistoryUseCase: ScanHistoryUseCase,
    val tokenManager: TokenManager // Changed from private to public
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadUserData()
        loadScanCountData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authUseCase.getCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val user = result.data
                        android.util.Log.d("HomeViewModel", "User data loaded: name=${user.name}, email=${user.email}, profilePicture=${user.profilePicture}")
                        _state.update {
                            it.copy(
                                user = user,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        loadProductSafetyStats(user.id.toString())
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("HomeViewModel", "Failed to load user data: ${result.errorMessage}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
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

    fun loadScanCountData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            scanHistoryUseCase.getScanCount().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                scanCount = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
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

    fun loadProductSafetyStats(userId: String) {
        viewModelScope.launch {
            scanHistoryUseCase.getProductSafetyStats(userId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                productSafetyStats = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
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

    fun refreshData() {
        loadUserData()
        loadScanCountData()
    }

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authUseCase.logout().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        tokenManager.clearToken()
                        _state.update {
                            it.copy(
                                user = null,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        tokenManager.clearToken()
                        _state.update {
                            it.copy(
                                user = null,
                                isLoading = false,
                                errorMessage = "Logout failed: ${result.errorMessage}"
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
}