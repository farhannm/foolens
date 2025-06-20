package com.proyek.foolens.domain.repository

import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Otp
import com.proyek.foolens.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Melakukan proses login dengan email dan password
     *
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<NetworkResult<User>> hasil login yang berisi data user jika berhasil atau error jika gagal
     */
    suspend fun login(email: String, password: String): Flow<NetworkResult<User>>

    /**
     * Melakukan proses registrasi pengguna baru
     *
     * @param name nama pengguna
     * @param email email pengguna
     * @param password password pengguna
     * @param phoneNumber nomor telepon pengguna
     * @return Flow<NetworkResult<User>> hasil registrasi yang berisi data user jika berhasil atau error jika gagal
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<NetworkResult<User>>

    /**
     * Melakukan proses logout, menghapus data sesi user
     */
    suspend fun logout(): Flow<NetworkResult<Unit>>

    /**
     * Mengecek apakah pengguna sudah login atau belum
     *
     * @return Flow<NetworkResult<Boolean>> true jika sudah login, false jika belum
     */
    fun isLoggedIn(): Flow<NetworkResult<Boolean>>

    /**
     * Mendapatkan data pengguna yang sedang login dari API
     *
     * @return Flow<NetworkResult<User>> data user terbaru dari API jika berhasil atau error jika gagal
     */
    fun getCurrentUser(): Flow<NetworkResult<User>>

    /**
     * Mengirim OTP ke email pengguna
     *
     * @param email email pengguna
     * @return Flow<NetworkResult<OtpResponse>> hasil pengiriman OTP
     */
    suspend fun sendOtp(email: String): Flow<NetworkResult<Otp>>

    /**
     * Memverifikasi OTP yang dikirim ke email pengguna
     *
     * @param email email pengguna
     * @param otp kode OTP yang dimasukkan
     * @return Flow<NetworkResult<OtpResponse>> hasil verifikasi OTP
     */
    suspend fun verifyOtp(email: String, otp: String): Flow<NetworkResult<Otp>>

    /**
     * Mereset password setelah dikirim OTP
     *
     * @param email email pengguna
     * @param password password baru
     * @return Flow<NetworkResult<Unit>>
     */
    suspend fun resetPassword(email: String, newPassword: String): Flow<NetworkResult<Unit>>
}