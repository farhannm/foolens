package com.proyek.foolens.data.remote.api

import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.util.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST(Constants.ENDPOINT_LOGIN)
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): Response<UserDto>

    @POST(Constants.ENDPOINT_REGISTER)
    suspend fun register(
        @Body registerRequest: Map<String, String>
    ): Response<UserDto>

    @GET(Constants.ENDPOINT_USER)
    suspend fun getUserProfile(): Response<UserDto>
}