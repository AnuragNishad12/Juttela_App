package com.example.juttela

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.juttela.Screens.ActivityLocationMapScreen
import com.example.juttela.Screens.ChatConversationScreen
import com.example.juttela.Screens.HomeScreen
import com.example.juttela.Screens.LiveTrackingMapScreen
import com.example.juttela.Screens.LoginScreen
import com.example.juttela.Screens.MainScreen
import com.example.juttela.Screens.SignUpScreen
import com.example.juttela.Screens.SplashScreen
import com.example.juttela.Screens.Subscription.SubscriptionScreen
import com.example.juttela.UI.HistoryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen()
        }



        composable("signup") {
            SignUpScreen(
                navController = navController
            )
        }

        composable("map_picker") {
            ActivityLocationMapScreen(navController = navController)
        }

        composable("login") {
            LoginScreen()
        }

        composable("main") {
            MainScreen(rootNavController = navController)
        }

        composable("subscription") {
            SubscriptionScreen()
        }

        composable("live_tracking/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            LiveTrackingMapScreen(navController = navController, sessionId = sessionId)
        }



        composable("chatConversation/{otherUserId}/{otherUserName}/{activity}") { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("otherUserId").orEmpty()
            val otherUserName = Uri.decode(backStackEntry.arguments?.getString("otherUserName").orEmpty())
            val activity = Uri.decode(backStackEntry.arguments?.getString("activity").orEmpty())

            ChatConversationScreen(
                navController = navController,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                activity = activity
            )
        }

    }
}