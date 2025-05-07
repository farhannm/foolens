package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.proyek.foolens.domain.model.Product

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("ingredients") val ingredients: String?,
    @SerializedName("image_url") val imageUrl: String?
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            categoryId = 0,
            barcode = null,
            productName = name,
            brand = brand,
            imageUrl = imageUrl,
            ingredients = ingredients,
            nutritionalInfo = null,
            popularityScore = 0.0
        )
    }
}

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