package com.proyek.foolens.domain.model

data class ScanCount(
    val totalCount: Int,
    val safeCount: Int,
    val unsafeCount: Int,
    val todayCount: Int
)