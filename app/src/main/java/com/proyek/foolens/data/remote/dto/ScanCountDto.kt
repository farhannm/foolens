package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScanCountDto(
    @SerializedName("total_scans") val totalCount: Int,
    @SerializedName("today_scans") val todayCount: Int,
    val safeCount: Int = 0,
    val unsafeCount: Int = 0
)

data class ScanCountResponseData(
    @SerializedName("today_scans") val todayCount: Int,
    @SerializedName("total_scans") val totalCount: Int,
    @SerializedName("display_message") val displayMessage: String
)

data class ScanCountResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ScanCountResponseData
)
