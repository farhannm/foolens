package com.proyek.foolens.ui.allergens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.domain.usecases.UserAllergenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllergenDetailViewModel @Inject constructor(
    private val userAllergenUseCase: UserAllergenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AllergenDetailState())
    val state: StateFlow<AllergenDetailState> = _state.asStateFlow()

    fun initWithAllergen(allergen: UserAllergen, userId: String) {
        _state.update {
            it.copy(
                allergen = allergen,
                userId = userId,
                severityLevel = allergen.severityLevel ?: 1,
                notes = allergen.notes ?: ""
            )
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
            _state.update { it.copy(isLoading = true, error = null) }

            val state = _state.value
            val allergen = state.allergen ?: return@launch

            userAllergenUseCase.updateUserAllergen(
                userId = state.userId,
                allergenId = allergen.id,
                severityLevel = state.severityLevel,
                notes = state.notes
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isUpdated = true,
                                error = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.errorMessage,
                                isUpdated = false
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

    fun deleteAllergen() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val state = _state.value
            val allergen = state.allergen ?: return@launch

            userAllergenUseCase.deleteUserAllergen(
                userId = state.userId,
                allergenId = allergen.id
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isDeleted = true,
                                error = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.errorMessage,
                                isDeleted = false
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