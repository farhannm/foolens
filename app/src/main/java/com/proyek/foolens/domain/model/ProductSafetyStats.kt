package com.proyek.foolens.domain.model

data class ProductSafetyStats(
    val totalCount: Int,
    val safeCount: Int,
    val unsafeCount: Int,
    val safePercentage: Double,
    val unsafePercentage: Double,
    val categoryBreakdown: Any?
)
