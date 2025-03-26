package com.proyek.foolens.domain.repository

import com.proyek.foolens.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Melakukan proses login dengan email dan password
     *
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<Result<User>> hasil login yang berisi data user jika berhasil atau error jika gagal
     */
    suspend fun login(email: String, password: String): Flow<Result<User>>

    /**
     * Melakukan proses registrasi pengguna baru
     *
     * @param name nama pengguna
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<Result<User>> hasil registrasi yang berisi data user jika berhasil atau error jika gagal
     */
    suspend fun register(name: String, email: String, password: String): Flow<Result<User>>

    /**
     * Melakukan proses logout, menghapus data sesi user
     */
    suspend fun logout()

    /**
     * Mengecek apakah pengguna sudah login atau belum
     *
     * @return Flow<Boolean> true jika sudah login, false jika belum
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * Mendapatkan data pengguna yang sedang login
     *
     * @return Flow<User?> data user jika ada, null jika tidak ada
     */
    fun getCurrentUser(): Flow<User?>
}