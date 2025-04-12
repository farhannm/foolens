package com.proyek.foolens.domain.model

data class Profile(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val profilePicture: String?,
    val role: String?
)