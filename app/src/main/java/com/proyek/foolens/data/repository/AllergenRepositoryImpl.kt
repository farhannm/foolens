package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.ErrorResponse
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.AllergenDetectionResult
import com.proyek.foolens.domain.repository.AllergenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllergenRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AllergenRepository {

    private val TAG = "AllergenRepositoryImpl"

    override suspend fun detectAllergens(ocrText: String): Flow<NetworkResult<AllergenDetectionResult>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Memulai deteksi alergen dengan teks OCR: ${ocrText.take(50)}...")

            val response = apiService.detectAllergens(mapOf(
                "ocr_text" to ocrText
            ))

            if (response.isSuccessful) {
                val detectionResponse = response.body()
                Log.d(TAG, "Deteksi alergen berhasil, hasil: $detectionResponse")

                if (detectionResponse != null) {
                    try {
                        val detectionResult = DataMapper.mapAllergenDetectionResponseToDomain(detectionResponse)
                        emit(NetworkResult.Success(detectionResult))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping detection response: ${e.message}", e)

                        // Fallback: Buat deteksi lokal jika mapping gagal
                        val fallbackResult = createFallbackDetectionResult(ocrText, detectionResponse)
                        emit(NetworkResult.Success(fallbackResult))
                    }
                } else {
                    Log.e(TAG, "Respon deteksi alergen kosong")
                    emit(NetworkResult.Error("Respon deteksi alergen kosong"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Deteksi alergen gagal dengan kode: ${response.code()}, error body: $errorBody")

                try {
                    if (!errorBody.isNullOrEmpty()) {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        emit(NetworkResult.Error(errorResponse.message ?: "Deteksi alergen gagal"))
                    } else {
                        emit(NetworkResult.Error("Deteksi alergen gagal: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error memproses respon error: ${e.message}")
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception umum: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    /**
     * Membuat hasil deteksi fallback jika terjadi error saat mapping
     */
    private fun createFallbackDetectionResult(
        ocrText: String,
        originalResponse: com.proyek.foolens.data.remote.dto.AllergenDetectionResponse
    ): AllergenDetectionResult {
        // Deteksi dari ID yang kita tahu
        val allergens = originalResponse.detectedAllergens.mapNotNull { dto ->
            try {
                val allergenName = when {
                    !dto.name.isNullOrEmpty() -> dto.name
                    !dto.allergenName.isNullOrEmpty() -> dto.allergenName
                    else -> when (dto.id) {
                        2 -> "Gandum"
                        3 -> "Gluten"
                        8 -> "Telur"
                        19 -> "Kacang Tanah"
                        20 -> "Susu"
                        22 -> "Kedelai"
                        else -> "Alergen #${dto.id}"
                    }
                }

                Allergen(
                    id = dto.id,
                    name = allergenName,
                    severityLevel = dto.severityLevel ?: 2,
                    description = dto.description
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error creating fallback allergen for ID ${dto.id}: ${e.message}")
                null
            }
        }

        return AllergenDetectionResult(
            ocrText = ocrText,
            detectedAllergens = allergens,
            hasAllergens = allergens.isNotEmpty()
        )
    }

    override suspend fun getAllAllergens(): Flow<NetworkResult<List<Allergen>>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Fetching all allergens")
            val response = apiService.getAllAllergens()

            if (response.isSuccessful) {
                val allergenResponse = response.body()

                if (allergenResponse != null) {  // Removed status check
                    val allergens = allergenResponse.allergens.map { dto ->
                        DataMapper.mapAllergenDtoToDomain(dto)
                    }
                    Log.d(TAG, "Successfully fetched ${allergens.size} allergens")
                    emit(NetworkResult.Success(allergens))
                } else {
                    Log.e(TAG, "Failed to get allergens: Response body is null")
                    emit(NetworkResult.Error("Failed to get allergens list"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get allergens list"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}, ${e.code()}", e)
            emit(NetworkResult.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            emit(NetworkResult.Error("Cannot connect to server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("An error occurred: ${e.message}"))
        }
    }

    override suspend fun searchAllergensByName(query: String): Flow<NetworkResult<List<Allergen>>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Searching allergens with query: $query")
            val response = apiService.searchAllergensByName(query)

            if (response.isSuccessful) {
                val allergenResponse = response.body()

                if (allergenResponse != null) {  // Removed status check
                    val allergens = allergenResponse.allergens.map { dto ->
                        DataMapper.mapAllergenDtoToDomain(dto)
                    }
                    Log.d(TAG, "Search returned ${allergens.size} allergens")
                    emit(NetworkResult.Success(allergens))
                } else {
                    Log.e(TAG, "Failed to search allergens: Response body is null")
                    emit(NetworkResult.Error("Failed to search allergens"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to search allergens"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}, ${e.code()}", e)
            emit(NetworkResult.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            emit(NetworkResult.Error("Cannot connect to server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("An error occurred: ${e.message}"))
        }
    }
}