package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class SmartAcceptRequest(
    @SerializedName("currentUserId")
    val currentUserId: String,

    @SerializedName("currentUserName")
    val currentUserName: String,

    @SerializedName("requestId")
    val requestId: String
)

data class SmartAcceptResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SmartAcceptData
)

data class SmartAcceptData(
    @SerializedName("otherUserId")
    val otherUserId: String,

    @SerializedName("otherUserName")
    val otherUserName: String,

    @SerializedName("otherUserPhoto")
    val otherUserPhoto: String,

    @SerializedName("otherUserAge")
    val otherUserAge: Int,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("matchScore")
    val matchScore: Int
)