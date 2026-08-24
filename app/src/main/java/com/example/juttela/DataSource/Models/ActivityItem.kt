package com.example.juttela.DataSource.Models


import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class ActivityItem(
    val title: String,
    val peopleNearby: Int,
    @DrawableRes val icon: Int,
    val iconColor: Color,
    val bgColor: Color
)