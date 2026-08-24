package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("receiverId")
    val receiverId: String,

    @SerializedName("text")
    val text: String
)

data class SendMessageResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: MessageData
)


data class MessageData(
    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("receiverId")
    val receiverId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("__v")
    val version: Int
)