package com.proyek.foolens.data.util

import android.util.Log
import com.proyek.foolens.data.remote.dto.AllergenCategoryDto
import com.proyek.foolens.data.remote.dto.AllergenDetectionResponse
import com.proyek.foolens.data.remote.dto.AllergenDto
import com.proyek.foolens.data.remote.dto.ProductSafetyStatsDto
import com.proyek.foolens.data.remote.dto.ProductScanResponse
import com.proyek.foolens.data.remote.dto.ScanCountDto
import com.proyek.foolens.data.remote.dto.ScanHistoryDto
import com.proyek.foolens.data.remote.dto.SendOtpResponse
import com.proyek.foolens.data.remote.dto.UserAllergenDto
import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.data.remote.dto.VerifyOtpResponse
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.AllergenCategory
import com.proyek.foolens.domain.model.AllergenDetectionResult
import com.proyek.foolens.domain.model.Otp
import com.proyek.foolens.domain.model.ProductSafetyStats
import com.proyek.foolens.domain.model.ProductScanResult
import com.proyek.foolens.domain.model.ScanCount
import com.proyek.foolens.domain.model.ScanHistory
import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.model.UserAllergen
import java.text.SimpleDateFormat
import java.util.*

object DataMapper {
    private const val TAG = "DataMapper"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun mapUserDtoToDomain(dto: UserDto): User {
        return User(
            id = dto.data.userId?.toString() ?: "",
            name = dto.data.name,
            email = dto.data.email,
            phone = dto.data.phoneNumber ?: "",
            profilePicture = dto.data.profilePicture ?: "",
            token = dto.data.token ?: ""
        )
    }

    fun mapAllergenDtoToDomain(dto: AllergenDto): Allergen {
        val allergenName = when {
            !dto.name.isNullOrEmpty() -> dto.name
            !dto.allergenName.isNullOrEmpty() -> dto.allergenName
            else -> null
        }
        val finalName = if (allergenName.isNullOrEmpty()) {
            Log.w(TAG, "Null allergen name detected for allergen ID: ${dto.id}, using fallback name")
            when (dto.id) {
                2 -> "Gandum"
                3 -> "Gluten"
                8 -> "Telur"
                19 -> "Kacang Tanah"
                20 -> "Susu"
                22 -> "Kedelai"
                23 -> "Lesitin"
                5 -> "Udang"
                6 -> "Kepiting"
                9 -> "Ikan"
                15 -> "Almond"
                16 -> "Kacang Mete"
                17 -> "Hazelnut"
                else -> "Alergen #${dto.id}"
            }
        } else {
            allergenName
        }
        return Allergen(
            id = dto.id,
            name = finalName,
            severityLevel = dto.severityLevel ?: 0,
            description = dto.description,
            alternativeNames = dto.alternativeNames
        )
    }

    fun mapAllergenDetectionResponseToDomain(response: AllergenDetectionResponse): AllergenDetectionResult {
        Log.d(TAG, "Processing detection response with ${response.detectedAllergens.size} allergens")
        response.detectedAllergens.forEach { allergen ->
            Log.d(TAG, "Processing allergen: ID=${allergen.id}, Name=${allergen.name ?: allergen.allergenName}, Severity=${allergen.severityLevel}")
        }
        try {
            val mappedAllergens = response.detectedAllergens.map { mapAllergenDtoToDomain(it) }
            return AllergenDetectionResult(
                ocrText = response.ocrText ?: "",
                detectedAllergens = mappedAllergens,
                hasAllergens = response.hasAllergens ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping allergen detection response: ${e.message}", e)
            return AllergenDetectionResult(
                ocrText = response.ocrText ?: "",
                detectedAllergens = emptyList(),
                hasAllergens = false
            )
        }
    }

    fun mapUserAllergenDtoToDomain(dto: UserAllergenDto): UserAllergen {
        val category = if (dto.category != null) {
            mapAllergenCategoryDtoToDomain(dto.category)
        } else {
            AllergenCategory(
                id = 0,
                name = "Uncategorized",
                icon = null
            )
        }
        return UserAllergen(
            id = dto.id,
            name = dto.name ?: "Unknown Allergen",
            description = dto.description ?: "",
            alternativeNames = dto.alternativeNames ?: "",
            category = category,
            severityLevel = dto.severityLevel ?: 1,
            notes = dto.notes ?: "",
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun mapAllergenCategoryDtoToDomain(dto: AllergenCategoryDto): AllergenCategory {
        return AllergenCategory(
            id = dto.id,
            name = dto.name ?: "Unknown",
            icon = dto.icon
        )
    }

    fun mapProductScanResponseToDomain(response: ProductScanResponse, barcode: String): ProductScanResult {
        val product = response.product?.toProduct()
        val detectedAllergens = response.detectedAllergens?.map { allergenDto ->
            Allergen(
                id = allergenDto.id.toIntOrNull() ?: 0,
                name = allergenDto.name,
                severityLevel = (allergenDto.confidenceLevel * 3).toInt().coerceIn(1, 3),
                description = null,
                alternativeNames = null
            )
        } ?: emptyList()
        return ProductScanResult(
            scannedBarcode = response.scannedBarcode ?: barcode,
            found = response.found,
            product = product,
            detectedAllergens = detectedAllergens,
            hasAllergens = response.hasAllergens ?: (detectedAllergens.isNotEmpty())
        )
    }

    fun mapScanHistoryDtoToDomain(dto: ScanHistoryDto): ScanHistory {
        Log.d(TAG, "Mapping ScanHistoryDto: id=${dto.id}, barcode=${dto.productId}")
        try {
            return ScanHistory(
                id = dto.id,
                userId = dto.userId,
                productId = dto.productId,
                isSafe = dto.isSafe,
                unsafeAllergens = dto.unsafeAllergens,
                product = dto.product?.toProduct(),
                createdAt = dateFormat.parse(dto.createdAt) ?: Date()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping ScanHistoryDto: ${e.message}", e)
            throw IllegalStateException("Gagal memetakan ScanHistoryDto: ${e.message}")
        }
    }

    fun mapScanCountDtoToDomain(scanCountDto: ScanCountDto): ScanCount {
        return ScanCount(
            totalCount = scanCountDto.totalCount,
            safeCount = scanCountDto.safeCount,
            unsafeCount = scanCountDto.unsafeCount,
            todayCount = scanCountDto.todayCount
        )
    }

    fun mapProductSafetyStatsDtoToDomain(dto: ProductSafetyStatsDto): ProductSafetyStats {
        return ProductSafetyStats(
            totalCount = dto.totalCount,
            safeCount = dto.safeCount,
            unsafeCount = dto.unsafeCount,
            safePercentage = dto.safePercentage,
            unsafePercentage = dto.unsafePercentage,
            categoryBreakdown = dto.categoryBreakdown
        )
    }

    /**
     * Mengkonversi SendOtpResponse ke OtpResponse domain model
     *
     * @param response SendOtpResponse dari API
     * @return OtpResponse domain model
     */
    fun mapSendOtpResponseToDomain(response: SendOtpResponse): Otp {
        return Otp(
            status = response.status,
            message = response.message ?: "",
            email = response.data?.email ?: "",
            expiresIn = response.data?.expiresIn,
            sendingMethod = response.data?.sendingMethod,
            verifiedAt = null
        )
    }

    /**
     * Mengkonversi VerifyOtpResponse ke OtpResponse domain model
     *
     * @param response VerifyOtpResponse dari API
     * @return OtpResponse domain model
     */
    fun mapVerifyOtpResponseToDomain(response: VerifyOtpResponse): Otp {
        return Otp(
            status = response.status,
            message = response.message ?: "",
            email = response.data?.email ?: "",
            expiresIn = null,
            sendingMethod = null,
            verifiedAt = response.data?.verifiedAt,
        )
    }
}