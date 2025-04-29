package com.proyek.foolens.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.ProfileUseCase
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
class ProfileViewModel @Inject constructor(
    private val profileUseCase: ProfileUseCase,
    private val authUseCase: AuthUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "ProfileViewModel"

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Selected image URI for profile picture update
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    init {
        loadProfile()
    }

    /**
     * Memuat data profil pengguna dari API
     */
    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            profileUseCase.getProfile().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                profile = result.data,
                                isLoading = false,
                                errorMessage = null,
                                // Update form fields with current data
                                nameField = result.data.name,
                                phoneField = result.data.phoneNumber ?: ""
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
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    /**
     * Memperbarui nama pada form
     */
    fun updateNameField(name: String) {
        _state.update { it.copy(nameField = name) }
    }

    /**
     * Memperbarui nomor telepon pada form
     */
    fun updatePhoneField(phone: String) {
        _state.update { it.copy(phoneField = phone) }
    }

    /**
     * Mengatur gambar profil yang dipilih
     */
    fun setSelectedImageUri(uri: Uri, file: File) {
        selectedImageUri = uri
        selectedImageFile = file
        _state.update { it.copy(selectedImageUri = uri) }
    }

    /**
     * Menyimpan perubahan profil ke API
     */
    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Get current state values
            val currentState = _state.value
            val name = currentState.nameField.takeIf { it != currentState.profile?.name }
            val phone = currentState.phoneField.takeIf { it != currentState.profile?.phoneNumber }
            val profilePicture = selectedImageFile

            // Check if there are any changes
            if (name == null && phone == null && profilePicture == null) {
                // No changes, just return success
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Tidak ada perubahan dilakukan"
                    )
                }
                return@launch
            }

            profileUseCase.updateProfile(name, phone, profilePicture).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Clear selected image after successful update
                        selectedImageUri = null
                        selectedImageFile = null

                        _state.update {
                            it.copy(
                                profile = result.data,
                                isLoading = false,
                                errorMessage = null,
                                successMessage = "Profil berhasil diperbarui",
                                // Update form fields with new data
                                nameField = result.data.name,
                                phoneField = result.data.phoneNumber ?: "",
                                selectedImageUri = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage,
                                successMessage = null
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    /**
     * Melakukan logout dan menghapus token
     */
    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            authUseCase.logout().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Clear token when logout is successful
                        tokenManager.clearToken()

                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                isLoggedOut = true
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        // Even if there's an error with the API, still clear the token locally
                        tokenManager.clearToken()

                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Logout failed: ${result.errorMessage}",
                                isLoggedOut = true // Still consider user logged out since token is cleared
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

    /**
     * Reset pesan sukses dan error
     */
    fun resetMessages() {
        _state.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}