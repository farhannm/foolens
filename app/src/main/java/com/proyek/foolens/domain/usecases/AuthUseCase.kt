package com.proyek.foolens.domain.usecases

import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * AuthUseCase mengimplementasikan business logic untuk autentikasi.
 * Class ini berada di domain layer dan menggunakan AuthRepository.
 */
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Melakukan login dengan email dan password
     *
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<Result<User>> hasil login
     */
    suspend fun login(email: String, password: String): Flow<Result<User>> {
        // Validasi input
        if (email.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Email tidak boleh kosong")))
            }
        }

        if (password.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Password tidak boleh kosong")))
            }
        }

        // Forward ke repository setelah validasi
        return authRepository.login(email, password)
    }

    /**
     * Melakukan registrasi dengan nama, email, dan password
     *
     * @param name nama pengguna
     * @param email email pengguna
     * @param password password pengguna
     * @return Flow<Result<User>> hasil registrasi
     */
    suspend fun register(name: String, email: String, password: String): Flow<Result<User>> {
        // Validasi input
        if (name.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Nama tidak boleh kosong")))
            }
        }

        if (email.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Email tidak boleh kosong")))
            }
        }

        if (password.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Password tidak boleh kosong")))
            }
        }

        // Forward ke repository setelah validasi
        return authRepository.register(name, email, password)
    }

    /**
     * Melakukan logout
     */
    suspend fun logout() {
        authRepository.logout()
    }

    /**
     * Mengecek status login
     *
     * @return Flow<Boolean> status login
     */
    fun isLoggedIn(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }

    /**
     * Mendapatkan data user yang sedang login
     *
     * @return Flow<User?> data user atau null
     */
    fun getCurrentUser(): Flow<User?> {
        return authRepository.getCurrentUser()
    }
}