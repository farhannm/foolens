package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateUserAllergenRequest(
    @SerializedName("severity_level")
    val severityLevel: String? = null,

    @SerializedName("notes")
    val notes: String? = null
)