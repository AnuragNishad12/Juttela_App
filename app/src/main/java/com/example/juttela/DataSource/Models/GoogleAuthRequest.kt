package com.example.juttela.DataSource.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class GoogleAuthRequest(
    val idToken: String
)

data class GoogleAuthResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("user")
    val user: UserAuthData
)

@Serializable
data class UserAuthData(
    @SerialName("_id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("email")
    val email: String,

    @SerialName("googleId")
    val googleId: String? = null,

    @SerialName("authProvider")
    val authProvider: String? = null,

    @SerialName("profilePicture")
    val profilePicture: String? = null
)