package com.proyek.foolens.domain.model

data class Allergen(
    val id: Int,
    val name: String,
    val severityLevel: Int,
    val description: String?
)

data class AllergenDetectionResult(
    val ocrText: String,
    val detectedAllergens: List<Allergen>,
    val hasAllergens: Boolean
)