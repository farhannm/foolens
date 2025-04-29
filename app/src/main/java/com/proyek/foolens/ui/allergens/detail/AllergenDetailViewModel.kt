package com.proyek.foolens.ui.allergens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.UserAllergenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AllergenDetailViewModel"

@HiltViewModel
class AllergenDetailViewModel @Inject constructor(
    private val userAllergenUseCase: UserAllergenUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AllergenDetailState())
    val state: StateFlow<AllergenDetailState> = _state.asStateFlow()

    fun initWithAllergen(allergen: UserAllergen) {
        // First get the current user ID
        viewModelScope.launch {
            Log.d(TAG, "Initializing with allergen: ${allergen.name}")

            // Initialize state with allergen data immediately to avoid UI delay
            _state.update {
                it.copy(
                    allergen = allergen,
                    severityLevel = allergen.severityLevel,
                    notes = allergen.notes ?: ""
                )
            }

            authUseCase.getCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val userId = result.data.id
                        Log.d(TAG, "Got user ID: $userId")

                        _state.update {
                            it.copy(
                                userId = userId,
                                isLoading = false
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error getting user: ${result.errorMessage}")
                        _state.update {
                            it.copy(
                                error = "Failed to get user information: ${result.errorMessage}",
                                isLoading = false
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

    fun updateSeverityLevel(level: Int) {
        _state.update { it.copy(severityLevel = level) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun saveAllergenDetails() {
        viewModelScope.launch {
            val state = _state.value
            val allergen = state.allergen ?: return@launch

            if (state.userId.isEmpty()) {
                _state.update { it.copy(error = "User ID not available, please try again") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            Log.d(TAG, "Saving allergen ${allergen.id} for user ${state.userId}")

            userAllergenUseCase.updateUserAllergen(
                userId = state.userId,
                allergenId = allergen.id,
                severityLevel = state.severityLevel,
                notes = state.notes.ifEmpty { null }
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Successfully updated allergen")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isUpdated = true,
                                error = null,
                                updatedAllergen = result.data
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error updating allergen: ${result.errorMessage}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.errorMessage,
                                isUpdated = false
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }

    fun deleteAllergen() {
        viewModelScope.launch {
            val state = _state.value
            val allergen = state.allergen ?: return@launch

            if (state.userId.isEmpty()) {
                _state.update { it.copy(error = "User ID not available, please try again") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            Log.d(TAG, "Deleting allergen ${allergen.id} for user ${state.userId}")

            userAllergenUseCase.deleteUserAllergen(
                userId = state.userId,
                allergenId = allergen.id
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Successfully deleted allergen")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isDeleted = true,
                                error = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error deleting allergen: ${result.errorMessage}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.errorMessage,
                                isDeleted = false
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }
}