package com.proyek.foolens.ui.allergens

import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.UserAllergen

data class AllergensState(
    // Personal allergens
    val isLoading: Boolean = false,
    val userAllergens: List<UserAllergen> = emptyList(),
    val filteredAllergens: List<UserAllergen> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,

    // Common allergens
    val commonAllergens: List<Allergen> = emptyList(),
    val isLoadingCommon: Boolean = false,
    val commonErrorMessage: String? = null,
)
