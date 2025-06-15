package com.proyek.foolens.ui.scan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.Product
import com.proyek.foolens.domain.model.ProductScanResult
import com.proyek.foolens.domain.usecases.AllergenUseCase
import com.proyek.foolens.domain.usecases.ProductUseCase
import com.proyek.foolens.domain.usecases.ScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val allergenUseCase: AllergenUseCase,
    private val productUseCase: ProductUseCase,
    private val scanHistoryUseCase: ScanHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val TAG = "ScanViewModel"

    // To prevent multiple API calls in quick succession
    private val isProcessingRequest = AtomicBoolean(false)

    // For OCR scanning
    private var lastProcessedText = ""
    private var lastApiRequestTime = 0L
    private val apiThrottleTime = 3000L // 3 seconds between API calls

    // For barcode scanning
    private var lastProcessedBarcode = ""
    private var lastBarcodeApiRequestTime = 0L

    // Cache detected allergens to reduce repeated API calls
    private val recentAllergenDetections = mutableMapOf<String, List<com.proyek.foolens.domain.model.Allergen>>()

    /**
     * Change the scan mode
     */
    fun setScanMode(mode: ScanMode) {
        _state.update { it.copy(currentScanMode = mode) }
        Log.d(TAG, "Scan mode changed to: $mode")
    }

    /**
     * Mendeteksi alergen dari teks OCR menggunakan API
     * Falls back to offline detection if the API call fails
     */
    fun detectAllergens(ocrText: String) {
        // Skip if scan mode is not allergen or scanning is paused
        if (_state.value.currentScanMode != ScanMode.ALLERGEN || _state.value.temporaryPauseScan) {
            Log.d(TAG, "Skipping allergen detection because wrong mode or scanning is paused")
            return
        }

        // Skip if already processing or text is very similar to recent processed text
        val currentTime = System.currentTimeMillis()
        if (isProcessingRequest.get() ||
            (currentTime - lastApiRequestTime < apiThrottleTime) ||
            textIsTooSimilar(ocrText, lastProcessedText)) {
            return
        }

        // Check cache first
        val cacheKey = generateCacheKey(ocrText)
        if (recentAllergenDetections.containsKey(cacheKey)) {
            Log.d(TAG, "Using cached allergen detection result")
            val cachedAllergens = recentAllergenDetections[cacheKey] ?: emptyList()
            if (cachedAllergens.isNotEmpty()) {
                _state.update {
                    it.copy(
                        detectedAllergens = cachedAllergens,
                        hasAllergens = true,
                        showAllergenAlert = true,
                        showSafeProductAlert = false,
                        temporaryPauseScan = true
                    )
                }
                return
            } else {
                _state.update {
                    it.copy(
                        detectedAllergens = emptyList(),
                        hasAllergens = false,
                        showAllergenAlert = false,
                        showSafeProductAlert = true,
                        temporaryPauseScan = true
                    )
                }
                return
            }
        }

        // Set processing flag and update time
        isProcessingRequest.set(true)
        lastApiRequestTime = currentTime
        lastProcessedText = ocrText

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }

            try {
                // Try online API call first
                allergenUseCase.detectAllergens(ocrText).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            val allergenResult = result.data
                            // Update cache
                            recentAllergenDetections[cacheKey] = allergenResult.detectedAllergens
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    detectedAllergens = allergenResult.detectedAllergens,
                                    hasAllergens = allergenResult.hasAllergens,
                                    showAllergenAlert = allergenResult.hasAllergens,
                                    showSafeProductAlert = !allergenResult.hasAllergens,
                                    errorMessage = null,
                                    temporaryPauseScan = true
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Error API detection: ${result.errorMessage}, falling back to offline mode")
                            isProcessingRequest.set(false)
                            detectAllergensOffline(ocrText)
                        }
                        is NetworkResult.Loading -> {
                            _state.update { it.copy(isProcessing = true) }
                        }
                    }
                    isProcessingRequest.set(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in detectAllergens: ${e.message}, falling back to offline")
                isProcessingRequest.set(false)
                detectAllergensOffline(ocrText)
            }
        }
    }

    fun detectAllergensOffline(ocrText: String) {
        // Skip if scan mode is not allergen or scanning is paused
        if (_state.value.currentScanMode != ScanMode.ALLERGEN || _state.value.temporaryPauseScan) {
            Log.d(TAG, "Skipping offline allergen detection because wrong mode or scanning is paused")
            return
        }

        // Skip if already processing or text is very similar to recent processed text
        val currentTime = System.currentTimeMillis()
        if (isProcessingRequest.get() ||
            (currentTime - lastApiRequestTime < apiThrottleTime) ||
            textIsTooSimilar(ocrText, lastProcessedText)) {
            return
        }

        isProcessingRequest.set(true)
        lastApiRequestTime = currentTime
        lastProcessedText = ocrText

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }
            delay(800)

            try {
                val offlineAllergens = mapOf(
                    "susu" to Triple("Susu", 3, "Milk, Dairy"),
                    "milk" to Triple("Susu", 3, "Milk, Dairy"),
                    "dairy" to Triple("Susu", 3, "Milk, Dairy"),
                    "gandum" to Triple("Gandum", 2, "Wheat"),
                    "wheat" to Triple("Gandum", 2, "Wheat"),
                    "tepung" to Triple("Tepung", 2, "Flour"),
                    "kacang" to Triple("Kacang", 3, "Nuts"),
                    "peanut" to Triple("Kacang Tanah", 3, "Groundnut"),
                    "almond" to Triple("Almond", 2, ""),
                    "telur" to Triple("Telur", 2, "Egg"),
                    "egg" to Triple("Telur", 2, "Egg"),
                    "kedelai" to Triple("Kedelai", 2, "Soy"),
                    "soy" to Triple("Kedelai", 2, "Soy"),
                    "lesitin" to Triple("Lesitin (Kedelai)", 2, "Lecithin"),
                    "lecithin" to Triple("Lesitin (Kedelai)", 2, "Lecithin"),
                    "udang" to Triple("Udang", 3, "Shrimp"),
                    "shrimp" to Triple("Udang", 3, "Shrimp"),
                    "kepiting" to Triple("Kepiting", 3, "Crab"),
                    "crab" to Triple("Kepiting", 3, "Crab")
                )

                val detectedAllergens = mutableListOf<Allergen>()
                val lowerCaseText = ocrText.lowercase()

                offlineAllergens.forEach { (keyword, allergenInfo) ->
                    val regex = "\\b$keyword\\b".toRegex()
                    if (regex.containsMatchIn(lowerCaseText)) {
                        val (name, severity, alternativeNames) = allergenInfo
                        if (detectedAllergens.none { it.name == name }) {
                            detectedAllergens.add(
                                Allergen(
                                    id = detectedAllergens.size + 1,
                                    name = name,
                                    severityLevel = severity,
                                    description = "Terdeteksi dalam teks OCR",
                                    alternativeNames = if (alternativeNames.isNotEmpty()) alternativeNames else null
                                )
                            )
                        }
                    }
                }

                val cacheKey = generateCacheKey(ocrText)
                recentAllergenDetections[cacheKey] = detectedAllergens

                _state.update {
                    it.copy(
                        isProcessing = false,
                        detectedAllergens = detectedAllergens,
                        hasAllergens = detectedAllergens.isNotEmpty(),
                        showAllergenAlert = detectedAllergens.isNotEmpty(),
                        showSafeProductAlert = detectedAllergens.isEmpty(),
                        errorMessage = null,
                        temporaryPauseScan = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error offline detection: ${e.message}")
                _state.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Error deteksi offline: ${e.message}"
                    )
                }
            }
            isProcessingRequest.set(false)
        }
    }

    /**
     * Scan product barcode and get product information
     */
    fun scanProductBarcode(barcode: String) {
        if (_state.value.currentScanMode != ScanMode.BARCODE || _state.value.temporaryPauseScan) {
            Log.d(TAG, "Skipping barcode scan because wrong mode or scanning is paused")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (isProcessingRequest.get() ||
            (currentTime - lastBarcodeApiRequestTime < apiThrottleTime) ||
            barcode == lastProcessedBarcode) {
            return
        }

        isProcessingRequest.set(true)
        lastBarcodeApiRequestTime = currentTime
        lastProcessedBarcode = barcode

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }

            try {
                productUseCase.scanProductBarcode(barcode).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            val scanResult = result.data
                            saveScanToHistory(barcode, scanResult)
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    product = scanResult.product,
                                    scannedBarcode = scanResult.scannedBarcode,
                                    productFound = scanResult.found,
                                    detectedAllergens = scanResult.detectedAllergens,
                                    hasAllergens = scanResult.hasAllergens,
                                    showProductFoundDialog = scanResult.found,
                                    showProductNotFoundDialog = !scanResult.found,
                                    errorMessage = null,
                                    temporaryPauseScan = true
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Error scanning barcode: ${result.errorMessage}")
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    errorMessage = result.errorMessage,
                                    showProductNotFoundDialog = true,
                                    temporaryPauseScan = true
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            _state.update { it.copy(isProcessing = true) }
                        }
                    }
                    isProcessingRequest.set(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception scanning barcode: ${e.message}")
                _state.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Error: ${e.message}",
                        showProductNotFoundDialog = true,
                        temporaryPauseScan = true
                    )
                }
                isProcessingRequest.set(false)
            }
        }
    }

    private suspend fun saveScanToHistory(barcode: String, scanResult: ProductScanResult) {
        scanHistoryUseCase.saveScan(barcode, scanResult).collect { saveResult ->
            when (saveResult) {
                is NetworkResult.Success -> {
                    val scanHistory = saveResult.data
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            product = scanResult.product,
                            scannedBarcode = scanResult.scannedBarcode,
                            productFound = scanResult.found,
                            detectedAllergens = scanResult.detectedAllergens,
                            hasAllergens = scanResult.hasAllergens,
                            showProductFoundDialog = scanResult.found,
                            showProductNotFoundDialog = !scanResult.found,
                            errorMessage = null,
                            temporaryPauseScan = true,
                            scanHistoryId = scanHistory.id
                        )
                    }
                    Log.d(TAG, "Scan history saved successfully: ${scanHistory.id}, unsafe_allergens: ${scanResult.detectedAllergens.map { it.name }}")
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error menyimpan hasil scan: ${saveResult.errorMessage}")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = saveResult.errorMessage,
                            showProductNotFoundDialog = true,
                            temporaryPauseScan = true
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    _state.update { it.copy(isProcessing = true) }
                }
            }
            isProcessingRequest.set(false)
        }
    }

    private fun generateCacheKey(text: String): String {
        return text.lowercase().trim().take(100)
    }

    private fun textIsTooSimilar(text1: String, text2: String): Boolean {
        if (text1.isEmpty() || text2.isEmpty()) return false
        val minLength = minOf(text1.length, text2.length)
        val checkLength = minOf(minLength, 50)
        val sample1 = text1.lowercase().trim().take(checkLength)
        val sample2 = text2.lowercase().trim().take(checkLength)
        return sample1.contains(sample2) || sample2.contains(sample1)
    }

    fun startScanning() {
        _state.update { it.copy(isScanning = true, temporaryPauseScan = false) }
    }

    fun stopScanning() {
        _state.update { it.copy(isScanning = false) }
    }

    fun dismissAllergenAlert() {
        _state.update { it.copy(showAllergenAlert = false, temporaryPauseScan = false) }
    }

    fun dismissSafeProductAlert() {
        _state.update { it.copy(showSafeProductAlert = false, temporaryPauseScan = false) }
    }

    fun dismissProductFoundDialog() {
        _state.update {
            it.copy(
                showProductFoundDialog = false,
                temporaryPauseScan = false,
                scanHistoryId = null
            )
        }
        lastProcessedBarcode = ""
        lastBarcodeApiRequestTime = 0L
        isProcessingRequest.set(false)
    }

    fun dismissProductNotFoundDialog() {
        _state.update {
            it.copy(
                showProductNotFoundDialog = false,
                temporaryPauseScan = false
            )
        }
        lastProcessedBarcode = ""
        lastBarcodeApiRequestTime = 0L
        isProcessingRequest.set(false)
    }

    fun pauseScanning() {
        _state.update { it.copy(temporaryPauseScan = true) }
    }

    fun resumeScanning() {
        _state.update { it.copy(temporaryPauseScan = false) }
    }

    fun resetState() {
        _state.update {
            ScanState(
                isScanning = it.isScanning,
                temporaryPauseScan = false,
                currentScanMode = it.currentScanMode
            )
        }
        isProcessingRequest.set(false)
        if (_state.value.currentScanMode == ScanMode.ALLERGEN) {
            lastProcessedText = ""
            lastApiRequestTime = 0L
        } else {
            lastProcessedBarcode = ""
            lastBarcodeApiRequestTime = 0L
        }
    }

    override fun onCleared() {
        super.onCleared()
        recentAllergenDetections.clear()
    }
}