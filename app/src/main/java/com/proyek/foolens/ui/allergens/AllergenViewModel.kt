package com.proyek.foolens.ui.allergens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.UserAllergenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AllergensViewModel"

@HiltViewModel
class AllergensViewModel @Inject constructor(
    private val userAllergenUseCase: UserAllergenUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AllergensState())
    val state: StateFlow<AllergensState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // Dapatkan user ID saat ini
            authUseCase.getCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val userId = result.data.id
                        if (userId.isNotEmpty()) {
                            loadUserAllergens(userId)
                        } else {
                            _state.update { it.copy(
                                errorMessage = "User ID tidak valid",
                                isLoading = false
                            ) }
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update { it.copy(
                            errorMessage = result.errorMessage,
                            isLoading = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun loadUserAllergens(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            userAllergenUseCase.getUserAllergens(userId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update { it.copy(
                            isLoading = false,
                            userAllergens = result.data,
                            filteredAllergens = result.data,
                            isRefreshing = false
                        ) }
                    }
                    is NetworkResult.Error -> {
                        _state.update { it.copy(
                            isLoading = false,
                            errorMessage = result.errorMessage,
                            isRefreshing = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun refreshAllergens() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }

            // Dapatkan user ID saat ini
            authUseCase.getCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val userId = result.data.id
                        if (userId.isNotEmpty()) {
                            loadUserAllergens(userId)
                        } else {
                            _state.update { it.copy(
                                errorMessage = "User ID tidak valid",
                                isRefreshing = false
                            ) }
                        }
                    }
                    is NetworkResult.Error -> {
                        _state.update { it.copy(
                            errorMessage = result.errorMessage,
                            isRefreshing = false
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Sudah dihandle di atas
                    }
                }
            }
        }
    }

    fun loadCommonAllergens() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCommon = true, commonErrorMessage = null) }

            // Use the userAllergenUseCase to get all allergens from the API
            userAllergenUseCase.getAllAllergens().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Successfully loaded ${result.data.size} common allergens")
                        _state.update { it.copy(
                            isLoadingCommon = false,
                            commonAllergens = result.data
                        ) }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error loading common allergens: ${result.errorMessage}")
                        _state.update { it.copy(
                            isLoadingCommon = false,
                            commonErrorMessage = result.errorMessage
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Loading state is already set
                    }
                }
            }
        }
    }

    fun searchCommonAllergens(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                // Reload all allergens if query is empty
                loadCommonAllergens()
                return@launch
            }

            _state.update { it.copy(isLoadingCommon = true) }

            // Use the API search functionality
            userAllergenUseCase.searchAllergens(query).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Found ${result.data.size} allergens matching '$query'")
                        _state.update { it.copy(
                            isLoadingCommon = false,
                            commonAllergens = result.data
                        ) }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error searching allergens: ${result.errorMessage}")
                        _state.update { it.copy(
                            isLoadingCommon = false,
                            commonErrorMessage = result.errorMessage
                        ) }
                    }
                    is NetworkResult.Loading -> {
                        // Loading state is already set
                    }
                }
            }
        }
    }

    fun setSelectedTab(tab: AllergensState.Tab) {
        _state.update { it.copy(selectedTab = tab) }
    }
}