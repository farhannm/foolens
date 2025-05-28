package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SendOtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SendOtpData? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
) {
    data class SendOtpData(
        @SerializedName("email") val email: String,
        @SerializedName("expires_in") val expiresIn: Int,
        @SerializedName("sending_method") val sendingMethod: String
    )
}