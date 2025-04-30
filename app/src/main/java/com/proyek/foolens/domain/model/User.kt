package com.proyek.foolens.domain.model

data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val phone: String = "",
    val profilePicture: String?,
    val token: String = ""
)