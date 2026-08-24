package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class ConnectionsRequest(
    @SerializedName("userId")
    val userId: String
)

data class ConnectionsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ConnectionsData
)

data class ConnectionsData(
    @SerializedName("connections")
    val connections: List<Connection>
)

data class Connection(
    @SerializedName("otherUserId")
    val otherUserId: String,

    @SerializedName("otherUserName")
    val otherUserName: String,

    @SerializedName("activity")
    val activity: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("connectedAt")
    val connectedAt: String
)