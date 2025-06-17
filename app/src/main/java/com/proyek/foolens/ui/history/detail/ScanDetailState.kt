package com.proyek.foolens.ui.history.detail

import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.Product
import com.proyek.foolens.domain.model.ScanHistory

data class ScanDetailState (
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val scanHistory: ScanHistory? = null,
    val scannedBarcode: String? = null,
    val product: Product? = null,
    val detectedAllergens: List<Allergen> = emptyList(),
    val unsafeAllergens: List<String> = emptyList(),
    val isSafe: Boolean? = null,
    val deleteSuccess: Boolean = false
)