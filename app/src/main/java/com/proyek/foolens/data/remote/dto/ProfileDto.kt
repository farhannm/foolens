package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: ProfileData,

    @SerializedName("message")
    val message: String? = null
) {
    data class ProfileData(
        @SerializedName("user")
        val user: ProfileUserDto
    )

    data class ProfileUserDto(
        @SerializedName("id")
        val id: Int,

        @SerializedName("name")
        val name: String,

        @SerializedName("email")
        val email: String,

        @SerializedName("phone_number")
        val phoneNumber: String?,

        @SerializedName("profile_picture")
        val profilePicture: String?,

        @SerializedName("role")
        val role: String?,

        @SerializedName("created_at")
        val createdAt: String?,

        @SerializedName("updated_at")
        val updatedAt: String?
    )
}