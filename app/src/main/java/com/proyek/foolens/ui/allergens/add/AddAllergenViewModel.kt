package com.proyek.foolens.ui.allergens.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.usecases.AllergenUseCase
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.UserAllergenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAllergenViewModel @Inject constructor(
    private val allergenUseCase: AllergenUseCase,
    private val userAllergenUseCase: UserAllergenUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {
    private val TAG = "AddAllergenViewModel"

    private val _state = MutableStateFlow(AddAllergenState())
    val state: StateFlow<AddAllergenState> = _state.asStateFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            authUseCase.getCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Got current user: ${result.data.id}")
                        _state.update { it.copy(
                            userId = result.data.id,
                            isLoading = false
                        ) }
                        // Only load allergens after we have the user ID
                        if (result.data.id.isNotEmpty()) {
                            loadAvailableAllergens(result.data.id)
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to get user: ${result.errorMessage}")
                        _state.update { it.copy(
                            error = "Gagal memuat data. Silakan coba lagi.",
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Just waiting
                    }
                }
            }
        }
    }

    // Modified to take userId parameter
    private fun loadAvailableAllergens(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // First get user's allergens to filter them out
            userAllergenUseCase.getUserAllergens(userId).collect { userAllergensResult ->
                when (userAllergensResult) {
                    is NetworkResult.Success -> {
                        val userAllergenIds = userAllergensResult.data.map { it.id }.toSet()
                        Log.d(TAG, "User has ${userAllergenIds.size} allergens: $userAllergenIds")

                        // Then get all available allergens
                        allergenUseCase.getAllAllergens().collect { result ->
                            when (result) {
                                is NetworkResult.Success -> {
                                    // Filter out allergens that user already has
                                    val filteredAllergens = result.data.filter {
                                        it.id !in userAllergenIds
                                    }

                                    Log.d(TAG, "Loaded ${filteredAllergens.size} available allergens (filtered from ${result.data.size})")
                                    _state.update { it.copy(
                                        availableAllergens = filteredAllergens,
                                        isLoading = false
                                    ) }
                                }
                                is NetworkResult.Error -> {
                                    Log.e(TAG, "Failed to load allergens: ${result.errorMessage}")
                                    _state.update { it.copy(
                                        error = "Gagal memuat daftar alergen: ${result.errorMessage}",
                                        isLoading = false
                                    ) }
                                }
                                is NetworkResult.Loading -> {
                                    // Already set loading state
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to get user allergens: ${userAllergensResult.errorMessage}")
                        // If we can't get user allergens, just load all allergens
                        loadAllAllergens()
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }

    private fun loadAllAllergens() {
        viewModelScope.launch {
            allergenUseCase.getAllAllergens().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Loaded all ${result.data.size} allergens (no filter)")
                        _state.update { it.copy(
                            availableAllergens = result.data,
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to load allergens: ${result.errorMessage}")
                        _state.update { it.copy(
                            error = "Gagal memuat daftar alergen: ${result.errorMessage}",
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }

    fun searchAllergens(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadAvailableAllergens(_state.value.userId)
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            // Get user allergens first to exclude them from search results
            userAllergenUseCase.getUserAllergens(_state.value.userId).collect { userAllergensResult ->
                when (userAllergensResult) {
                    is NetworkResult.Success -> {
                        val userAllergenIds = userAllergensResult.data.map { it.id }.toSet()

                        // Then search allergens
                        allergenUseCase.searchAllergensByName(query).collect { result ->
                            when (result) {
                                is NetworkResult.Success -> {
                                    // Filter out allergens user already has
                                    val filteredResults = result.data.filter {
                                        it.id !in userAllergenIds
                                    }

                                    Log.d(TAG, "Search found ${filteredResults.size} allergens for query: $query (filtered from ${result.data.size})")
                                    _state.update { it.copy(
                                        availableAllergens = filteredResults,
                                        isLoading = false
                                    ) }
                                }
                                is NetworkResult.Error -> {
                                    Log.e(TAG, "Failed to search allergens: ${result.errorMessage}")
                                    _state.update { it.copy(
                                        error = "Gagal mencari alergen: ${result.errorMessage}",
                                        isLoading = false
                                    ) }
                                }
                                is NetworkResult.Loading -> {
                                    // Already set loading state
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        // If we can't get user allergens, search without filtering
                        searchAllAllergens(query)
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }

    private fun searchAllAllergens(query: String) {
        viewModelScope.launch {
            allergenUseCase.searchAllergensByName(query).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Search found ${result.data.size} allergens for query: $query (no filter)")
                        _state.update { it.copy(
                            availableAllergens = result.data,
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to search allergens: ${result.errorMessage}")
                        _state.update { it.copy(
                            error = "Gagal mencari alergen: ${result.errorMessage}",
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }

    fun toggleAllergenPicker(show: Boolean) {
        _state.update { it.copy(showAllergenPicker = show) }
    }

    fun setSelectedAllergen(allergen: Allergen) {
        _state.update { it.copy(selectedAllergen = allergen) }
    }

    fun clearSelectedAllergen() {
        _state.update { it.copy(selectedAllergen = null) }
    }

    fun updateSeverityLevel(level: Int) {
        _state.update { it.copy(severityLevel = level) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun saveAllergen() {
        viewModelScope.launch {
            val userId = _state.value.userId
            if (userId.isEmpty()) {
                _state.update { it.copy(error = "Gagal memuat data. Silakan coba lagi.") }
                return@launch
            }

            val selectedAllergen = _state.value.selectedAllergen
            if (selectedAllergen == null) {
                _state.update { it.copy(error = "Pilih satu alergen terlebih dahulu") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            Log.d(TAG, "Saving allergen ${selectedAllergen.name} for user $userId")

            // Karena hanya satu allergen, tidak perlu maps lagi
            val allergenIds = listOf(selectedAllergen.id)
            val severityLevels = mapOf(selectedAllergen.id to _state.value.severityLevel)
            val notes = mapOf(selectedAllergen.id to _state.value.notes)

            userAllergenUseCase.addUserAllergens(
                userId = userId,
                allergenIds = allergenIds,
                severityLevels = severityLevels,
                notes = notes
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Successfully added allergen")
                        _state.update { it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        ) }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to add allergen: ${result.errorMessage}")
                        _state.update { it.copy(
                            isLoading = false,
                            isSuccess = false,
                            error = "Gagal menyimpan data. Silakan coba lagi."
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Already set loading state
                    }
                }
            }
        }
    }
}