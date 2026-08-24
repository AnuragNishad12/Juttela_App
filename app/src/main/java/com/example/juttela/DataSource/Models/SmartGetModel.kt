package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class SmartGetRequestBody(
    @SerializedName("userId")
    val userId: String
)


data class SmartGetRequestResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SmartGetRequestData
)

data class SmartGetRequestData(
    @SerializedName("requests")
    val requests: List<SmartGetRequest>
)

data class SmartGetRequest(
    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("photo")
    val photo: String?,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("interests")
    val interests: List<String>,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("distanceKm")
    val distanceKm: Double,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("feedbackCount")
    val feedbackCount: Int,

    @SerializedName("matchScore")
    val matchScore: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)