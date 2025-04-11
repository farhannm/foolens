package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AllergenResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("allergens")
    val allergens: List<AllergenDto>,

    @SerializedName("total_count")
    val totalCount: Int
)