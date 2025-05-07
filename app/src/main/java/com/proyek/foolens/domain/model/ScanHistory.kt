package com.proyek.foolens.domain.model

import java.util.Date

data class ScanHistory(
    val id: String,
    val userId: String,
    val productId: String,
    val isSafe: Boolean,
    val unsafeAllergens: List<String>?,
    val product: Product?,
    val createdAt: Date
)