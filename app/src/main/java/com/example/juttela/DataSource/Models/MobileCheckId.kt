package com.example.juttela.DataSource.Models

data class UserIdRequest(
    val userId: String
)

data class MobileCheckResponse(
    val success:Boolean,
    val message:String,
)