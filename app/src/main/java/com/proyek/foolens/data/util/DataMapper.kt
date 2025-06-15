package com.proyek.foolens.data.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proyek.foolens.data.remote.dto.*
import com.proyek.foolens.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

object DataMapper {
    private const val TAG = "DataMapper"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val gson = Gson()

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

    fun mapAllergenItemDtoToDomain(dto: AllergenItemDto): Allergen {
        return Allergen(
            id = dto.id.toIntOrNull() ?: 0,
            name = dto.name,
            severityLevel = (dto.confidenceLevel * 3).toInt().coerceIn(1, 3),
            description = null,
            alternativeNames = null
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
                ocrText = response.ocrText,
                detectedAllergens = mappedAllergens,
                hasAllergens = response.hasAllergens
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping allergen detection response: ${e.message}", e)
            return AllergenDetectionResult(
                ocrText = response.ocrText,
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
        val product = response.product?.let { mapProductDtoToDomain(it) }
        val detectedAllergens = response.detectedAllergens?.map { mapAllergenItemDtoToDomain(it) } ?: emptyList()
        return ProductScanResult(
            scannedBarcode = response.scannedBarcode ?: barcode,
            found = response.found,
            product = product,
            detectedAllergens = detectedAllergens,
            hasAllergens = response.hasAllergens ?: detectedAllergens.isNotEmpty()
        )
    }

    fun mapScanHistoryDtoToDomain(dto: ScanHistoryDto): ScanHistory {
        val createdAt = try {
            dateFormat.parse(dto.createdAt) ?: Date()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: ${dto.createdAt}, ${e.message}")
            Date()
        }
        val product = dto.product?.let {
            mapProductDtoToDomain(it)
        } ?: run {
            Log.w(TAG, "Product is null for scan ID: ${dto.id}")
            null
        }
        return ScanHistory(
            id = dto.id.toString(),
            userId = dto.userId.toString(),
            productId = dto.productId.toString(),
            isSafe = dto.isSafe,
            unsafeAllergens = dto.unsafeAllergens ?: emptyList(),
            product = product,
            createdAt = createdAt
        )
    }

    fun mapNutritionalInfoDtoToDomain(dto: NutritionalInfoDto): NutritionalInfo {
        return NutritionalInfo(
            fat = dto.fat,
            carbs = dto.carbs,
            protein = dto.protein,
            calories = dto.calories
        )
    }

    fun mapProductDtoToDomain(dto: ProductDto): Product {
        val nutritionalInfo = when (dto.nutritionalInfoDto) {
            is Map<*, *> -> mapNutritionalInfoDtoToDomain(
                Gson().fromJson(
                    Gson().toJson(dto.nutritionalInfoDto),
                    object : TypeToken<NutritionalInfoDto>() {}.type
                )
            )
            else -> null
        }
        return Product(
            id = dto.id.toString(),
            productName = dto.productName,
            brand = dto.brand,
            barcode = dto.barcode,
            imageUrl = dto.imageUrl,
            ingredients = dto.ingredients,
            categoryId = dto.categoryId,
            nutritionalInfo = nutritionalInfo,
            popularityScore = dto.popularityScore
        )
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

    fun mapVerifyOtpResponseToDomain(response: VerifyOtpResponse): Otp {
        return Otp(
            status = response.status,
            message = response.message ?: "",
            email = response.data?.email ?: "",
            expiresIn = null,
            sendingMethod = null,
            verifiedAt = response.data?.verifiedAt
        )
    }

    fun mapAllergensToNames(allergens: List<Allergen>): List<String> {
        return allergens.map { it.name }
    }
}