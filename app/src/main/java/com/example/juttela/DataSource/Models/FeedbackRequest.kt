package com.example.juttela.DataSource.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequest(
    @SerialName("fromUserId")
    val fromUserId: String,

    @SerialName("toUserId")
    val toUserId: String,

    @SerialName("message")
    val message: String
)

@Serializable
data class FeedbackResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: FeedbackDataOriginal
)

@Serializable
data class FeedbackDataOriginal(
    @SerialName("fromUserId")
    val fromUserId: String,

    @SerialName("toUserId")
    val toUserId: String,

    @SerialName("message")
    val message: String,

    @SerialName("_id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String,

    @SerialName("__v")
    val version: Int
)