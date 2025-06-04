package com.proyek.foolens.ui.home

import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ScanCount
import com.proyek.foolens.domain.model.User

/**
 * State class untuk HomeScreen
 */
data class HomeState(
        val user: User? = null,
        val scanCount: ScanCount? = null,
        val productSafetyStats: ProductSafetyStats? = null,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
)