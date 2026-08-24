package com.example.juttela.Utils

import java.util.Calendar

fun getGreeting(): String {

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🌇"
        else -> "Good Night 🌙"
    }
}