package com.proyek.foolens.domain.usecases

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.AllergenDetectionResult
import com.proyek.foolens.domain.repository.AllergenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AllergenUseCase @Inject constructor(
    private val allergenRepository: AllergenRepository
) {
    /**
     * Mendeteksi alergen dari teks hasil OCR
     *
     * @param ocrText Teks hasil OCR dari gambar produk
     * @return Flow<NetworkResult<AllergenDetectionResult>> hasil deteksi alergen
     */
    suspend fun detectAllergens(ocrText: String): Flow<NetworkResult<AllergenDetectionResult>> {
        // Validasi input
        if (ocrText.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(NetworkResult.Error("Teks OCR tidak boleh kosong"))
            }
        }

        // Forward ke repository
        return allergenRepository.detectAllergens(ocrText)
    }

    /**
     * Get all available allergens
     *
     * @return Flow<NetworkResult<List<Allergen>>> list of all allergens
     */
    suspend fun getAllAllergens(): Flow<NetworkResult<List<Allergen>>> {
        return allergenRepository.getAllAllergens()
    }

    /**
     * Search allergens by name
     *
     * @param query Search query string
     * @return Flow<NetworkResult<List<Allergen>>> list of matching allergens
     */
    suspend fun searchAllergensByName(query: String): Flow<NetworkResult<List<Allergen>>> {
        return allergenRepository.searchAllergensByName(query)
    }
}