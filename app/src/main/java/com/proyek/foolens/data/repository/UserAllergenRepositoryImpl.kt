package com.proyek.foolens.data.repository

import android.util.Log
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.remote.dto.UserAllergenRequest
import com.proyek.foolens.data.util.DataMapper
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.domain.repository.UserAllergenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAllergenRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserAllergenRepository {

    private val TAG = "UserAllergenRepoImpl"

    override suspend fun getUserAllergens(userId: String): Flow<NetworkResult<List<UserAllergen>>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Fetching allergens for user ID: $userId")

            val response = apiService.getUserAllergens(userId)

            if (response.isSuccessful) {
                val userAllergenResponse = response.body()
                Log.d(TAG, "Received allergens response: $userAllergenResponse")

                if (userAllergenResponse != null && userAllergenResponse.status == "success") {
                    val userAllergens = userAllergenResponse.userAllergens?.map { dto ->
                        DataMapper.mapUserAllergenDtoToDomain(dto)
                    } ?: emptyList()

                    emit(NetworkResult.Success(userAllergens))
                } else {
                    Log.e(TAG, "Failed to get allergens: ${userAllergenResponse?.status}")
                    emit(NetworkResult.Error("Gagal mendapatkan data alergen"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Gagal mendapatkan data alergen"
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

    override suspend fun addUserAllergens(
        userId: String,
        allergenIds: List<Int>,
        severityLevels: Map<Int, Int?>,
        notes: Map<Int, String?>
    ): Flow<NetworkResult<List<UserAllergen>>> = flow {
        emit(NetworkResult.Loading)

        // Input validation
        if (userId.isBlank()) {
            emit(NetworkResult.Error("User ID cannot be empty"))
            return@flow
        }

        if (allergenIds.isEmpty()) {
            emit(NetworkResult.Error("No allergens selected"))
            return@flow
        }

        try {
            // Format the allergen data according to backend API expectation
            val allergenList = allergenIds.map { allergenId ->
                UserAllergenRequest.AllergenEntry(
                    id = allergenId.toString(),
                    severityLevel = (severityLevels[allergenId]?.toString() ?: "1"),
                    notes = (notes[allergenId] ?: "")
                )
            }

            val requestBody = UserAllergenRequest(allergens = allergenList)

            Log.d(TAG, "Adding allergens: $requestBody for user $userId")

            val response = apiService.addUserAllergens(userId, requestBody)

            when {
                !response.isSuccessful -> {
                    val errorMessage = response.errorBody()?.string()
                        ?: "Failed to add allergens (Unknown error)"
                    Log.e(TAG, "API Error: $errorMessage")
                    emit(NetworkResult.Error(errorMessage))
                }
                response.body() == null -> {
                    Log.e(TAG, "Empty response body")
                    emit(NetworkResult.Error("No response from server"))
                }
                response.body()?.status != "success" -> {
                    val errorMsg = response.body()?.message
                        ?: "Failed to add allergens"
                    Log.e(TAG, errorMsg)
                    emit(NetworkResult.Error(errorMsg))
                }
                else -> {
                    // After successful addition, fetch the complete list of user allergens
                    val getAllergensResponse = apiService.getUserAllergens(userId)

                    if (getAllergensResponse.isSuccessful && getAllergensResponse.body() != null) {
                        val userAllergens = getAllergensResponse.body()?.userAllergens?.map { dto ->
                            DataMapper.mapUserAllergenDtoToDomain(dto)
                        } ?: emptyList()

                        Log.d(TAG, "Successfully added allergens, total count: ${userAllergens.size}")
                        emit(NetworkResult.Success(userAllergens))
                    } else {
                        // Return data from add response if available
                        val userAllergens = response.body()?.userAllergens?.map { dto ->
                            DataMapper.mapUserAllergenDtoToDomain(dto)
                        } ?: emptyList()

                        Log.d(TAG, "Successfully added ${userAllergens.size} allergens")
                        emit(NetworkResult.Success(userAllergens))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in addUserAllergens", e)
            emit(NetworkResult.Error("An error occurred: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserAllergen(
        userId: String,
        allergenId: Int,
        severityLevel: Int?,
        notes: String?
    ): Flow<NetworkResult<UserAllergen>> = flow {
        emit(NetworkResult.Loading)

        try {
            val requestBody = mutableMapOf<String, Any>()
            severityLevel?.let { requestBody["severity_level"] = it.toString() }
            notes?.let { requestBody["notes"] = it }

            Log.d(TAG, "Updating allergen ID $allergenId for user $userId with data: $requestBody")

            val response = apiService.updateUserAllergen(userId, allergenId, requestBody)

            if (response.isSuccessful) {
                // Since the API doesn't return the complete updated allergen,
                // fetch the allergens list again to get the updated version
                val getAllergensResponse = apiService.getUserAllergens(userId)

                if (getAllergensResponse.isSuccessful && getAllergensResponse.body() != null) {
                    val userAllergens = getAllergensResponse.body()?.userAllergens
                    val updatedAllergen = userAllergens?.find { it.id == allergenId }

                    if (updatedAllergen != null) {
                        Log.d(TAG, "Successfully updated allergen: $updatedAllergen")
                        emit(NetworkResult.Success(DataMapper.mapUserAllergenDtoToDomain(updatedAllergen)))
                    } else {
                        Log.e(TAG, "Allergen not found after update")
                        emit(NetworkResult.Error("Alergen tidak ditemukan setelah pembaruan"))
                    }
                } else {
                    Log.e(TAG, "Failed to fetch updated allergen list")
                    emit(NetworkResult.Error("Gagal mendapatkan data alergen terbaru"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Gagal memperbarui alergen"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in updateUserAllergen", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }

    override suspend fun deleteUserAllergen(
        userId: String,
        allergenId: Int
    ): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)

        try {
            Log.d(TAG, "Deleting allergen ID $allergenId for user $userId")

            val response = apiService.deleteUserAllergen(userId, allergenId)

            if (response.isSuccessful) {
                Log.d(TAG, "Successfully deleted allergen")
                emit(NetworkResult.Success(true))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Gagal menghapus alergen"
                Log.e(TAG, "Error: ${response.code()}, $errorMessage")
                emit(NetworkResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in deleteUserAllergen", e)
            emit(NetworkResult.Error("Terjadi kesalahan: ${e.message}"))
        }
    }
}