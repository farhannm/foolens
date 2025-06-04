package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.ErrorResponse
import com.proyek.foolens.data.remote.dto.ProductScanResponse
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.ImageUtils
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Product
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
    private val gson = Gson()

    override suspend fun scanProductBarcode(
        barcode: String?,
        product: Product?
    ): Flow<NetworkResult<ProductScanResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            if (product != null) {
                Log.d(TAG, "Using product data from ScanHistory: $product")
                val adjustedImageUrl = ImageUtils.getFullImageUrl(product.imageUrl)
                val adjustedProduct = product.copy(imageUrl = adjustedImageUrl)
                val scanResult = ProductScanResult(
                    scannedBarcode = barcode ?: "Unknown",
                    found = true,
                    product = adjustedProduct,
                    detectedAllergens = emptyList(),
                    hasAllergens = false
                )
                emit(NetworkResult.Success(scanResult))
            } else if (barcode != null) {
                // Panggil API dengan POST /products/scan
                Log.d(TAG, "Scanning product with barcode: $barcode")
                val request = mapOf("barcode" to barcode)
                val response = apiService.scanProductBarcode(request)

                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    Log.d(TAG, "Deserialized response body: ${gson.toJson(scanResponse)}")
                    if (scanResponse?.found == true) {
                        // Gunakan ImageUtils untuk mengonversi imageUrl
                        val adjustedImageUrl = ImageUtils.getFullImageUrl(scanResponse.product?.imageUrl)
                        Log.d(TAG, "Adjusted imageUrl: $adjustedImageUrl")
                        // Buat Product dengan imageUrl yang disesuaikan
                        val adjustedProduct = scanResponse.product?.copy(imageUrl = adjustedImageUrl)
                        val scanResult = DataMapper.mapProductScanResponseToDomain(
                            scanResponse.copy(product = adjustedProduct),
                            barcode
                        )
                        Log.d(TAG, "Mapped ProductScanResult: $scanResult")
                        emit(NetworkResult.Success(scanResult))
                    } else {
                        val errorMessage = scanResponse?.message ?: "Produk tidak ditemukan"
                        Log.w(TAG, "Failed to get product details: $errorMessage")
                        emit(NetworkResult.Error(errorMessage))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()?.trim()
                    Log.w(TAG, "Failed to get product details: HTTP ${response.code()}, $errorBody")
                    try {
                        if (!errorBody.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                            emit(NetworkResult.Error(errorResponse.message ?: "Gagal mengambil detail produk"))
                        } else {
                            emit(NetworkResult.Error("Gagal mengambil detail produk: HTTP ${response.code()}"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing error response: ${e.message}", e)
                        emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                    }
                }
            } else {
                Log.w(TAG, "No barcode or product data available")
                emit(NetworkResult.Error("Tidak ada data produk atau barcode"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}", e)
            if (e.code() == 404) {
                emit(NetworkResult.Error("Produk tidak ditemukan"))
            } else if (e.code() == 401 || e.code() == 403) {
                emit(NetworkResult.Error("Sesi tidak valid. Silakan login kembali."))
            } else {
                emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product scan: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}