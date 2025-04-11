package com.proyek.foolens.ui.allergens.add

import com.proyek.foolens.domain.model.Allergen

data class AddAllergenState(
    val userId: String = "",
    val availableAllergens: List<Allergen> = emptyList(),
    val selectedAllergen: Allergen? = null, // Berubah dari List menjadi single object
    val severityLevel: Int = 1,
    val notes: String = "",
    val showAllergenPicker: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)