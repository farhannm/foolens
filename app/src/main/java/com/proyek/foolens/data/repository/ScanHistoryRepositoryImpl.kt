package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.*
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ScanCount
import com.proyek.foolens.domain.model.ProductScanResult
import com.proyek.foolens.domain.model.ScanHistory
import com.proyek.foolens.domain.repository.ScanHistoryRepository
import com.proyek.foolens.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanHistoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ScanHistoryRepository {

    private val TAG = "ScanHistoryRepositoryImpl"
    private val gson = Gson()

    override suspend fun saveScan(barcode: String, scanResult: ProductScanResult): Flow<NetworkResult<ScanHistory>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Saving scan with barcode: $barcode")
            val request = ScanDtoRequest(
                barcode = barcode,
                productId = scanResult.product?.id?.toInt() ?: throw IllegalStateException("Product ID not available"),
                isSafe = !scanResult.hasAllergens,
                userId = null,
                unsafeAllergens = DataMapper.mapAllergensToNames(scanResult.detectedAllergens)
            )
            Log.d(TAG, "Request body: ${gson.toJson(request)}")
            val response = apiService.saveScan(request)

            if (response.isSuccessful) {
                val saveResponse = response.body()
                Log.d(TAG, "Deserialized response body: ${gson.toJson(saveResponse)}")
                if (saveResponse?.status == "success" && saveResponse.data != null) {
                    val scanHistory = DataMapper.mapScanHistoryDtoToDomain(saveResponse.data)
                    Log.d(TAG, "Mapped ScanHistory: $scanHistory")
                    emit(NetworkResult.Success(scanHistory))
                } else {
                    val errorMessage = saveResponse?.message ?: "Data riwayat scan tidak tersedia"
                    Log.e(TAG, "Failed to save scan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error: ${response.code()}, $errorBody")
                try {
                    if (!errorBody.isNullOrEmpty()) {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        emit(NetworkResult.Error(errorResponse.message ?: "Gagal menyimpan scan"))
                    } else {
                        emit(NetworkResult.Error("Gagal menyimpan scan: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing error response: ${e.message}")
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}", e)
            if (e.code() == 401 || e.code() == 403) {
                emit(NetworkResult.Error("Sesi tidak valid. Silakan login kembali."))
            } else {
                emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun deleteScan(scanId: String): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Deleting scan with ID: $scanId")
            val response = apiService.deleteScan(scanId.toInt())

            if (response.isSuccessful) {
                val deleteResponse = response.body()
                if (deleteResponse?.status == "success") {
                    emit(NetworkResult.Success(Unit))
                } else {
                    val errorMessage = deleteResponse?.message
                        ?: "Gagal menghapus riwayat pemindaian"
                    Log.e(TAG, "Failed to delete scan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                val errorMessage = errorResponse?.message
                    ?: errorResponse?.errorDetails
                    ?: errorBody
                    ?: "Gagal menghapus riwayat pemindaian (HTTP ${response.code()})"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}, ${e.code()}", e)
            emit(NetworkResult.Error("Error jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun getScanHistory(limit: Int, page: Int, safetyFilter: String?): Flow<NetworkResult<List<ScanHistory>>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Fetching scan history: limit=$limit, page=$page, safetyFilter=$safetyFilter")
            val response = apiService.getScanHistory(limit, page, safetyFilter)
            if (response.isSuccessful) {
                val scanResponse = response.body()
                Log.d(TAG, "Deserialized response body: ${gson.toJson(scanResponse)}")
                if (scanResponse?.status == "success") {
                    val scanHistories = scanResponse.data.map { scanHistoryDto ->
                        DataMapper.mapScanHistoryDtoToDomain(scanHistoryDto)
                    }
                    Log.d(TAG, "Mapped ScanHistories: $scanHistories")
                    emit(NetworkResult.Success(scanHistories))
                } else {
                    val errorMessage = "Gagal mengambil riwayat pemindaian"
                    Log.e(TAG, "Failed to fetch scan history: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error: ${response.code()}, $errorBody")
                try {
                    if (!errorBody.isNullOrEmpty()) {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        emit(NetworkResult.Error(errorResponse.message ?: "Gagal mengambil riwayat pemindaian"))
                    } else {
                        emit(NetworkResult.Error("Gagal mengambil riwayat pemindaian: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing error response: ${e.message}")
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}", e)
            if (e.code() == 401 || e.code() == 403) {
                emit(NetworkResult.Error("Sesi tidak valid. Silakan login kembali."))
            } else {
                emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun getScanCount(): Flow<NetworkResult<ScanCount>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Fetching scan count statistics")
            val response = apiService.getTodayScanCount()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val dto = ScanCountDto(
                        totalCount = body.data.totalCount,
                        todayCount = body.data.todayCount,
                        safeCount = 0,
                        unsafeCount = 0
                    )
                    val domain = DataMapper.mapScanCountDtoToDomain(dto)
                    emit(NetworkResult.Success(domain))
                } else {
                    emit(NetworkResult.Error("Response status bukan success"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                val errorMessage = errorResponse?.message
                    ?: errorResponse?.errorDetails
                    ?: errorBody
                    ?: "Gagal mengambil data statistik pemindaian"
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun getProductSafetyStats(userId: String): Flow<NetworkResult<ProductSafetyStats>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getProductSafetyStats(userId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val domain = DataMapper.mapProductSafetyStatsDtoToDomain(body.data)
                    emit(NetworkResult.Success(domain))
                } else {
                    emit(NetworkResult.Error("Status bukan success atau body null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                val errorMessage = errorResponse?.message
                    ?: errorResponse?.errorDetails
                    ?: errorBody
                    ?: "Gagal mengambil statistik keamanan produk"
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error("HTTP error: ${e.message()}"))
        } catch (e: IOException) {
            emit(NetworkResult.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}