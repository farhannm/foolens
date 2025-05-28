package com.proyek.foolens.domain.model

data class Product(
    val id: String,
    val productName: String?,
    val brand: String?,
    val barcode: String?,
    val imageUrl: String?,
    val ingredients: String?,
    val categoryId: Int,
    val nutritionalInfo: NutritionalInfo?,
    val popularityScore: Double?
)

data class NutritionalInfo(
    val fat: Int?,
    val carbs: Int?,
    val protein: Int?,
    val calories: Int?
)