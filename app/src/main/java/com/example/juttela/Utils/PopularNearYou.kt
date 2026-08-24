// com/example/juttela/components/PopularNearYou.kt
package com.example.juttela.components

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juttela.DataSource.Models.ActivityItem
import com.example.juttela.R


private val activities = listOf(
    ActivityItem("Football", 8, R.drawable.football, Color(0xFF2962FF), Color(0xFFE8EEFF)),
    ActivityItem("Running",  5,  R.drawable.running,  Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    ActivityItem("Cycling",  3,  R.drawable.cycling,  Color(0xFFB26A00), Color(0xFFFFF3E0)),
    ActivityItem("Yoga",     6,  R.drawable.yoga,     Color(0xFF5E35B1), Color(0xFFEDE7F6))
)

@Composable
fun PopularNearYou(
    items: List<ActivityItem> = activities,
    onActivityClick: (ActivityItem) -> Unit = {}
) {
    Column {
        Text(
            text = "Popular near you",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(260.dp)
        ) {
            items(items) { activity ->
                ActivityCard(
                    activity = activity,
                    onClick = { onActivityClick(activity) }
                )
            }
        }
    }
}