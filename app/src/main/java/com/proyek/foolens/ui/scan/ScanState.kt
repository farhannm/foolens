package com.proyek.foolens.ui.scan

import com.proyek.foolens.domain.model.Allergen

data class ScanState(
    val isScanning: Boolean = false,
    val isProcessing: Boolean = false,
    val temporaryPauseScan: Boolean = false,
    val ocrText: String = "",
    val detectedAllergens: List<Allergen> = emptyList(),
    val hasAllergens: Boolean = false,
    val showAllergenAlert: Boolean = false,
    val showSafeProductAlert: Boolean = false,
    val errorMessage: String? = null
)