package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class SendingSmartRequest(
    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("recipientId")
    val recipientId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("photo")
    val photo: String?,

    @SerializedName("age")
    val age: Int?,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("interests")
    val interests: List<String>,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("distanceKm")
    val distanceKm: Double,

    @SerializedName("rating")
    val rating: Double,

    @SerializedName("feedbackCount")
    val feedbackCount: Int,

    @SerializedName("matchScore")
    val matchScore: Int,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class SendingSmartRequestResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SmartRequestData
)

data class SmartRequestData(
    @SerializedName("_id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("__v")
    val version: Int,

    @SerializedName("requests")
    val requests: List<SendingSmartRequest>
)