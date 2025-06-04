package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("data")
    val data: UserData,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?
) {
    data class UserData(
        @SerializedName("user_id")
        val userId: Int?,

        @SerializedName("name")
        val name: String,

        @SerializedName("email")
        val email: String,

        @SerializedName("phone_number")
        val phoneNumber: String? = null,

        @SerializedName("profile_picture")
        val profilePicture: String? = null,

        @SerializedName("access_token")
        val token: String? = null,

        @SerializedName("token_type")
        val tokenType: String? = null,

        @SerializedName("expires_in")
        val expiresIn: Int? = null,

        @SerializedName("role")
        val role: String? = null
    )

    override fun toString(): String {
        return "UserDto(status=$status, message=$message, data=${data}, token=${data.token})"
    }
}