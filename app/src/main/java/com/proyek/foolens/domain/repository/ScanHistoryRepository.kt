package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ScanCount
import com.proyek.foolens.domain.model.ScanHistory
import kotlinx.coroutines.flow.Flow
import com.proyek.foolens.util.Constants

interface ScanHistoryRepository {
    suspend fun saveScan(barcode: String): Flow<NetworkResult<ScanHistory>>
    suspend fun deleteScan(scanId: String): Flow<NetworkResult<Unit>>
    suspend fun getScanHistory(
        limit: Int = Constants.DEFAULT_PAGE_SIZE,
        page: Int = Constants.DEFAULT_PAGE_NUMBER,
        safetyFilter: String? = null
    ): Flow<NetworkResult<List<ScanHistory>>>

    suspend fun getScanCount(): Flow<NetworkResult<ScanCount>>

    suspend fun getProductSafetyStats(userId: String): Flow<NetworkResult<ProductSafetyStats>>

}