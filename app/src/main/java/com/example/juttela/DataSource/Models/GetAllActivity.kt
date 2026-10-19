package com.example.juttela.DataSource.Models

data class GetAllActivity(
    val success: Boolean,
    val count: Int,
    val data: List<Activity>
)

data class Activity(
    val _id: String,
    val key: String,
    val label: String,
    val category: String,
    val icon: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)