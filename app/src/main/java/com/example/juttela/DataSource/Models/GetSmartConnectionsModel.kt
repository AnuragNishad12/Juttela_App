package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class GetSmartConnectionsRequest(
    @SerializedName("userId")
    val userId: String
)

data class GetSmartConnectionsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: GetSmartConnectionsData
)

data class GetSmartConnectionsData(
    @SerializedName("connections")
    val connections: List<GetSmartConnection>
)

data class GetSmartConnection(
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
    val matchScore: Int,

    @SerializedName("_id")
    val id: String,

    @SerializedName("connectedAt")
    val connectedAt: String
)