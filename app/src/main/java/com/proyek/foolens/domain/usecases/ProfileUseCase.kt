package com.proyek.foolens.domain.usecases

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Profile
import com.proyek.foolens.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class ProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    /**
     * Mendapatkan data profil pengguna
     *
     * @return Flow<NetworkResult<Profile>> data profil pengguna
     */
    suspend fun getProfile(): Flow<NetworkResult<Profile>> {
        return profileRepository.getProfile()
    }

    /**
     * Memperbarui data profil pengguna
     *
     * @param name Nama pengguna (opsional)
     * @param phoneNumber Nomor telepon (opsional)
     * @param profilePicture File gambar profil (opsional)
     * @return Flow<NetworkResult<Profile>> data profil pengguna terbaru
     */
    suspend fun updateProfile(
        name: String? = null,
        phoneNumber: String? = null,
        profilePicture: File? = null
    ): Flow<NetworkResult<Profile>> {
        return profileRepository.updateProfile(name, phoneNumber, profilePicture)
    }
}