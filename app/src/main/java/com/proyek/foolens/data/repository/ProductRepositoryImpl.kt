package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.ErrorResponse
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.ProductScanResult
import com.proyek.foolens.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {

    private val TAG = "ProductRepositoryImpl"

    override suspend fun scanProductBarcode(barcode: String): Flow<NetworkResult<ProductScanResult>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Scanning barcode: $barcode")

            val response = apiService.scanProductBarcode(mapOf(
                "barcode" to barcode
            ))

            if (response.isSuccessful) {
                val scanResponse = response.body()
                Log.d(TAG, "Barcode scan successful, result: $scanResponse")

                if (scanResponse != null) {
                    try {
                        // Map the response to domain model
                        val product = scanResponse.product?.toProduct()

                        // Map detected allergens if any
                        val detectedAllergens = scanResponse.detectedAllergens?.map { allergenDto ->
                            Allergen(
                                id = allergenDto.id.toIntOrNull() ?: 0,
                                name = allergenDto.name,
                                severityLevel = (allergenDto.confidenceLevel * 3).toInt().coerceIn(1, 3), // Convert confidence to severity 1-3
                                description = null,
                                alternativeNames = null
                            )
                        } ?: emptyList()

                        val scanResult = ProductScanResult(
                            scannedBarcode = scanResponse.scannedBarcode ?: barcode,
                            found = scanResponse.found,
                            product = product,
                            detectedAllergens = detectedAllergens,
                            hasAllergens = scanResponse.hasAllergens ?: (detectedAllergens.isNotEmpty())
                        )

                        emit(NetworkResult.Success(scanResult))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping scan response: ${e.message}", e)
                        emit(NetworkResult.Error("Error memproses respons: ${e.message}"))
                    }
                } else {
                    Log.e(TAG, "Scan response is empty")
                    emit(NetworkResult.Error("Respons scan barcode kosong"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Barcode scan failed with code: ${response.code()}, error body: $errorBody")

                try {
                    if (!errorBody.isNullOrEmpty()) {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        emit(NetworkResult.Error(errorResponse.message ?: "Scan barcode gagal"))
                    } else {
                        emit(NetworkResult.Error("Scan barcode gagal: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing error response: ${e.message}")
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
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}