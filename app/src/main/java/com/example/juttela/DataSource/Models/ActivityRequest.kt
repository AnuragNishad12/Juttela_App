package com.example.juttela.DataSource.Models

data class ActivityRequest(
    val key: String,
    val label: String,
    val category: String,
    val icon: String,
    val sortOrder: Int,
    val isActive: Boolean
)

data class ActivityResponse(
    val success: Boolean,
    val message: String,
    val data: ActivityDataNewData
)

data class ActivityDataNewData(
    val key: String,
    val label: String,
    val category: String,
    val icon: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val _id: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)