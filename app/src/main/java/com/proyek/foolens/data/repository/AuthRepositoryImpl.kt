package com.proyek.foolens.data.repository

import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val response = apiService.login(mapOf(
                "email" to email,
                "password" to password
            ))

            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    val user = DataMapper.mapUserDtoToDomain(userDto)
                    currentUser = user
                    emit(Result.success(user))
                } ?: emit(Result.failure(Exception("Data tidak ditemukan")))
            } else {
                emit(Result.failure(Exception("Login gagal: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Login gagal: ${e.message}")))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Flow<Result<User>> = flow {
        try {
            val response = apiService.register(mapOf(
                "name" to name,
                "email" to email,
                "password" to password
            ))

            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    val user = DataMapper.mapUserDtoToDomain(userDto)
                    currentUser = user
                    emit(Result.success(user))
                } ?: emit(Result.failure(Exception("Data tidak ditemukan")))
            } else {
                emit(Result.failure(Exception("Registrasi gagal: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Registrasi gagal: ${e.message}")))
        }
    }

    override suspend fun logout() {
        currentUser = null
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        emit(currentUser != null)
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        emit(currentUser)
    }
}