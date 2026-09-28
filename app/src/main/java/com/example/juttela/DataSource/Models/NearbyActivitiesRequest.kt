package com.example.juttela.DataSource.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbyActivitiesRequest(
    @SerialName("userId")
    val userId: String,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("radiusKm")
    val radiusKm: Int,

    @SerialName("limit")
    val limit: Int
)

@Serializable
data class NearbyActivitiesResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("data")
    val data: NearbyActivitiesData
)

@Serializable
data class NearbyActivitiesData(
    @SerialName("searchRadiusKm")
    val searchRadiusKm: Int,

    @SerialName("items")
    val items: List<NearbyActivity>
)

@Serializable
data class NearbyActivity(
    @SerialName("activity")
    val activity: String,

    @SerialName("label")
    val label: String,

    @SerialName("peopleNearby")
    val peopleNearby: Int,

    @SerialName("source")
    val source: String
)