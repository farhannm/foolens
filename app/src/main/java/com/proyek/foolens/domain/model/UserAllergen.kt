package com.proyek.foolens.domain.model

data class UserAllergen(
    val id: Int,
    val name: String,
    val description: String?,
    val alternativeNames: String?,
    val category: AllergenCategory,
    val severityLevel: Int,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)
