package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class SignupResponse(
    val success:Boolean,
    val message:String,
    @SerializedName("user")
    val data:User
)