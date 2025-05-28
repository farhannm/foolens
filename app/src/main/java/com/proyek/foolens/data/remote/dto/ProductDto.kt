package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName(value = "name", alternate = ["product_name"]) val productName: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("ingredients") val ingredients: String?,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("popularity_score") val popularityScore: Double?,
    @SerializedName("nutritional_info") val nutritionalInfoDto: Any?
)

data class NutritionalInfoDto(
    @SerializedName("fat") val fat: Int?,
    @SerializedName("carbs") val carbs: Int?,
    @SerializedName("protein") val protein: Int?,
    @SerializedName("calories") val calories: Int?
)

data class AllergenItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("confidence_level") val confidenceLevel: Double,
    @SerializedName("is_direct") val isDirect: Boolean
)

data class ProductScanResponse(
    @SerializedName("found") val found: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("scanned_barcode") val scannedBarcode: String?,
    @SerializedName("product") val product: ProductDto?,
    @SerializedName("detected_allergens") val detectedAllergens: List<AllergenItemDto>?,
    @SerializedName("has_allergens") val hasAllergens: Boolean?
)