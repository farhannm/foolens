package com.proyek.foolens.domain.usecases

import android.util.Log
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Otp
import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.repository.AuthRepository
import com.proyek.foolens.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * AuthUseCase mengimplementasikan business logic untuk autentikasi.
 * Class ini berada di domain layer dan menggunakan AuthRepository.
 */
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    /**
     * Melakukan login dengan email dan password
     *
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<NetworkResult<User>> hasil login
     */
    suspend fun login(email: String, password: String): Flow<NetworkResult<User>> {
        if (email.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Email tidak boleh kosong"))
            }
        }
        if (password.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Password tidak boleh kosong"))
            }
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return flow {
                emit(NetworkResult.Error("Format email tidak valid"))
            }
        }
        return authRepository.login(email, password)
    }

    /**
     * Melakukan registrasi dengan nama, email, password, dan nomor telepon
     *
     * @param name nama pengguna
     * @param email email pengguna
     * @param password password pengguna
     * @param phoneNumber nomor telepon pengguna
     * @return Flow<NetworkResult<User>> hasil registrasi
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<NetworkResult<User>> {
        if (name.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Nama tidak boleh kosong"))
            }
        }
        if (email.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Email tidak boleh kosong"))
            }
        }
        if (password.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Password tidak boleh kosong"))
            }
        }
        if (phoneNumber.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Nomor telepon tidak boleh kosong"))
            }
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return flow {
                emit(NetworkResult.Error("Format email tidak valid"))
            }
        }
        val phoneRegex = Regex("^(08|\\+62)[0-9]{8,12}$")
        if (!phoneRegex.matches(phoneNumber)) {
            return flow {
                emit(NetworkResult.Error("Format nomor telepon tidak valid"))
            }
        }
        if (password.length < 6) {
            return flow {
                emit(NetworkResult.Error("Password minimal 6 karakter"))
            }
        }
        return authRepository.register(name, email, password, phoneNumber)
    }

    /**
     * Melakukan logout
     */
    suspend fun logout(): Flow<NetworkResult<Unit>> {
        return authRepository.logout()
    }

    /**
     * Mengecek status login
     *
     * @return Flow<NetworkResult<Boolean>> status login
     */
    fun isLoggedIn(): Flow<NetworkResult<Boolean>> {
        Log.d("AuthUseCase", "Checking isLoggedIn, TokenManager hasToken: ${tokenManager.hasToken()}")
        val hasToken = tokenManager.hasToken()
        if (hasToken) {
            Log.d("AuthUseCase", "Token exists, returning true directly")
            return flow { emit(NetworkResult.Success(true)) }
        }
        return authRepository.isLoggedIn()
    }

    /**
     * Mendapatkan data user yang sedang login dari API
     *
     * @return Flow<NetworkResult<User>> data user terbaru atau error
     */
    fun getCurrentUser(): Flow<NetworkResult<User>> {
        return authRepository.getCurrentUser()
    }

    /**
     * Mengirim OTP ke email pengguna
     *
     * @param email email pengguna
     * @return Flow<NetworkResult<Otp>> hasil pengiriman OTP
     */
    suspend fun sendOtp(email: String): Flow<NetworkResult<Otp>> {
        if (email.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Email tidak boleh kosong"))
            }
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return flow {
                emit(NetworkResult.Error("Format email tidak valid"))
            }
        }
        return authRepository.sendOtp(email)
    }

    /**
     * Memverifikasi OTP yang dikirim ke email pengguna
     *
     * @param email email pengguna
     * @param otp kode OTP yang dimasukkan
     * @return Flow<NetworkResult<Otp>> hasil verifikasi OTP
     */
    suspend fun verifyOtp(email: String, otp: String): Flow<NetworkResult<Otp>> {
        if (email.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Email tidak boleh kosong"))
            }
        }
        if (otp.isBlank()) {
            return flow {
                emit(NetworkResult.Error("OTP tidak boleh kosong"))
            }
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return flow {
                emit(NetworkResult.Error("Format email tidak valid"))
            }
        }
        val otpRegex = Regex("^[0-9]{6}$")
        if (!otpRegex.matches(otp)) {
            return flow {
                emit(NetworkResult.Error("OTP harus terdiri dari 6 digit angka"))
            }
        }
        return authRepository.verifyOtp(email, otp)
    }

    /**
     * Mengubah password pengguna setelah verifikasi OTP
     *
     * @param email email pengguna
     * @param resetToken token reset dari verifikasi OTP
     * @param newPassword password baru
     * @return Flow<NetworkResult<Unit>> hasil perubahan password
     */
    suspend fun resetPassword(email: String, newPassword: String): Flow<NetworkResult<Unit>> {
        if (email.isBlank()) {
            return flow {
                emit(NetworkResult.Error("Email tidak boleh kosong"))
            }
        }
        if (newPassword.length < 6) {
            return flow {
                emit(NetworkResult.Error("Kata sandi harus minimal 6 karakter"))
            }
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return flow {
                emit(NetworkResult.Error("Format email tidak valid"))
            }
        }
        return authRepository.resetPassword(email, newPassword)
    }
}