package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Model untuk error response dari API
 * Disesuaikan dengan format respons error dari backend
 */
data class ErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("error_code")
    val error_code: String?,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null,

    @SerializedName("error_details")
    val errorDetails: String? = null
)