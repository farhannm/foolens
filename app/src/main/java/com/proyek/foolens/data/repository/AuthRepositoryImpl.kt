package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.ErrorResponse
import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.User
import com.proyek.foolens.domain.repository.AuthRepository
import com.proyek.foolens.ui.auth.login.LoginState
import com.proyek.foolens.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    private var currentUser: User? = null
    private val TAG = "AuthRepositoryImpl"

    override suspend fun login(email: String, password: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d("AuthRepo", "Memulai request login untuk email: $email")

            val response = apiService.login(mapOf(
                "email" to email,
                "password" to password
            ))

            Log.d("AuthRepo", "Response login diterima: code=${response.code()}")

            if (response.isSuccessful) {
                val userDto = response.body()
                Log.d("AuthRepo", "Login sukses, body: $userDto")

                if (userDto != null && userDto.status == "success") {
                    // Extract token from response
                    val token = userDto.data.token
                    Log.d("AuthRepo", "Login token diterima: $token")

                    if (token != null) {
                        // Save token to TokenManager
                        tokenManager.saveToken(token)

                        val user = User(
                            id = userDto.data.userId?.toString() ?: "",
                            name = userDto.data.name,
                            email = userDto.data.email,
                            phone = userDto.data.phoneNumber ?: "",
                            profilePicture = userDto.data.profilePicture,
                            token = token
                        )

                        currentUser = user
                        emit(NetworkResult.Success(user))
                    } else {
                        Log.e("AuthRepo", "Token tidak ditemukan dalam respons")
                        emit(NetworkResult.Error("Token tidak ditemukan dalam respons"))
                    }
                } else {
                    val errorMessage = userDto?.message ?: "Login gagal"
                    Log.e("AuthRepo", "Login gagal dengan pesan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthRepo", "Login gagal dengan code: ${response.code()}, error body: $errorBody")

                    // Cek apakah error body valid dan bukan HTML
                    if (!errorBody.isNullOrEmpty() && errorBody.startsWith("{")) {
                        try {
                            val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)

                            // Menangani error berdasarkan error_code
                            val errorMessage = when (errorJson.error_code) {
                                "EMAIL_NOT_FOUND" -> "Email tidak terdaftar"
                                "INVALID_PASSWORD" -> "Password yang Anda masukkan salah"
                                else -> errorJson.message ?: "Login gagal: ${response.message()}"
                            }

                            // Menentukan field yang error
                            val fieldError = when (errorJson.error_code) {
                                "EMAIL_NOT_FOUND" -> LoginState.Field.EMAIL
                                "INVALID_PASSWORD" -> LoginState.Field.PASSWORD
                                else -> null
                            }

                            Log.d("AuthRepo", "Error message: $errorMessage, field error: $fieldError")
                            emit(NetworkResult.Error(errorMessage, fieldError))
                        } catch (e: JsonSyntaxException) {
                            Log.e("AuthRepo", "Error parsing JSON response: ${e.message}")
                            emit(NetworkResult.Error("Format respons tidak valid: ${e.message}"))
                        }
                    } else {
                        // Jika errorBody tidak valid atau berisi HTML
                        Log.e("AuthRepo", "Server mengembalikan respons non-JSON (mungkin HTML)")

                        // Fallback untuk kode error yang umum
                        val errorMessage = when (response.code()) {
                            401 -> "Email atau password salah"
                            422 -> "Data yang dimasukkan tidak valid"
                            404 -> "Endpoint API tidak ditemukan. Harap hubungi administrator."
                            500 -> "Terjadi kesalahan di server. Harap coba lagi nanti."
                            else -> "Login gagal dengan kode: ${response.code()}"
                        }
                        emit(NetworkResult.Error(errorMessage))
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepo", "Error saat memproses respons error: ${e.message}", e)
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e("AuthRepo", "SocketTimeoutException: ${e.message}", e)
            emit(NetworkResult.Error("Waktu koneksi habis. Silakan coba lagi."))
        } catch (e: UnknownHostException) {
            Log.e("AuthRepo", "UnknownHostException: ${e.message}", e)
            emit(NetworkResult.Error("Server tidak dapat ditemukan. Periksa koneksi internet Anda."))
        } catch (e: ConnectException) {
            Log.e("AuthRepo", "ConnectException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: JsonSyntaxException) {
            Log.e("AuthRepo", "JsonSyntaxException: ${e.message}", e)
            emit(NetworkResult.Error("Server mengembalikan data yang tidak valid. Harap hubungi administrator."))
        } catch (e: HttpException) {
            Log.e("AuthRepo", "HttpException: ${e.message()}, code: ${e.code()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("AuthRepo", "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e("AuthRepo", "Exception umum: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)

        try {
            val registerRequest = mapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "phone_number" to phoneNumber
            )

            val response = apiService.register(registerRequest)

            println("Register response code: ${response.code()}")

            if (response.isSuccessful) {
                val userDto = response.body()
                println("Register response: $userDto")

                if (userDto != null && userDto.status == "success") {
                    // Extract token from response
                    val token = userDto.data.token
                    println("Register token received: $token")

                    if (token != null) {
                        // Save token to TokenManager
                        tokenManager.saveToken(token)

                        val user = User(
                            id = userDto.data.userId?.toString() ?: "",
                            name = userDto.data.name,
                            email = userDto.data.email,
                            phone = phoneNumber,
                            profilePicture = null,
                            token = token
                        )

                        currentUser = user
                        emit(NetworkResult.Success(user))
                    } else {
                        // Jika token null tapi registrasi berhasil, autentikasi manual
                        login(email, password).collect { result ->
                            emit(result)
                        }
                    }
                } else {
                    val errorMessage = userDto?.message ?: "Registrasi gagal"
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("Register error body: $errorBody")

                val errorMessage = when {
                    response.code() == 409 -> "Email sudah terdaftar"
                    errorBody?.contains("email") == true -> "Format email tidak valid"
                    errorBody?.contains("password") == true -> "Password minimal 6 karakter"
                    else -> "Registrasi gagal: ${response.message()}"
                }
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            println("Register HTTP exception: ${e.message()}")
            emit(NetworkResult.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            println("Register IO exception: ${e.message}")
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            println("Register general exception: ${e.message}")
            emit(NetworkResult.Error("Error: ${e.message}"))
        }
    }

    override suspend fun logout(): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)

        try {
            if (tokenManager.hasToken()) {
                val response = apiService.logout()

                if (response.isSuccessful) {
                    // Clear token from TokenManager
                    tokenManager.clearToken()
                    currentUser = null
                    emit(NetworkResult.Success(Unit))
                } else {
                    emit(NetworkResult.Error("Logout gagal: ${response.message()}"))
                }
            } else {
                // If no token, just clear state
                tokenManager.clearToken()
                currentUser = null
                emit(NetworkResult.Success(Unit))
            }
        } catch (e: Exception) {
            // Even if API fails, clear local state
            tokenManager.clearToken()
            currentUser = null
            emit(NetworkResult.Error("Logout error: ${e.message}"))
        }
    }

    override fun isLoggedIn(): Flow<NetworkResult<Boolean>> = flow {
        // Untuk debugging
        val hasToken = tokenManager.hasToken()
        println("isLoggedIn check: hasToken = $hasToken")

        emit(NetworkResult.Success(hasToken))
    }

    override fun getCurrentUser(): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)

        try {
            if (!tokenManager.hasToken()) {
                emit(NetworkResult.Error("User tidak terautentikasi"))
                return@flow
            }

            val response = apiService.getUserProfile()
            println("GetUserProfile response code: ${response.code()}")
            println("Authorization header: Bearer ${tokenManager.getToken()}")

            if (response.isSuccessful) {
                val userDto = response.body()
                println("GetUserProfile response: $userDto")

                if (userDto != null && userDto.status == "success") {
                    val user = User(
                        id = userDto.data.userId?.toString() ?: "",
                        name = userDto.data.name,
                        email = userDto.data.email,
                        phone = userDto.data.phoneNumber ?: "",
                        profilePicture = userDto.data.profilePicture,
                        token = tokenManager.getToken()
                    )
                    currentUser = user
                    emit(NetworkResult.Success(user))
                } else {
                    emit(NetworkResult.Error("Gagal mengambil data pengguna"))
                }
            } else {
                println("GetUserProfile error: ${response.errorBody()?.string()}")
                // If API call fails but we have cached user, return that
                if (currentUser != null) {
                    emit(NetworkResult.Success(currentUser!!))
                } else {
                    emit(NetworkResult.Error("Gagal mengambil data pengguna: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            println("GetUserProfile exception: ${e.message}")
            // If error but we have cached user, return that
            if (currentUser != null) {
                emit(NetworkResult.Success(currentUser!!))
            } else {
                emit(NetworkResult.Error("Error: ${e.message}"))
            }
        }
    }
}