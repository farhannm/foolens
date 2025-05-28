package com.proyek.foolens.ui.scan

import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.Product

// Enum for scan modes
enum class ScanMode {
    ALLERGEN, BARCODE
}

data class ScanState(
    val isScanning: Boolean = false,
    val isProcessing: Boolean = false,
    val temporaryPauseScan: Boolean = false,
    val ocrText: String = "",
    val detectedAllergens: List<Allergen> = emptyList(),
    val hasAllergens: Boolean = false,
    val showAllergenAlert: Boolean = false,
    val showSafeProductAlert: Boolean = false,

    // Scan mode
    val currentScanMode: ScanMode = ScanMode.ALLERGEN,

    // Product barcode scanning fields
    val scannedBarcode: String? = null,
    val product: Product? = null,
    val productFound: Boolean = false,
    val showProductFoundDialog: Boolean = false,
    val showProductNotFoundDialog: Boolean = false,

    val errorMessage: String? = null,

    val scanHistoryId: String? = null
)