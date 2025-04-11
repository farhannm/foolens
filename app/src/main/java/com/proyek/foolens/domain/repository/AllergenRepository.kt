package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.AllergenDetectionResult
import kotlinx.coroutines.flow.Flow

interface AllergenRepository {
    /**
     * Mendeteksi alergen dari teks hasil OCR
     *
     * @param ocrText Teks hasil OCR dari gambar produk
     * @return Flow<NetworkResult<AllergenDetectionResult>> hasil deteksi alergen
     */
    suspend fun detectAllergens(ocrText: String): Flow<NetworkResult<AllergenDetectionResult>>

    /**
     * Get all available allergens
     *
     * @return Flow<NetworkResult<List<Allergen>>> list of all allergens
     */
    suspend fun getAllAllergens(): Flow<NetworkResult<List<Allergen>>>

    /**
     * Search allergens by name
     *
     * @param query Search query string
     * @return Flow<NetworkResult<List<Allergen>>> list of matching allergens
     */
    suspend fun searchAllergensByName(query: String): Flow<NetworkResult<List<Allergen>>>
}