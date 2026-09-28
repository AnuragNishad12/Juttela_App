package com.example.juttela.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.juttela.Utils.ArrivalCard
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.ArrivalsViewModel

private val JuttelaOrange = Color(0xFFFF7A1A)
private val ScreenBg = Color(0xFFF5F5F5)
private val TextDark = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF8A8A8A)

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: ArrivalsViewModel = viewModel()
) {
    val context = LocalContext.current
    val userId = remember { UserPrefs.getUserId(context) }
    val state = viewModel.state
    val arrivals = state.arrivals?.arrivals.orEmpty()

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            viewModel.getArrivals(userId) { _, _, _ -> }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Where you've been, and to whom",
                fontSize = 15.sp,
                color = TextGray
            )
        }

        // Scrollable middle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                userId.isNullOrBlank() -> {
                    EmptyState(
                        title = "User not logged in",
                        subtitle = "Please log in to view your history"
                    )
                }

                state.loading -> {
                    CircularProgressIndicator(
                        color = JuttelaOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                !state.success && state.message.isNotEmpty() -> {
                    EmptyState(
                        title = "Something went wrong",
                        subtitle = state.message
                    )
                }

                arrivals.isEmpty() -> {
                    EmptyState(
                        title = "No arrivals yet",
                        subtitle = "Your arrival history will show up here"
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = arrivals,
                            key = { it.id } // change if your model uses another unique field
                        ) { item ->
                            ArrivalCard(item = item)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.navigate("subscription") },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JuttelaOrange)
        ) {
            Text(
                text = "Juttela Pro",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = TextGray
        )
    }
}