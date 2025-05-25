package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductSafetyStatsDto(
    @SerializedName("total_products") val totalCount: Int,
    @SerializedName("safe_products") val safeCount: Int,
    @SerializedName("unsafe_products") val unsafeCount: Int,
    @SerializedName("safe_percentage") val safePercentage: Double,
    @SerializedName("unsafe_percentage") val unsafePercentage: Double,
    @SerializedName("category_breakdown") val categoryBreakdown: Any? // bisa diganti ke model jika tersedia
)

data class ProductSafetyStatsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ProductSafetyStatsDto
)
