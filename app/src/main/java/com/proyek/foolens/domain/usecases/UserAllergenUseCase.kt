package com.proyek.foolens.domain.usecases

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.domain.repository.AllergenRepository
import com.proyek.foolens.domain.repository.UserAllergenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserAllergenUseCase @Inject constructor(
    private val userAllergenRepository: UserAllergenRepository,
    private val allergenRepository: AllergenRepository
) {

    /**
     * Get all available allergens
     *
     * @return Flow<NetworkResult<List<Allergen>>> list of all allergens
     */
    suspend fun getAllAllergens(): Flow<NetworkResult<List<Allergen>>> {
        return allergenRepository.getAllAllergens()
    }

    /**
     * Search allergens by name or description
     *
     * @param query The search query
     * @return Flow<NetworkResult<List<Allergen>>> list of matching allergens
     */
    suspend fun searchAllergens(query: String): Flow<NetworkResult<List<Allergen>>> {
        return allergenRepository.searchAllergensByName(query)
    }

    /**
     * Mendapatkan daftar alergen yang dimiliki oleh pengguna
     *
     * @param userId ID pengguna
     * @return Flow<NetworkResult<List<UserAllergen>>> daftar alergen pengguna
     */
    suspend fun getUserAllergens(userId: String): Flow<NetworkResult<List<UserAllergen>>> {
        return userAllergenRepository.getUserAllergens(userId)
    }

    /**
     * Menambahkan alergen baru ke pengguna
     *
     * @param userId ID pengguna
     * @param allergenIds List ID alergen
     * @param severityLevels Map ID alergen ke tingkat keparahan
     * @param notes Map ID alergen ke catatan
     * @return Flow<NetworkResult<List<UserAllergen>>> daftar terbaru alergen pengguna
     */
    suspend fun addUserAllergens(
        userId: String,
        allergenIds: List<Int>,
        severityLevels: Map<Int, Int?>,
        notes: Map<Int, String?>
    ): Flow<NetworkResult<List<UserAllergen>>> {
        return userAllergenRepository.addUserAllergens(userId, allergenIds, severityLevels, notes)
    }

    /**
     * Memperbarui data alergen pengguna
     *
     * @param userId ID pengguna
     * @param allergenId ID alergen
     * @param severityLevel Tingkat keparahan (opsional)
     * @param notes Catatan (opsional)
     * @return Flow<NetworkResult<UserAllergen>> data terbaru alergen pengguna
     */
    suspend fun updateUserAllergen(
        userId: String,
        allergenId: Int,
        severityLevel: Int?,
        notes: String?
    ): Flow<NetworkResult<UserAllergen>> {
        return userAllergenRepository.updateUserAllergen(userId, allergenId, severityLevel, notes)
    }

    /**
     * Menghapus alergen dari daftar alergen pengguna
     *
     * @param userId ID pengguna
     * @param allergenId ID alergen
     * @return Flow<NetworkResult<Boolean>> status keberhasilan
     */
    suspend fun deleteUserAllergen(
        userId: String,
        allergenId: Int
    ): Flow<NetworkResult<Boolean>> {
        return userAllergenRepository.deleteUserAllergen(userId, allergenId)
    }
}