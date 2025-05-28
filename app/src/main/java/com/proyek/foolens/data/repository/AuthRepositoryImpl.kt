package com.proyek.foolens.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.ErrorResponse
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Otp
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
            Log.d(TAG, "Memulai request login untuk email: $email")
            val response = apiService.login(mapOf("email" to email, "password" to password))
            Log.d(TAG, "Response login diterima: code=${response.code()}")
            if (response.isSuccessful) {
                val userDto = response.body()
                Log.d(TAG, "Login sukses, body: $userDto")
                if (userDto != null && userDto.status == "success") {
                    val token = userDto.data.token
                    Log.d(TAG, "Login token diterima: $token")
                    if (token != null) {
                        tokenManager.saveToken(token)
                        val user = DataMapper.mapUserDtoToDomain(userDto)
                        currentUser = user
                        emit(NetworkResult.Success(user))
                    } else {
                        Log.e(TAG, "Token tidak ditemukan dalam respons")
                        emit(NetworkResult.Error("Token tidak ditemukan dalam respons"))
                    }
                } else {
                    val errorMessage = userDto?.message ?: "Login gagal"
                    Log.e(TAG, "Login gagal dengan pesan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Login gagal dengan code: ${response.code()}, error body: $errorBody")
                    if (!errorBody.isNullOrEmpty() && errorBody.startsWith("{")) {
                        try {
                            val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            val errorMessage = when (errorJson.error_code) {
                                "EMAIL_NOT_FOUND" -> "Email tidak terdaftar"
                                "INVALID_PASSWORD" -> "Password yang Anda masukkan salah"
                                else -> errorJson.message ?: "Login gagal: ${response.message()}"
                            }
                            val fieldError = when (errorJson.error_code) {
                                "EMAIL_NOT_FOUND" -> LoginState.Field.EMAIL
                                "INVALID_PASSWORD" -> LoginState.Field.PASSWORD
                                else -> null
                            }
                            Log.d(TAG, "Error message: $errorMessage, field error: $fieldError")
                            emit(NetworkResult.Error(errorMessage, fieldError))
                        } catch (e: JsonSyntaxException) {
                            Log.e(TAG, "Error parsing JSON response: ${e.message}")
                            emit(NetworkResult.Error("Format respons tidak valid: ${e.message}"))
                        }
                    } else {
                        Log.e(TAG, "Server mengembalikan respons non-JSON (mungkin HTML)")
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
                    Log.e(TAG, "Error saat memproses respons error: ${e.message}", e)
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException: ${e.message}", e)
            emit(NetworkResult.Error("Waktu koneksi habis. Silakan coba lagi."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "UnknownHostException: ${e.message}", e)
            emit(NetworkResult.Error("Server tidak dapat ditemukan. Periksa koneksi internet Anda."))
        } catch (e: ConnectException) {
            Log.e(TAG, "ConnectException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JsonSyntaxException: ${e.message}", e)
            emit(NetworkResult.Error("Server mengembalikan data yang tidak valid. Harap hubungi administrator."))
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}, code: ${e.code()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception umum: ${e.message}", e)
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
            Log.d(TAG, "Register response code: ${response.code()}")
            if (response.isSuccessful) {
                val userDto = response.body()
                Log.d(TAG, "Register response: $userDto")
                if (userDto != null && userDto.status == "success") {
                    val token = userDto.data.token
                    Log.d(TAG, "Register token received: $token")
                    if (token != null) {
                        tokenManager.saveToken(token)
                        val user = DataMapper.mapUserDtoToDomain(userDto)
                        currentUser = user
                        emit(NetworkResult.Success(user))
                    } else {
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
                Log.e(TAG, "Register error body: $errorBody")
                val errorMessage = when {
                    response.code() == 409 -> "Email sudah terdaftar"
                    errorBody?.contains("email") == true -> "Format email tidak valid"
                    errorBody?.contains("password") == true -> "Password minimal 6 karakter"
                    else -> "Registrasi gagal: ${response.message()}"
                }
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Register HTTP exception: ${e.message()}")
            emit(NetworkResult.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Register IO exception: ${e.message}")
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Register general exception: ${e.message}")
            emit(NetworkResult.Error("Error: ${e.message}"))
        }
    }

    override suspend fun logout(): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            if (tokenManager.hasToken()) {
                val response = apiService.logout()
                if (response.isSuccessful) {
                    tokenManager.clearToken()
                    currentUser = null
                    emit(NetworkResult.Success(Unit))
                } else {
                    emit(NetworkResult.Error("Logout gagal: ${response.message()}"))
                }
            } else {
                tokenManager.clearToken()
                currentUser = null
                emit(NetworkResult.Success(Unit))
            }
        } catch (e: Exception) {
            tokenManager.clearToken()
            currentUser = null
            emit(NetworkResult.Error("Logout error: ${e.message}"))
        }
    }

    override fun isLoggedIn(): Flow<NetworkResult<Boolean>> = flow {
        val hasToken = tokenManager.hasToken()
        Log.d(TAG, "isLoggedIn check: hasToken = $hasToken")
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
            Log.d(TAG, "GetUserProfile response code: ${response.code()}")
            Log.d(TAG, "Authorization header: Bearer ${tokenManager.getToken()}")
            if (response.isSuccessful) {
                val userDto = response.body()
                Log.d(TAG, "GetUserProfile response: $userDto")
                if (userDto != null && userDto.status == "success") {
                    val user = DataMapper.mapUserDtoToDomain(userDto)
                    currentUser = user
                    emit(NetworkResult.Success(user))
                } else {
                    emit(NetworkResult.Error("Gagal mengambil data pengguna"))
                }
            } else {
                Log.e(TAG, "GetUserProfile error: ${response.errorBody()?.string()}")
                if (currentUser != null) {
                    emit(NetworkResult.Success(currentUser!!))
                } else {
                    emit(NetworkResult.Error("Gagal mengambil data pengguna: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GetUserProfile exception: ${e.message}")
            if (currentUser != null) {
                emit(NetworkResult.Success(currentUser!!))
            } else {
                emit(NetworkResult.Error("Error: ${e.message}"))
            }
        }
    }

    override suspend fun sendOtp(email: String): Flow<NetworkResult<Otp>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Memulai request send OTP untuk email: $email")
            val response = apiService.sendOtp(mapOf("email" to email))
            Log.d(TAG, "Response send OTP diterima: code=${response.code()}")
            if (response.isSuccessful) {
                val sendOtpResponse = response.body()
                Log.d(TAG, "Send OTP sukses, body: $sendOtpResponse")
                if (sendOtpResponse != null && sendOtpResponse.status == "success") {
                    val otpResponse = DataMapper.mapSendOtpResponseToDomain(sendOtpResponse)
                    emit(NetworkResult.Success(otpResponse))
                } else {
                    val errorMessage = sendOtpResponse?.message ?: "Gagal mengirim OTP"
                    Log.e(TAG, "Send OTP gagal dengan pesan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Send OTP gagal dengan code: ${response.code()}, error body: $errorBody")
                    if (!errorBody.isNullOrEmpty() && errorBody.startsWith("{")) {
                        try {
                            val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            val errorMessage = when (errorJson.error_code) {
                                "EMAIL_NOT_FOUND" -> "Email tidak terdaftar"
                                else -> errorJson.message ?: "Gagal mengirim OTP: ${response.message()}"
                            }
                            emit(NetworkResult.Error(errorMessage))
                        } catch (e: JsonSyntaxException) {
                            Log.e(TAG, "Error parsing JSON response: ${e.message}")
                            emit(NetworkResult.Error("Format respons tidak valid: ${e.message}"))
                        }
                    } else {
                        val errorMessage = when (response.code()) {
                            422 -> "Data yang dimasukkan tidak valid"
                            500 -> "Terjadi kesalahan di server. Harap coba lagi nanti."
                            else -> "Gagal mengirim OTP dengan kode: ${response.code()}"
                        }
                        emit(NetworkResult.Error(errorMessage))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saat memproses respons error: ${e.message}", e)
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException: ${e.message}", e)
            emit(NetworkResult.Error("Waktu koneksi habis. Silakan coba lagi."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "UnknownHostException: ${e.message}", e)
            emit(NetworkResult.Error("Server tidak dapat ditemukan. Periksa koneksi internet Anda."))
        } catch (e: ConnectException) {
            Log.e(TAG, "ConnectException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JsonSyntaxException: ${e.message}", e)
            emit(NetworkResult.Error("Server mengembalikan data yang tidak valid. Harap hubungi administrator."))
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}, code: ${e.code()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception umum: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Flow<NetworkResult<Otp>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Memulai request verify OTP untuk email: $email")
            val response = apiService.verifyOtp(mapOf("email" to email, "otp" to otp))
            Log.d(TAG, "Response verify OTP diterima: code=${response.code()}")
            if (response.isSuccessful) {
                val verifyOtpResponse = response.body()
                Log.d(TAG, "Verify OTP sukses, body: $verifyOtpResponse")
                if (verifyOtpResponse != null && verifyOtpResponse.status == "success") {
                    val otpResponse = DataMapper.mapVerifyOtpResponseToDomain(verifyOtpResponse)
                    emit(NetworkResult.Success(otpResponse))
                } else {
                    val errorMessage = verifyOtpResponse?.message ?: "Gagal memverifikasi OTP"
                    Log.e(TAG, "Verify OTP gagal dengan pesan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Verify OTP gagal dengan code: ${response.code()}, error body: $errorBody")
                    if (!errorBody.isNullOrEmpty() && errorBody.startsWith("{")) {
                        try {
                            val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            val errorMessage = when (errorJson.error_code) {
                                "INVALID_OTP" -> "OTP tidak valid atau telah kedaluwarsa"
                                else -> errorJson.message ?: "Gagal memverifikasi OTP: ${response.message()}"
                            }
                            emit(NetworkResult.Error(errorMessage))
                        } catch (e: JsonSyntaxException) {
                            Log.e(TAG, "Error parsing JSON response: ${e.message}")
                            emit(NetworkResult.Error("Format respons tidak valid: ${e.message}"))
                        }
                    } else {
                        val errorMessage = when (response.code()) {
                            400 -> "OTP tidak valid atau telah kedaluwarsa"
                            422 -> "Data yang dimasukkan tidak valid"
                            500 -> "Terjadi kesalahan di server. Harap coba lagi nanti."
                            else -> "Gagal memverifikasi OTP dengan kode: ${response.code()}"
                        }
                        emit(NetworkResult.Error(errorMessage))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saat memproses respons error: ${e.message}", e)
                    emit(NetworkResult.Error("Terjadi kesalahan saat memproses respons: ${e.message}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException: ${e.message}", e)
            emit(NetworkResult.Error("Waktu koneksi habis. Silakan coba lagi."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "UnknownHostException: ${e.message}", e)
            emit(NetworkResult.Error("Server tidak dapat ditemukan. Periksa koneksi internet Anda."))
        } catch (e: ConnectException) {
            Log.e(TAG, "ConnectException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JsonSyntaxException: ${e.message}", e)
            emit(NetworkResult.Error("Server mengembalikan data yang tidak valid. Harap hubungi administrator."))
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}, code: ${e.code()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception umum: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun resetPassword(email: String, newPassword: String): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            Log.d(TAG, "Memulai request reset password untuk email: $email")
            val response = apiService.resetPassword(
                mapOf(
                    "email" to email,
                    "password" to newPassword,
                    "password_confirmation" to newPassword
                )
            )
            Log.d(TAG, "Response reset password diterima: code=${response.code()}")
            if (response.isSuccessful) {
                val resetResponse = response.body()
                Log.d(TAG, "Reset password sukses, body: $resetResponse")
                if (resetResponse != null && resetResponse.status == "success") {
                    emit(NetworkResult.Success(Unit))
                } else {
                    val errorMessage = resetResponse?.message ?: "Gagal mereset kata sandi"
                    Log.e(TAG, "Reset password gagal dengan pesan: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Reset password gagal dengan code: ${response.code()}, error body: $errorBody")
                    if (!errorBody.isNullOrEmpty() && errorBody.startsWith("{")) {
                        val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        val errorMessage = errorJson.message ?: "Gagal mereset kata sandi: ${response.message()}"
                        emit(NetworkResult.Error(errorMessage))
                    } else {
                        val errorMessage = when (response.code()) {
                            422 -> "Data yang dimasukkan tidak valid"
                            404 -> "Email tidak ditemukan"
                            500 -> "Terjadi kesalahan di server. Harap coba lagi nanti."
                            else -> "Gagal mereset kata sandi dengan kode: ${response.code()}"
                        }
                        emit(NetworkResult.Error(errorMessage))
                    }
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "Error parsing JSON response: ${e.message}")
                    emit(NetworkResult.Error("Format respons tidak valid: ${e.message}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException: ${e.message}", e)
            emit(NetworkResult.Error("Waktu koneksi habis. Silakan coba lagi."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "UnknownHostException: ${e.message}", e)
            emit(NetworkResult.Error("Server tidak dapat ditemukan. Periksa koneksi internet Anda."))
        } catch (e: ConnectException) {
            Log.e(TAG, "ConnectException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JsonSyntaxException: ${e.message}", e)
            emit(NetworkResult.Error("Server mengembalikan data yang tidak valid. Harap hubungi administrator."))
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException: ${e.message()}, code: ${e.code()}", e)
            emit(NetworkResult.Error("Kesalahan jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception umum: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}