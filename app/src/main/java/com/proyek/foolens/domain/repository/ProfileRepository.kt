package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.remote.dto.ChangePasswordResponse
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import java.io.File

interface ProfileRepository {
    /**
     * Mendapatkan data profil pengguna
     *
     * @return Flow<NetworkResult<Profile>> data profil pengguna
     */
    suspend fun getProfile(): Flow<NetworkResult<Profile>>

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
    ): Flow<NetworkResult<Profile>>

}