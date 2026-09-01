package com.example.juttela.DataSource.Models


import com.google.gson.annotations.SerializedName

data class ArrivalsResponse(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("arrivals")
    val arrivals: List<Arrival>
)

data class Arrival(
    @SerializedName("_id")
    val id: String,

    @SerializedName("sessionId")
    val sessionId: String,

    @SerializedName("arrivedUserId")
    val arrivedUserId: String,

    @SerializedName("senderId")
    val senderId: String,

    @SerializedName("sender")
    val sender: Sender,

    @SerializedName("pin")
    val pin: Location,

    @SerializedName("arrival")
    val arrival: Location,

    @SerializedName("arrivedAt")
    val arrivedAt: Long,

    @SerializedName("arrivedAtIso")
    val arrivedAtIso: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("__v")
    val version: Int
)

data class Sender(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("name")
    val name: String
)

data class Location(
    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double
)