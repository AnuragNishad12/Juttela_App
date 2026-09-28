package com.example.juttela.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juttela.DataSource.Models.ActivityItem
import com.example.juttela.DataSource.Models.NearbyActivity
import com.example.juttela.R
import com.example.juttela.ViewModels.PopularNearbyViewModel

private data class ActivityStyle(
    val icon: Int,
    val iconColor: Color,
    val backgroundColor: Color
)

private val activityStyles = mapOf(
    "football" to ActivityStyle(R.drawable.football, Color(0xFF2962FF), Color(0xFFE8EEFF)),
    "running" to ActivityStyle(R.drawable.running, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    "cycling" to ActivityStyle(R.drawable.cycling, Color(0xFFB26A00), Color(0xFFFFF3E0)),
    "yoga" to ActivityStyle(R.drawable.yoga, Color(0xFF5E35B1), Color(0xFFEDE7F6))
)

private val defaultStyle = ActivityStyle(
    icon = R.drawable.running,
    iconColor = Color(0xFF455A64),
    backgroundColor = Color(0xFFF5F5F5)
)

fun NearbyActivity.toActivityItem(): ActivityItem {
    val style = activityStyles[activity] ?: defaultStyle
    return ActivityItem(
        title = label,
        peopleNearby = peopleNearby,
        icon = style.icon,
        iconColor = style.iconColor,
        bgColor = style.backgroundColor
    )
}

@Composable
fun PopularNearYou(
    userId: String,
    longitude: Double,
    latitude: Double,
    viewModel: PopularNearbyViewModel = viewModel(),
    onActivityClick: (NearbyActivity) -> Unit = {}
) {
    val state = viewModel.state

    LaunchedEffect(userId, longitude, latitude) {
        if (userId.isNotBlank()) {
            viewModel.loadPopularNearby(
                userId = userId,
                longitude = longitude,
                latitude = latitude
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Popular near you",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.loading && state.items.isEmpty()) {
            Text(
                text = "Finding people nearby...",
                fontSize = 13.sp,
                color = Color.Gray
            )
        } else if (!state.success && state.items.isEmpty() && state.message.isNotBlank()) {
            Text(
                text = state.message,
                fontSize = 13.sp,
                color = Color.Gray
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(260.dp)
            ) {
                items(state.items, key = { it.activity }) { nearby ->
                    ActivityCard(
                        activity = nearby.toActivityItem(),
                        onClick = { onActivityClick(nearby) }
                    )
                }
            }
        }
    }
}