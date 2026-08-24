package com.example.juttela.DataSource.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmartMatchRequest(
    @SerialName("userId")
    val userId: String,

    @SerialName("name")
    val name: String,

    @SerialName("activity")
    val activity: String,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("radiusKm")
    val radiusKm: Double,

    @SerialName("agePreference")
    val agePreference: AgePreference,

    @SerialName("genderPreference")
    val genderPreference: String
)

@Serializable
data class AgePreference(
    @SerialName("min")
    val min: Int,

    @SerialName("max")
    val max: Int
)

@Serializable
data class SmartMatchResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: SmartMatchData
)

@Serializable
data class SmartMatchData(
    @SerialName("activity")
    val activity: String,

    @SerialName("searchRadiusKm")
    val searchRadiusKm: Double,

    @SerialName("matches")
    val matches: List<SmartMatch>,

    @SerialName("matchCount")
    val matchCount: Int
)

@Serializable
data class SmartMatch(
    @SerialName("userId")
    val userId: String,

    @SerialName("name")
    val name: String,

    @SerialName("photo")
    val photo: String?,

    @SerialName("age")
    val age: Int,

    @SerialName("gender")
    val gender: String,

    @SerialName("interests")
    val interests: List<String>,

    @SerialName("activity")
    val activity: String,

    @SerialName("distanceKm")
    val distanceKm: Double,

    @SerialName("rating")
    val rating: Double,

    @SerialName("feedbackCount")
    val feedbackCount: Int,

    @SerialName("matchScore")
    val matchScore: Int
)