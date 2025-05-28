package com.proyek.foolens.domain.model

data class Otp(
    val status: String,
    val message: String,
    val email: String,
    val expiresIn: Int? = null,
    val sendingMethod: String? = null,
    val verifiedAt: String? = null,
    val resetToken: String? = null
)