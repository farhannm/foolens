package com.proyek.foolens.data.remote.api

import com.proyek.foolens.data.remote.dto.AllergenDetectionResponse
import com.proyek.foolens.data.remote.dto.AllergenResponse
import com.proyek.foolens.data.remote.dto.ProfileResponse
import com.proyek.foolens.data.remote.dto.UserAllergenRequest
import com.proyek.foolens.data.remote.dto.UserAllergenResponse
import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.util.Constants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Authentication endpoints
    @POST(Constants.ENDPOINT_LOGIN)
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): Response<UserDto>

    @POST(Constants.ENDPOINT_REGISTER)
    suspend fun register(
        @Body registerRequest: Map<String, String>
    ): Response<UserDto>

    @POST(Constants.ENDPOINT_LOGOUT)
    suspend fun logout(): Response<Map<String, String>>

    @GET(Constants.ENDPOINT_PROFILE)
    suspend fun getUserProfile(): Response<UserDto>

    // Profile endpoints
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @POST("profile")
    suspend fun updateProfile(
        @Part profilePicture: MultipartBody.Part?,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<ProfileResponse>

    // Allergen endpoints
    @POST(Constants.ENDPOINT_DETECT_ALLERGENS)
    suspend fun detectAllergens(
        @Body request: Map<String, String>
    ): Response<AllergenDetectionResponse>

    @GET(Constants.ENDPOINT_GET_ALL_ALLERGENS)
    suspend fun getAllAllergens(): Response<AllergenResponse>

    @GET(Constants.ENDPOINT_SEARCH_ALLERGENS)
    suspend fun searchAllergensByName(
        @Query("query") query: String
    ): Response<AllergenResponse>

    // User allergen management endpoints
    @GET(Constants.ENDPOINT_GET_USER_ALLERGENS)
    suspend fun getUserAllergens(
        @Path("user_id") userId: String
    ): Response<UserAllergenResponse>

    @POST(Constants.ENDPOINT_ADD_USER_ALLERGENS)
    suspend fun addUserAllergens(
        @Path("user_id") userId: String,
        @Body allergenRequest: UserAllergenRequest
    ): Response<UserAllergenResponse>

    @PUT(Constants.ENDPOINT_UPDATE_USER_ALLERGEN)
    suspend fun updateUserAllergen(
        @Path("user_id") userId: String,
        @Path("allergen_id") allergenId: Int,
        @Body updateRequest: Map<String, Any>
    ): Response<Map<String, Any>>

    @DELETE(Constants.ENDPOINT_DELETE_USER_ALLERGEN)
    suspend fun deleteUserAllergen(
        @Path("user_id") userId: String,
        @Path("allergen_id") allergenId: Int
    ): Response<Map<String, Any>>
}