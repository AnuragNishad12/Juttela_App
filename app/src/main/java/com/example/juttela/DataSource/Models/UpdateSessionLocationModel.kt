package com.example.juttela.DataSource.Models

data class UpdateSessionLocationRequest(
    val userId: String,
    val lat: Double,
    val lng: Double
)

data class UpdateSessionLocationResponse(
    val sessionId: String,
    val userId: String,
    val distanceMeters: Double,
    val arrived: Boolean,
    val justArrived: Boolean,
    val notified: Boolean
)