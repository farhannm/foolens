package com.proyek.foolens.data.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proyek.foolens.data.remote.dto.AllergenCategoryDto
import com.proyek.foolens.data.remote.dto.AllergenDetectionResponse
import com.proyek.foolens.data.remote.dto.AllergenDto
import com.proyek.foolens.data.remote.dto.NutritionalInfoDto
import com.proyek.foolens.data.remote.dto.ProductDto
import com.proyek.foolens.data.remote.dto.ProductScanResponse
import com.proyek.foolens.data.remote.dto.ScanHistoryDto
import com.proyek.foolens.data.remote.dto.UserAllergenDto
import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.AllergenCategory
import com.proyek.foolens.domain.model.AllergenDetectionResult
import com.proyek.foolens.domain.model.NutritionalInfo
import com.proyek.foolens.domain.model.Product
import com.proyek.foolens.domain.model.ProductScanResult
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

    /**
     * Mengkonversi UserDto (dari API) ke User domain model
     *
     * @param dto UserDto dari API
     * @return User domain model untuk digunakan di aplikasi
     */
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

    /**
     * Mengkonversi AllergenDto ke Allergen domain model
     *
     * @param dto AllergenDto dari API
     * @return Allergen domain model
     */
    fun mapAllergenDtoToDomain(dto: AllergenDto): Allergen {
        // Coba gunakan name atau allergenName (mana yang tidak null)
        val allergenName = when {
            !dto.name.isNullOrEmpty() -> dto.name
            !dto.allergenName.isNullOrEmpty() -> dto.allergenName
            else -> null // Keduanya null
        }

        // Handle null name dengan memberikan nilai default berdasarkan ID
        val finalName = if (allergenName.isNullOrEmpty()) {
            // Log warning dan coba gunakan nilai fallback berdasarkan ID yang umum
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

    /**
     * Mengkonversi AllergenDetectionResponse ke AllergenDetectionResult domain model
     *
     * @param response AllergenDetectionResponse dari API
     * @return AllergenDetectionResult domain model
     */
    fun mapAllergenDetectionResponseToDomain(response: AllergenDetectionResponse): AllergenDetectionResult {
        // Log untuk debugging
        Log.d(TAG, "Processing detection response with ${response.detectedAllergens.size} allergens")
        response.detectedAllergens.forEach { allergen ->
            Log.d(TAG, "Processing allergen: ID=${allergen.id}, Name=${allergen.name ?: allergen.allergenName}, Severity=${allergen.severityLevel}")
        }

        try {
            val mappedAllergens = response.detectedAllergens.map { mapAllergenDtoToDomain(it) }

            return AllergenDetectionResult(
                ocrText = response.ocrText ?: "",  // Handle null OCR text
                detectedAllergens = mappedAllergens,
                hasAllergens = response.hasAllergens ?: false  // Handle null hasAllergens
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping allergen detection response: ${e.message}", e)
            // Return empty result on error
            return AllergenDetectionResult(
                ocrText = response.ocrText ?: "",
                detectedAllergens = emptyList(),
                hasAllergens = false
            )
        }
    }

    /**
     * Mengkonversi UserAllergenDto ke UserAllergen domain model
     *
     * @param dto UserAllergenDto dari API
     * @return UserAllergen domain model
     */
    fun mapUserAllergenDtoToDomain(dto: UserAllergenDto): UserAllergen {
        // Handle null category
        val category = if (dto.category != null) {
            mapAllergenCategoryDtoToDomain(dto.category)
        } else {
            // Fallback kategori jika null
            AllergenCategory(
                id = 0,
                name = "Uncategorized",
                icon = null
            )
        }

        return UserAllergen(
            id = dto.id,
            name = dto.name ?: "Unknown Allergen", // Provide default for null name
            description = dto.description ?: "",  // Handle null description
            alternativeNames = dto.alternativeNames ?: "",  // Handle null alternativeNames
            category = category,
            severityLevel = dto.severityLevel ?: 1,  // Default to 1 if null
            notes = dto.notes ?: "",  // Handle null notes
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    /**
     * Mengkonversi AllergenCategoryDto ke AllergenCategory domain model
     *
     * @param dto AllergenCategoryDto dari API
     * @return AllergenCategory domain model
     */
    fun mapAllergenCategoryDtoToDomain(dto: AllergenCategoryDto): AllergenCategory {
        return AllergenCategory(
            id = dto.id,
            name = dto.name ?: "Unknown",  // Provide default for null name
            icon = dto.icon
        )
    }

    /**
     * Maps ProductScanResponse to ProductScanResult domain model
     */
    fun mapProductScanResponseToDomain(response: ProductScanResponse, barcode: String): ProductScanResult {
        // Map product if found
        val product = response.product?.let { mapProductDtoToDomain(it) }

        // Map detected allergens
        val detectedAllergens = response.detectedAllergens?.map { allergenDto ->
            Allergen(
                id = allergenDto.id.toIntOrNull() ?: 0,
                name = allergenDto.name,
                severityLevel = (allergenDto.confidenceLevel * 3).toInt().coerceIn(1, 3), // Convert confidence to severity 1-3
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

    /**
     * Mengkonversi ScanHistoryDto ke ScanHistory domain model
     *
     * @param dto ScanHistoryDto dari API
     * @return ScanHistory domain model
     */
    fun mapScanHistoryDtoToDomain(dto: ScanHistoryDto): ScanHistory {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val createdAt = try {
            dateFormat.parse(dto.createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        return ScanHistory(
            id = dto.id.toString(),
            userId = dto.userId.toString(),
            productId = dto.productId.toString(),
            isSafe = dto.isSafe,
            unsafeAllergens = dto.unsafeAllergens ?: emptyList(),
            product = dto.product?.let { mapProductDtoToDomain(it) },
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
}