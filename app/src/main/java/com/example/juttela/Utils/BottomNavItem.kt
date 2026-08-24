package com.example.juttela.Utils


import androidx.annotation.DrawableRes
import com.example.juttela.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int
) {

    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = R.drawable.juttelahome
    )

    object Request : BottomNavItem(
        route = "Request",
        title = "Request",
        icon = R.drawable.requesticons
    )

    object History : BottomNavItem(
        route = "history",
        title = "History",
        icon = R.drawable.juttelahistory
    )

    object Chat : BottomNavItem(
        route = "chat",
        title = "Chat",
        icon = R.drawable.juttelachat
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = R.drawable.juttelaprofile
    )
}