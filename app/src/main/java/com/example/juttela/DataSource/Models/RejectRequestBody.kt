package com.example.juttela.DataSource.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RejectRequestBody(
    @SerialName("currentUserId")
    val currentUserId: String,

    @SerialName("requestId")
    val requestId: String
)

@Serializable
data class RejectRequestResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: RejectRequestData? = null
)

@Serializable
data class RejectRequestData(
    @SerialName("otherUserId")
    val otherUserId: String,

    @SerialName("otherUserName")
    val otherUserName: String,

    @SerialName("activity")
    val activity: String
)