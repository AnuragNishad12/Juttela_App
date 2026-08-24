package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class GeoAddRequestModel(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("activity")
    val activity: String,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("name")
    val name: String
)

data class GeoAddResponseModel(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ActivityData
)

data class ActivityData(
    @SerializedName("activity")
    val activity: String,
    @SerializedName("searchRadiusKm")
    val searchRadiusKm: Int,
    @SerializedName("matches")
    val matches: List<Match>,
    @SerializedName("matchCount")
    val matchCount: Int
)

data class Match(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("activity")
    val activity: String,
    @SerializedName("distanceKm")
    val distanceKm: Double
)