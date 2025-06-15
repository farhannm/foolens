package com.proyek.foolens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.usecases.ProfileUseCase
import com.proyek.foolens.domain.usecases.ScanHistoryUseCase
import com.proyek.foolens.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scanHistoryUseCase: ScanHistoryUseCase,
    private val profileUseCase: ProfileUseCase,
    private val tokenManager: TokenManager
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
            profileUseCase.getProfile().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val profile = result.data
                        android.util.Log.d("HomeViewModel", "Profile data loaded: name=${profile.name}, email=${profile.email}, profilePicture=${profile.profilePicture}")
                        _state.update {
                            it.copy(
                                user = User(
                                    id = profile.id.toString(),
                                    name = profile.name,
                                    email = profile.email,
                                    phone = profile.phoneNumber ?: "",
                                    profilePicture = profile.profilePicture,
                                    token = tokenManager.getToken() ?: ""
                                ),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        loadProductSafetyStats(profile.id.toString())
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("HomeViewModel", "Failed to load profile data: ${result.errorMessage}")
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
}