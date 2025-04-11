package com.proyek.foolens.ui.allergens.detail

import com.proyek.foolens.domain.model.UserAllergen

data class AllergenDetailState(
    val allergen: UserAllergen? = null,
    val userId: String = "",
    val severityLevel: Int = 1,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)