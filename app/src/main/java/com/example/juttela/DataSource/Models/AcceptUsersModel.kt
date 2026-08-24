package com.example.juttela.DataSource.Models
import com.google.gson.annotations.SerializedName

data class AcceptUsersModel(
    @SerializedName("currentUserId")
    val currentUserId : String,
    @SerializedName("currentUserName")
    val currentUserName : String,
    @SerializedName("requestId")
    val requestId : String
)


data class RequestAcceptedResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ConnectionData
)

data class ConnectionData(
    @SerializedName("otherUserId")
    val otherUserId: String,

    @SerializedName("otherUserName")
    val otherUserName: String,

    @SerializedName("activity")
    val activity: String
)