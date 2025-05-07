package com.proyek.foolens.domain.model

data class Product(
    val id: String,
    val categoryId: Int,
    val barcode: String?,
    val productName: String,
    val brand: String?,
    val imageUrl: String?,
    val ingredients: String?,
    val nutritionalInfo: Map<String, Any>?,
    val popularityScore: Double
)