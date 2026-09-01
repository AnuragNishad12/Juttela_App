package com.example.juttela.DataSource.Models


data class AddUserToSessionRequest(
    val userId: String
)

data class AddUserToSessionResponse(
    val sessionId: String,
    val userId: String,
    val attached: Boolean
)