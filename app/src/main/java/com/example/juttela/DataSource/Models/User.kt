package com.example.juttela.DataSource.Models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val mobileId: String,
    val password: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

