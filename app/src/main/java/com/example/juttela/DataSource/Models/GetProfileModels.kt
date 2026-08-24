package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class GetProfileRequest(
    @SerializedName("userId")
    val userId: String
)

data class GetProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ProfileDataNew
)

data class ProfileDataNew(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("about")
    val about: String?,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("age")
    val age: Int?,

    @SerializedName("gender")
    val gender: String?,


    @SerializedName("interests")
val interests: List<String>? = emptyList(),

@SerializedName("rating")
val rating: RatingData,

@SerializedName("feedbackCount")
val feedbackCount: Int = 0

)
data class RatingData(
    @SerializedName("average")
    val average: Double = 0.0,

    @SerializedName("count")
    val count: Int = 0
)

