package com.example.juttela.DataSource.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class LocationPinRequest(
    val lat: Double,
    val lng: Double
)

data class LocationSessionStartRequest(
    val pin: LocationPinRequest,
    val invitedBy: String? = null
)

data class LocationSessionResponse(
    val sessionId: String,
    val pin: LocationPinRequest,
    val createdAt: Long,
    val invitedBy: String? = null,
    val expiresInSeconds: Long? = null
)





@Serializable
data class CancelSessionResponse(
    @SerialName("sessionId")
    val sessionId: String,

    @SerialName("cancelled")
    val cancelled: Boolean
)