package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("about")
    val about: String,

    @SerializedName("interests")
    val interests: List<String>
)

data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ProfileData
)

data class ProfileData(
    @SerializedName("_id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("__v")
    val version: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("about")
    val about: String,

    @SerializedName("interests")
    val interests: List<String> = emptyList(),

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("rating")
    val rating: Double = 0.0,

    @SerializedName("feedbackCount")
    val feedbackCount: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)