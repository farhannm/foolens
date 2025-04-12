package com.proyek.foolens.data.repository

import android.util.Log
import com.proyek.foolens.data.preferences.PreferencesManager
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.Profile
import com.proyek.foolens.domain.repository.ProfileRepository
import com.proyek.foolens.util.Constants
import com.proyek.foolens.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ProfileRepository {

    private val TAG = "ProfileRepositoryImpl"

    override suspend fun getProfile(): Flow<NetworkResult<Profile>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Fetching user profile")
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val profileResponse = response.body()

                if (profileResponse != null && profileResponse.status == "success") {
                    val userData = profileResponse.data.user
                    val fullProfilePicUrl = getFullImageUrl(userData.profilePicture)

                    Log.d(TAG, "Profile picture URL from API: ${userData.profilePicture}")
                    Log.d(TAG, "Converted profile picture URL: $fullProfilePicUrl")

                    val profile = Profile(
                        id = userData.id,
                        name = userData.name,
                        email = userData.email,
                        phoneNumber = userData.phoneNumber,
                        profilePicture = fullProfilePicUrl,
                        role = userData.role
                    )

                    emit(NetworkResult.Success(profile))
                } else {
                    Log.e(TAG, "Failed to get profile: ${profileResponse?.status}")
                    emit(NetworkResult.Error("Gagal mendapatkan data profil"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Gagal mendapatkan data profil"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}, ${e.code()}", e)
            emit(NetworkResult.Error("Error jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun updateProfile(
        name: String?,
        phoneNumber: String?,
        profilePicture: File?
    ): Flow<NetworkResult<Profile>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Updating user profile")

            // Prepare multipart form data
            val params = mutableMapOf<String, okhttp3.RequestBody>()

            name?.let {
                params["name"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            phoneNumber?.let {
                params["phone_number"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            // Prepare profile picture if provided
            val profilePicturePart = profilePicture?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profile_picture", it.name, requestFile)
            }

            val response = apiService.updateProfile(profilePicturePart, params)

            if (response.isSuccessful) {
                val profileResponse = response.body()

                if (profileResponse != null && profileResponse.status == "success") {
                    val userData = profileResponse.data.user
                    val fullProfilePicUrl = getFullImageUrl(userData.profilePicture)

                    Log.d(TAG, "Updated profile picture URL from API: ${userData.profilePicture}")
                    Log.d(TAG, "Converted updated profile picture URL: $fullProfilePicUrl")

                    val profile = Profile(
                        id = userData.id,
                        name = userData.name,
                        email = userData.email,
                        phoneNumber = userData.phoneNumber,
                        profilePicture = fullProfilePicUrl,
                        role = userData.role
                    )

                    emit(NetworkResult.Success(profile))
                } else {
                    Log.e(TAG, "Failed to update profile: ${profileResponse?.status}")
                    emit(NetworkResult.Error("Gagal memperbarui data profil"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Gagal memperbarui data profil"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}, ${e.code()}", e)
            emit(NetworkResult.Error("Error jaringan: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            emit(NetworkResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    /**
     * Helper method to convert relative image paths to full URLs
     */
    private fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrEmpty()) return null

        // If the URL already starts with http:// or https://, return it as is
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }

        // Use the base URL from Constants and remove any /api part
        val baseUrl = Constants.BASE_URL.trimEnd('/')

        // Remove the '/api' part if it exists in the URL
        val cleanBaseUrl = baseUrl.replace("/api", "")

        val cleanPath = relativePath.trimStart('/') // Ensure no leading slash

        // Construct the full URL with the correct base and storage path
        val fullUrl = "$cleanBaseUrl/storage/$cleanPath"

        Log.d(TAG, "Converted $relativePath to $fullUrl")
        return fullUrl
    }
}