package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO untuk permintaan save scan
data class ScanDtoRequest(
    @SerializedName("barcode") val barcode: String,
    @SerializedName("user_id") val userId: String? = null
)

// DTO untuk satu entitas scan history
data class ScanHistoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("is_safe") val isSafe: Boolean,
    @SerializedName("unsafe_allergens") val unsafeAllergens: List<String>?,
    @SerializedName("product") val product: ProductDto?,
    @SerializedName("created_at") val createdAt: String
)

// DTO untuk respons save scan
data class SaveScanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: SaveScanResponseData
)

data class SaveScanResponseData(
    @SerializedName("scan_history") val scanHistory: ScanHistoryDto
)

// DTO untuk respons get scan history
data class ScanHistoryListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ScanHistoryListResponseData
)

data class ScanHistoryListResponseData(
    @SerializedName("scan_history") val scanHistory: List<ScanHistoryDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int
)

// DTO untuk respons delete scan
data class DeleteScanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?
)