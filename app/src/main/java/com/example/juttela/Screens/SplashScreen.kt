package com.example.juttela.Screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.juttela.R
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.AuthViewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import com.onesignal.OneSignal
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val subscriptionViewModel: SubscriptionViewModel = viewModel()


    val leftOffset = remember { Animatable(-400f) }
    val rightOffset = remember { Animatable(400f) }
    val logoAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val storedUserId = UserPrefs.getUserId(context)

        Log.d("STORED_USER_ID", storedUserId ?: "none found")

        if (storedUserId != null) {
            subscriptionViewModel.checkProStatus()
            OneSignal.login(storedUserId)
            authViewModel.checkUserId(storedUserId)
        } else {
            // No user stored locally yet - skip the API call, go straight to signup
            delay(1500) // small delay so splash still shows briefly
            navController.navigate("signup") {
                popUpTo("splash") { inclusive = true }
            }
        }

        leftOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        rightOffset.animateTo(
            targetValue = 10f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        delay(400)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(authViewModel.success, authViewModel.message) {
        if (authViewModel.message.isNotEmpty()) {
            delay(2000)

            if (authViewModel.success) {
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("signup") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color.Black,
                        Color(0xFF0A0A0A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo block
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.j),
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = leftOffset.value.dp)
                )

                Image(
                    painter = painterResource(R.drawable.innerj),
                    contentDescription = null,
                    modifier = Modifier
                        .size(270.dp)
                        .offset(x = rightOffset.value.dp, y = 7.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "JUTELLA",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFA500),
                trackColor = Color(0xFF2A2A2A),
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .alpha(taglineAlpha.value)
            )
        }
    }
}