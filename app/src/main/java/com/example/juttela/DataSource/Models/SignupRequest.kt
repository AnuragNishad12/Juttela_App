package com.example.juttela.DataSource.Models

data class SignupRequest(
    val name:String,
    val email:String,
    val password:String,
)

data class DeleteAccountResponse(
    val success: Boolean,
    val message: String,
    val deletedUser: DeletedUser
)

data class DeletedUser(
    val _id: String,
    val name: String,
    val email: String
)