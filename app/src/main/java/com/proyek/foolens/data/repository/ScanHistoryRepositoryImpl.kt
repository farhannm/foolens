package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.*
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ScanCount
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

    override suspend fun saveScan(barcode: String): Flow<NetworkResult<ScanHistory>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Saving scan with barcode: $barcode")
            val request = ScanDtoRequest(barcode = barcode)
            val response = apiService.saveScan(mapOf("barcode" to barcode))

            if (response.isSuccessful) {
                val saveResponse = response.body()?.let {
                    SaveScanResponse(
                        status = it["status"] as String,
                        message = it["message"] as String?,
                        data = it["data"] as SaveScanResponseData
                    )
                }
                if (saveResponse?.status == "success" && saveResponse.data.scanHistory != null) {
                    val scanHistory = DataMapper.mapScanHistoryDtoToDomain(saveResponse.data.scanHistory)
                    emit(NetworkResult.Success(scanHistory))
                } else {
                    val errorResponse = response.body()?.let {
                        gson.fromJson(gson.toJson(it), ErrorResponse::class.java)
                    }
                    val errorMessage = errorResponse?.message
                        ?: errorResponse?.errorDetails
                        ?: "Gagal menyimpan riwayat pemindaian"
                    Log.e(TAG, "Failed to save scan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                val errorMessage = errorResponse?.message
                    ?: errorResponse?.errorDetails
                    ?: errorBody
                    ?: "Gagal menyimpan riwayat pemindaian"
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

    override suspend fun deleteScan(scanId: String): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Deleting scan with ID: $scanId")
            val response = apiService.deleteScan(scanId.toInt())

            if (response.isSuccessful) {
                val deleteResponse = response.body()?.let {
                    DeleteScanResponse(status = it["status"] as String, message = it["message"] as String?)
                }
                if (deleteResponse?.status == "success") {
                    emit(NetworkResult.Success(Unit))
                } else {
                    val errorResponse = response.body()?.let {
                        gson.fromJson(gson.toJson(it), ErrorResponse::class.java)
                    }
                    val errorMessage = errorResponse?.message
                        ?: errorResponse?.errorDetails
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
                    ?: "Gagal menghapus riwayat pemindaian"
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

    override suspend fun getScanHistory(
        limit: Int,
        page: Int,
        safetyFilter: String?
    ): Flow<NetworkResult<List<ScanHistory>>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Fetching scan history: limit=$limit, page=$page, safetyFilter=$safetyFilter")
            val response = apiService.getScanHistory(limit, page, safetyFilter)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody["status"] == "success") {
                    val data = responseBody["data"]
                    val scanHistories = if (data is List<*>) {
                        // Handle kasus data adalah list (kosong atau berisi ScanHistoryDto)
                        (data as List<ScanHistoryDto>).map { DataMapper.mapScanHistoryDtoToDomain(it) }
                    } else if (data is Map<*, *>) {
                        // Handle kasus data adalah ScanHistoryListResponseData
                        val listResponseData = gson.fromJson(gson.toJson(data), ScanHistoryListResponseData::class.java)
                        listResponseData.scanHistory.map { DataMapper.mapScanHistoryDtoToDomain(it) }
                    } else {
                        // Data kosong atau format tidak dikenal
                        emptyList()
                    }
                    emit(NetworkResult.Success(scanHistories))
                } else {
                    val errorResponse = response.body()?.let {
                        gson.fromJson(gson.toJson(it), ErrorResponse::class.java)
                    }
                    val errorMessage = errorResponse?.message
                        ?: errorResponse?.errorDetails
                        ?: "Gagal mengambil riwayat pemindaian"
                    Log.e(TAG, "Failed to fetch scan history: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                val errorMessage = errorResponse?.message
                    ?: errorResponse?.errorDetails
                    ?: errorBody
                    ?: "Gagal mengambil riwayat pemindaian"
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

    override suspend fun getScanCount(): Flow<NetworkResult<ScanCount>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Fetching scan count statistics")
            val response = apiService.getTodayScanCount() // return Response<ScanCountResponse>

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val dto = ScanCountDto(
                        totalCount = body.data.totalCount,
                        todayCount = body.data.todayCount,
                        safeCount = 0, // fallback default
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