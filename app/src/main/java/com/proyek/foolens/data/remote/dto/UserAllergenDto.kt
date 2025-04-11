package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserAllergenResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("user_allergens")
    val userAllergens: List<UserAllergenDto>?,

    @SerializedName("total_count")
    val totalCount: Int? = null
)

data class UserAllergenDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("alternative_names")
    val alternativeNames: String?,

    @SerializedName("category")
    val category: AllergenCategoryDto,

    @SerializedName("severity_level")
    val severityLevel: Int?,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)