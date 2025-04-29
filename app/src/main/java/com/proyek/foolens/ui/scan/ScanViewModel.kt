package com.proyek.foolens.ui.scan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.AllergenUseCase
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
    private val allergenUseCase: AllergenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val TAG = "ScanViewModel"

    // To prevent multiple API calls in quick succession
    private val isProcessingRequest = AtomicBoolean(false)

    // Keep track of last processed text to avoid repeated processing
    private var lastProcessedText = ""
    private var lastApiRequestTime = 0L
    private val apiThrottleTime = 3000L // 3 seconds between API calls

    // Cache detected allergens to reduce repeated API calls
    private val recentAllergenDetections = mutableMapOf<String, List<com.proyek.foolens.domain.model.Allergen>>()

    /**
     * Mendeteksi alergen dari teks OCR menggunakan API
     * Falls back to offline detection if the API call fails
     */
    fun detectAllergens(ocrText: String) {
        // Skip jika scanning di-pause sementara (karena dialog muncul)
        if (_state.value.temporaryPauseScan) {
            Log.d(TAG, "Skipping allergen detection because scanning is temporarily paused")
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
                        temporaryPauseScan = true // Hentikan scanning sementara ketika dialog muncul
                    )
                }
                return
            } else {
                // Jika cache menunjukkan tidak ada allergen
                _state.update {
                    it.copy(
                        detectedAllergens = emptyList(),
                        hasAllergens = false,
                        showAllergenAlert = false,
                        showSafeProductAlert = true,
                        temporaryPauseScan = true // Hentikan scanning sementara ketika dialog muncul
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
                                    showSafeProductAlert = !allergenResult.hasAllergens, // Tampilkan dialog aman jika tidak ada allergen
                                    errorMessage = null,
                                    temporaryPauseScan = true // Hentikan scanning sementara ketika dialog muncul
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Error API detection: ${result.errorMessage}, falling back to offline mode")

                            // API failed, try offline detection instead
                            isProcessingRequest.set(false) // Reset for offline detection
                            detectAllergensOffline(ocrText)
                        }
                        is NetworkResult.Loading -> {
                            _state.update { it.copy(isProcessing = true) }
                        }
                    }

                    // Clear processing flag
                    isProcessingRequest.set(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in detectAllergens: ${e.message}, falling back to offline")

                // Exception occurred, try offline detection
                isProcessingRequest.set(false) // Reset for offline detection
                detectAllergensOffline(ocrText)
            }
        }
    }

    fun detectAllergensOffline(ocrText: String) {
        // Skip jika scanning di-pause sementara (karena dialog muncul)
        if (_state.value.temporaryPauseScan) {
            Log.d(TAG, "Skipping offline allergen detection because scanning is temporarily paused")
            return
        }

        // Skip if already processing or text is very similar to recent processed text
        val currentTime = System.currentTimeMillis()
        if (isProcessingRequest.get() ||
            (currentTime - lastApiRequestTime < apiThrottleTime) ||
            textIsTooSimilar(ocrText, lastProcessedText)) {
            return
        }

        // Set processing flag and update time
        isProcessingRequest.set(true)
        lastApiRequestTime = currentTime
        lastProcessedText = ocrText

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }

            // Simulasi delay untuk memastikan UI tidak terlalu cepat berubah
            delay(800)

            try {
                // List alergen umum untuk deteksi offline
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

                val detectedAllergens = mutableListOf<com.proyek.foolens.domain.model.Allergen>()
                val lowerCaseText = ocrText.lowercase()

                // Deteksi alergen
                offlineAllergens.forEach { (keyword, allergenInfo) ->
                    // Gunakan regex dengan word boundary untuk keakuratan
                    val regex = "\\b$keyword\\b".toRegex()
                    if (regex.containsMatchIn(lowerCaseText)) {
                        // Tambahkan ke list deteksi jika belum ada
                        val (name, severity, alternativeNames) = allergenInfo
                        if (detectedAllergens.none { it.name == name }) {
                            detectedAllergens.add(
                                com.proyek.foolens.domain.model.Allergen(
                                    id = detectedAllergens.size + 1,
                                    name = name,
                                    severityLevel = severity,
                                    description = "Terdeteksi dalam teks OCR",
                                    alternativeNames = if(alternativeNames.isNotEmpty()) alternativeNames else null
                                )
                            )
                        }
                    }
                }

                // Update cache
                val cacheKey = generateCacheKey(ocrText)
                recentAllergenDetections[cacheKey] = detectedAllergens

                _state.update {
                    it.copy(
                        isProcessing = false,
                        detectedAllergens = detectedAllergens,
                        hasAllergens = detectedAllergens.isNotEmpty(),
                        showAllergenAlert = detectedAllergens.isNotEmpty(),
                        showSafeProductAlert = detectedAllergens.isEmpty(), // Tampilkan dialog aman jika tidak ada allergen
                        errorMessage = null,
                        temporaryPauseScan = true // Hentikan scanning sementara ketika dialog muncul
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

            // Clear processing flag
            isProcessingRequest.set(false)
        }
    }

    /**
     * Generate a simple cache key for similar text detection
     */
    private fun generateCacheKey(text: String): String {
        // Simple implementation: lowercase and trim the text, get first 100 chars
        return text.lowercase().trim().take(100)
    }

    /**
     * Check if two text strings are very similar to avoid repeated processing
     */
    private fun textIsTooSimilar(text1: String, text2: String): Boolean {
        // If either is empty, they're not similar
        if (text1.isEmpty() || text2.isEmpty()) return false

        // Simple check: if they share a significant substring
        val minLength = minOf(text1.length, text2.length)
        val checkLength = minOf(minLength, 50) // Check up to 50 chars

        val sample1 = text1.lowercase().trim().take(checkLength)
        val sample2 = text2.lowercase().trim().take(checkLength)

        // Check for at least 70% similarity
        return sample1.contains(sample2) || sample2.contains(sample1)
    }

    /**
     * Memulai sesi scan kamera
     */
    fun startScanning() {
        _state.update { it.copy(isScanning = true, temporaryPauseScan = false) }
    }

    /**
     * Menghentikan sesi scan kamera
     */
    fun stopScanning() {
        _state.update { it.copy(isScanning = false) }
    }

    /**
     * Menutup alert alergen dan melanjutkan scanning
     */
    fun dismissAllergenAlert() {
        _state.update { it.copy(showAllergenAlert = false, temporaryPauseScan = false) }
    }

    /**
     * Menutup alert produk aman dan melanjutkan scanning
     */
    fun dismissSafeProductAlert() {
        _state.update { it.copy(showSafeProductAlert = false, temporaryPauseScan = false) }
    }

    /**
     * Pause pemindaian sementara
     */
    fun pauseScanning() {
        _state.update { it.copy(temporaryPauseScan = true) }
    }

    /**
     * Lanjutkan pemindaian setelah di-pause
     */
    fun resumeScanning() {
        _state.update { it.copy(temporaryPauseScan = false) }
    }

    /**
     * Reset state aplikasi
     */
    fun resetState() {
        _state.update {
            ScanState(
                isScanning = it.isScanning,
                temporaryPauseScan = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clear any resources
        recentAllergenDetections.clear()
    }
}