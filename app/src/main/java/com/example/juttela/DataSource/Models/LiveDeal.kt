package com.example.juttela.DataSource.Models

data class LiveDeal(
    val id: String,
    val title: String,
    val offerText: String,
    val imageUrl: String,
    val streamUrl: String? = null,
    val isLive: Boolean = true
)