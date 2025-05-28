package com.proyek.foolens.ui.history.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                                val barcode = scanHistory.product?.barcode
                                if (barcode != null && scanHistory.product != null) {
                                    // Gunakan product dari ScanHistory, tapi cek alergen via API
                                    productUseCase.scanProductBarcode(barcode, scanHistory.product).collect { productResult ->
                                        when (productResult) {
                                            is NetworkResult.Success -> {
                                                _state.update {
                                                    it.copy(
                                                        isLoading = false,
                                                        scanHistory = scanHistory,
                                                        product = scanHistory.product,
                                                        scannedBarcode = barcode,
                                                        detectedAllergens = productResult.data.detectedAllergens,
                                                        unsafeAllergens = scanHistory.unsafeAllergens ?: emptyList(),
                                                        isSafe = scanHistory.isSafe,
                                                        errorMessage = null
                                                    )
                                                }
                                            }
                                            is NetworkResult.Error -> {
                                                // Jika API gagal, gunakan data dari ScanHistory
                                                _state.update {
                                                    it.copy(
                                                        isLoading = false,
                                                        scanHistory = scanHistory,
                                                        product = scanHistory.product,
                                                        scannedBarcode = barcode,
                                                        detectedAllergens = emptyList(),
                                                        unsafeAllergens = scanHistory.unsafeAllergens ?: emptyList(),
                                                        isSafe = scanHistory.isSafe,
                                                        errorMessage = "Gagal mengambil data alergen: ${productResult.errorMessage}"
                                                    )
                                                }
                                            }
                                            is NetworkResult.Loading -> {}
                                        }
                                    }
                                } else {
                                    // Tidak ada barcode atau product
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            scanHistory = scanHistory,
                                            product = scanHistory.product,
                                            scannedBarcode = null,
                                            detectedAllergens = emptyList(),
                                            unsafeAllergens = scanHistory.unsafeAllergens ?: emptyList(),
                                            isSafe = scanHistory.isSafe,
                                            errorMessage = if (scanHistory.product == null) "Data produk tidak tersedia" else null
                                        )
                                    }
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Riwayat scan tidak ditemukan"
                                    )
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Error loading scan details: ${historyResult.errorMessage}")
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
}