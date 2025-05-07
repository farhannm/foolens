package com.proyek.foolens.domain.model

data class ProductScanResult(
    val scannedBarcode: String,
    val found: Boolean,
    val product: Product? = null,
    val detectedAllergens: List<Allergen> = emptyList(),
    val hasAllergens: Boolean = false
)