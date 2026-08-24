package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class RequestModels(
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("senderName")
    val senderName: String,
    @SerializedName("recipientId")
    val recipientId: String,
    @SerializedName("activity")
    val activity: String,
    @SerializedName("distanceKm")
    val distanceKm: Double
)


data class RequestResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RequestData
)

data class RequestData(
    @SerializedName("_id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("__v")
    val version: Int,

    @SerializedName("requests")
    val requests: List<UserDataRequest>
)

data class UserDataRequest(
    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("distanceKm")
    val distanceKm: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)


data class GetIdRequest(
    @SerializedName("userId")
    val userId: String
)


data class GetRequestsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RequestsData
)

data class RequestsData(
    @SerializedName("requests")
    val requests: List<UserRequest>
)

data class UserRequest(
    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("distanceKm")
    val distanceKm: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)


