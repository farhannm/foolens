package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String
)
