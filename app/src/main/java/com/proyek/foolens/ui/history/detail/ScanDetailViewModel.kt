package com.proyek.foolens.ui.history.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.ImageUtils
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.ProductUseCase
import com.proyek.foolens.domain.usecases.ScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    private val scanHistoryUseCase: ScanHistoryUseCase,
    private val productUseCase: ProductUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanDetailState())
    val state: StateFlow<ScanDetailState> = _state.asStateFlow()

    private val TAG = "ScanDetailViewModel"

    fun loadScanDetails(scanId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                scanHistoryUseCase.getScanHistory().collect { historyResult ->
                    when (historyResult) {
                        is NetworkResult.Success -> {
                            val scanHistory = historyResult.data.find { it.id == scanId }
                            if (scanHistory != null) {
                                // Adjust imageUrl dari scanHistory.product
                                val adjustedScanProduct = scanHistory.product?.let { product ->
                                    val adjustedImageUrl = ImageUtils.getFullImageUrl(product.imageUrl)
                                    Log.d(TAG, "Adjusted scanHistory product imageUrl: $adjustedImageUrl")
                                    product.copy(imageUrl = adjustedImageUrl)
                                }
                                val adjustedScanHistory = scanHistory.copy(product = adjustedScanProduct)

                                val barcode = adjustedScanHistory.product?.barcode
                                if (barcode != null && adjustedScanHistory.product != null) {
                                    productUseCase.scanProductBarcode(barcode, adjustedScanHistory.product).collect { productResult ->
                                        when (productResult) {
                                            is NetworkResult.Success -> {
                                                val productScanResult = productResult.data
                                                val product = productScanResult.product?.let { prod ->
                                                    val adjustedImageUrl = ImageUtils.getFullImageUrl(prod.imageUrl)
                                                    Log.d(TAG, "Adjusted product imageUrl: $adjustedImageUrl")
                                                    prod.copy(imageUrl = adjustedImageUrl)
                                                }
                                                Log.d(TAG, "Product loaded - imageUrl: ${product?.imageUrl}")
                                                _state.update {
                                                    it.copy(
                                                        isLoading = false,
                                                        scanHistory = adjustedScanHistory,
                                                        product = product ?: adjustedScanHistory.product,
                                                        scannedBarcode = productScanResult.scannedBarcode,
                                                        detectedAllergens = productScanResult.detectedAllergens,
                                                        unsafeAllergens = adjustedScanHistory.unsafeAllergens ?: emptyList(),
                                                        isSafe = !productScanResult.hasAllergens,
                                                        errorMessage = null
                                                    )
                                                }
                                            }
                                            is NetworkResult.Error -> {
                                                Log.e(TAG, "Error loading product: ${productResult.errorMessage}")
                                                _state.update {
                                                    it.copy(
                                                        isLoading = false,
                                                        scanHistory = adjustedScanHistory,
                                                        product = adjustedScanHistory.product,
                                                        scannedBarcode = barcode,
                                                        detectedAllergens = emptyList(),
                                                        unsafeAllergens = adjustedScanHistory.unsafeAllergens ?: emptyList(),
                                                        isSafe = adjustedScanHistory.isSafe,
                                                        errorMessage = "Gagal mengambil data alergen: ${productResult.errorMessage}"
                                                    )
                                                }
                                            }
                                            is NetworkResult.Loading -> {}
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Barcode or product is null for scanId: $scanId")
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            scanHistory = adjustedScanHistory,
                                            product = adjustedScanHistory.product,
                                            scannedBarcode = null,
                                            detectedAllergens = emptyList(),
                                            unsafeAllergens = adjustedScanHistory.unsafeAllergens ?: emptyList(),
                                            isSafe = adjustedScanHistory.isSafe,
                                            errorMessage = if (adjustedScanHistory.product == null) "Data produk tidak tersedia" else null
                                        )
                                    }
                                }
                            } else {
                                Log.e(TAG, "Scan history not found for scanId: $scanId")
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Riwayat scan tidak ditemukan"
                                    )
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Error loading scan history: ${historyResult.errorMessage}")
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = historyResult.errorMessage ?: "Gagal mengambil detail"
                                )
                            }
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading scan details: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            scanHistoryUseCase.deleteScan(scanId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update { it.copy(deleteSuccess = true) }
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                errorMessage = result.errorMessage ?: "Gagal menghapus riwayat"
                            )
                        }
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }
}