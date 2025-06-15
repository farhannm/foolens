package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO untuk permintaan save scan
data class ScanDtoRequest(
    @SerializedName("barcode") val barcode: String,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("is_safe") val isSafe: Boolean,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("unsafe_allergens") val unsafeAllergens: List<String>? = null
)

// DTO untuk satu entitas scan history
data class ScanHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("is_safe") val isSafe: Boolean,
    @SerializedName("unsafe_allergens") val unsafeAllergens: List<String>?,
    @SerializedName("scan_notes") val scanNotes: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updateAt: String,
    @SerializedName("product") val product: ProductDto?,
)

// DTO untuk respons save scan
data class SaveScanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ScanHistoryDto
)

// DTO untuk respons get scan history
data class ScanHistoryListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ScanHistoryDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

data class PaginationDto(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("per_page") val perPage: Int
)

// DTO untuk respons delete scan
data class DeleteScanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?
)