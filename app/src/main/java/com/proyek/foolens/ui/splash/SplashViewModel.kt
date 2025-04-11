package com.proyek.foolens.ui.splash

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
class SplashViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * Memeriksa status login pengguna secara komprehensif
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val hasToken = tokenManager.hasToken()
                Log.d("SplashViewModel", "Token check - hasToken: $hasToken")

                if (hasToken) {
                    // Prioritaskan token checking
                    val userProfileJob = async { fetchUserProfile() }
                    val isUserProfileFetched = userProfileJob.await()

                    Log.d("SplashViewModel", "User Profile Fetch Result: $isUserProfileFetched")

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true, // Force true if token exists
                            errorMessage = if (!isUserProfileFetched) "Gagal memverifikasi profil" else null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            errorMessage = "Tidak ada sesi yang aktif"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Login status check failed", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = "Kesalahan sistem: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Mengambil profil pengguna untuk memvalidasi token
     * @return Boolean status pengambilan profil
     */
    private suspend fun fetchUserProfile(): Boolean {
        return try {
            var profileFetched = false
            authUseCase.getCurrentUser().collect { result ->
                profileFetched = when (result) {
                    is NetworkResult.Success -> {
                        Log.d("SplashViewModel", "User profile fetched successfully")
                        true
                    }
                    is NetworkResult.Error -> {
                        Log.e("SplashViewModel", "Failed to fetch user profile: ${result.errorMessage}")
                        false
                    }
                    is NetworkResult.Loading -> false
                }
            }
            profileFetched
        } catch (e: Exception) {
            Log.e("SplashViewModel", "User profile fetch exception", e)
            false
        }
    }
}