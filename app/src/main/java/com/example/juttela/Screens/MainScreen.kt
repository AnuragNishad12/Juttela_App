package com.example.juttela.Screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.juttela.UI.HistoryScreen
import com.example.juttela.Utils.BottomBar

@Composable
fun MainScreen(rootNavController: NavHostController) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color(0xFFF5F5F5),
        bottomBar = { BottomBar(navController, currentRoute) }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("Request") { RequestScreen() }
            composable("history") {
                HistoryScreen(rootNavController)
            }
            composable("chat") {
                ChatScreen(navController = rootNavController)
            }
//            composable("profile") { ProfileScreen() }
        }
    }
}