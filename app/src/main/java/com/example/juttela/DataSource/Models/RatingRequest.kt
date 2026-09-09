package com.example.juttela.DataSource.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    @SerialName("fromUserId")
    val fromUserId: String,

    @SerialName("toUserId")
    val toUserId: String,

    @SerialName("stars")
    val stars: Int
)

@Serializable
data class RatingResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: RatingDataOriginal
)

@Serializable
data class RatingDataOriginal(
    @SerialName("_id")
    val id: String,

    @SerialName("toUserId")
    val toUserId: String,

    @SerialName("fromUserId")
    val fromUserId: String,

    @SerialName("__v")
    val version: Int,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("stars")
    val stars: Int,

    @SerialName("updatedAt")
    val updatedAt: String
)