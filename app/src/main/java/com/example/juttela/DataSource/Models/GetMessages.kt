package com.example.juttela.DataSource.Models
import com.google.gson.annotations.SerializedName

data class GetMessagesRequest(
    @SerializedName("currentUserId")
    val currentUserId: String,

    @SerializedName("otherUserId")
    val otherUserId: String
)

data class GetMessagesResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: MessagesData
)

data class MessagesData(
    @SerializedName("messages")
    val messages: List<Message>
)

data class Message(
    @SerializedName("_id")
    val id: String,

    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("receiverId")
    val receiverId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("__v")
    val version: Int
)