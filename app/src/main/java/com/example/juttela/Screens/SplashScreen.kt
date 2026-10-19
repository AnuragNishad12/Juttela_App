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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.juttela.Utils.ApprovalPrefs
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.ApprovalViewModel
import com.example.juttela.ViewModels.AuthViewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import com.onesignal.OneSignal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val approvalViewModel: ApprovalViewModel = viewModel()

    val context = LocalContext.current

    val leftOffset = remember { Animatable(-400f) }
    val rightOffset = remember { Animatable(400f) }
    val logoAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    // Retry / error state
    var attempt by remember { mutableIntStateOf(0) }   // bump to re-run the whole flow
    var showRetry by remember { mutableStateOf(false) }
    var navigated by remember { mutableStateOf(false) }

    fun goTo(route: String) {
        if (navigated) return
        navigated = true
        navController.navigate(route) {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ---------- Main flow: runs ONE call at a time ----------
    LaunchedEffect(attempt) {
        showRetry = false

        val storedUserId = UserPrefs.getUserId(context)
        Log.d("STORED_USER_ID", storedUserId ?: "none found")

        if (storedUserId == null) {
            delay(1500)
            goTo("signup")
            return@LaunchedEffect
        }

        // Step 1: Approval (with retries + timeout)
        val maxRetries = 3
        var tries = 0
        var approved = false

        while (tries < maxRetries && !approved) {
            val result = CompletableDeferred<Boolean>()

            approvalViewModel.getUserApproval(storedUserId) { success, message, approval ->
                Log.d(
                    "ApprovalResult",
                    "success=$success, message=$message, status=${approval?.approvalStatus}"
                )

                if (success) {
                    ApprovalPrefs.saveStatus(context, approval?.approvalStatus) // <-- add this
                }

                result.complete(success)
            }

            // Don't wait forever if the callback never fires
            approved = withTimeoutOrNull(15_000) { result.await() } ?: false

            if (!approved) {
                tries++
                if (tries < maxRetries) delay(1500L * tries) // 1.5s, then 3s
            }
        }

        if (!approved) {
            // Server down / 502: do NOT log the user out, let them retry
            showRetry = true
            return@LaunchedEffect
        }

        // Step 2: Auth check (OneSignal is not your backend, so it's safe here)
        OneSignal.login(storedUserId)
        authViewModel.checkUserId(storedUserId)
        // Step 3 (pro status) runs after auth succeeds, see the effect below
    }

    // ---------- Logo animations ----------
    LaunchedEffect(Unit) {
        leftOffset.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        rightOffset.animateTo(10f, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(400)
        taglineAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    // ---------- Navigation after AuthViewModel result ----------
    LaunchedEffect(authViewModel.success, authViewModel.message) {
        if (authViewModel.message.isNotEmpty()) {
            if (authViewModel.success) {
                // Step 3: pro status only AFTER auth has finished
                subscriptionViewModel.checkProStatus()
                delay(2000)
                goTo("main")
            } else {
                delay(2000)
                goTo("signup")
            }
        }
    }

    // ---------- UI ----------
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

            if (showRetry) {
                Text(
                    text = "Couldn't reach the server",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { attempt++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Retry", color = Color.Black)
                }
            } else {
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
}