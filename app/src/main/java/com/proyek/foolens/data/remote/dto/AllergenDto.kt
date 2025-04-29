package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AllergenDetectionResponse(
    @SerializedName("ocr_text")
    val ocrText: String,

    @SerializedName("detected_allergens")
    val detectedAllergens: List<AllergenDto>,

    @SerializedName("has_allergens")
    val hasAllergens: Boolean
)

data class AllergenDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("allergen_name") val allergenName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("severity_level") val severityLevel: Int?,
    @SerializedName("alternative_names") val alternativeNames: String? = null
)