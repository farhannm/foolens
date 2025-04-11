package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserAllergenRequest(
    @SerializedName("allergens")
    val allergens: List<AllergenEntry>
) {
    data class AllergenEntry(
        @SerializedName("id")
        val id: String,

        @SerializedName("severity_level")
        val severityLevel: String,

        @SerializedName("notes")
        val notes: String
    )
}