package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: VerifyOtpData? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
) {
    data class VerifyOtpData(
        @SerializedName("email") val email: String,
        @SerializedName("verified_at") val verifiedAt: String
    )
}