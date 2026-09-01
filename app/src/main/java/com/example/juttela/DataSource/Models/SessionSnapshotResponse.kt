package com.example.juttela.DataSource.Models


data class SessionSnapshotResponse(
    val sessionId: String,
    val pin: LocationPinRequest,
    val users: List<SessionSnapshotUser>,
    val snapshotAt: Long
)

data class SessionSnapshotUser(
    val userId: String,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Double,
    val arrived: Boolean,
    val updatedAt: Long
)