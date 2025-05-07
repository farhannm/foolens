package com.proyek.foolens.domain.usecases

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.ProductScanResult
import com.proyek.foolens.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    /**
     * Scans a product barcode and retrieves product information
     *
     * @param barcode The barcode to scan
     * @return Flow<NetworkResult<ProductScanResult>> Product scan result with allergen information
     */
    suspend fun scanProductBarcode(barcode: String): Flow<NetworkResult<ProductScanResult>> {
        // Validate input
        if (barcode.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(NetworkResult.Error("Barcode tidak boleh kosong"))
            }
        }

        // Forward to repository
        return productRepository.scanProductBarcode(barcode)
    }
}