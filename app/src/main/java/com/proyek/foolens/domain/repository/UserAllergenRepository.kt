package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.UserAllergen
import kotlinx.coroutines.flow.Flow

interface UserAllergenRepository {
    /**
     * Mendapatkan daftar alergen yang dimiliki oleh pengguna
     *
     * @param userId ID pengguna
     * @return Flow<NetworkResult<List<UserAllergen>>> daftar alergen pengguna
     */
    suspend fun getUserAllergens(userId: String): Flow<NetworkResult<List<UserAllergen>>>

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
    ): Flow<NetworkResult<List<UserAllergen>>>

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
    ): Flow<NetworkResult<UserAllergen>>

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
    ): Flow<NetworkResult<Boolean>>
}