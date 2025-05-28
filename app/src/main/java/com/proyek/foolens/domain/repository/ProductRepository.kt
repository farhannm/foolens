package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Product
import com.proyek.foolens.domain.model.ProductScanResult
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /**
     * Scan a product barcode to retrieve product information and allergen data
     *
     * @param barcode The product barcode to scan
     * @return Flow<NetworkResult<ProductScanResult>> The result of the scan operation
     */
    suspend fun scanProductBarcode(barcode: String?, product: Product?): Flow<NetworkResult<ProductScanResult>>
}