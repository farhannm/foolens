package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AllergenCategoryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AllergenCategoryData
) {
    // Convenience property to get categories
    val allergenCategories: List<AllergenCategoryDto>
        get() = data.categories ?: emptyList()
}

data class AllergenCategoryData(
    @SerializedName("categories")
    val categories: List<AllergenCategoryDto>?
)

data class AllergenCategoryDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("category_name")
    val name: String,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)