package com.proyek.foolens.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChangePasswordResponse( @SerializedName("status") val status: String,
   @SerializedName("message")
   val message: String? = null,

   @SerializedName("data")
   val data: ChangePasswordData? = null,

   @SerializedName("errors")
   val errors: Map<String, List<String>>? = null

) { data class ChangePasswordData( @SerializedName("user_id") val userId: Int,
        @SerializedName("email")
        val email: String,

        @SerializedName("updated_at")
        val updatedAt: String?
    )

}