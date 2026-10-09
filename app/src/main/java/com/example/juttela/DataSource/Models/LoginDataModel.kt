package com.example.juttela.DataSource.Models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userId: String?,
    val user: UserDataLogin?
)


data class UserDataLogin(
    val _id: String?,
    val email: String?,
    val authProvider: String?
)