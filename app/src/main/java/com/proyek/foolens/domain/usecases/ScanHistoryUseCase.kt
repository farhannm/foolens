package com.proyek.foolens.domain.usecases

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ScanCount
import com.proyek.foolens.domain.model.ScanHistory
import com.proyek.foolens.domain.repository.ScanHistoryRepository
import com.proyek.foolens.util.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {
    suspend fun saveScan(barcode: String): Flow<NetworkResult<ScanHistory>> {
        return scanHistoryRepository.saveScan(barcode)
    }

    suspend fun deleteScan(scanId: String): Flow<NetworkResult<Unit>> {
        return scanHistoryRepository.deleteScan(scanId)
    }

    suspend fun getScanHistory(
        limit: Int = Constants.DEFAULT_PAGE_SIZE,
        page: Int = Constants.DEFAULT_PAGE_NUMBER,
        safetyFilter: String? = null
    ): Flow<NetworkResult<List<ScanHistory>>> {
        return scanHistoryRepository.getScanHistory(limit, page, safetyFilter)
    }

    suspend fun getScanCount(): Flow<NetworkResult<ScanCount>> {
        return scanHistoryRepository.getScanCount()
    }

    suspend fun getProductSafetyStats(userId: String): Flow<NetworkResult<ProductSafetyStats>> {
        return scanHistoryRepository.getProductSafetyStats(userId)
    }

}