package com.example.juttela.Utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.juttela.DataSource.Models.SmartGetRequest

@Composable
fun SmartRequestCard(
    request: SmartGetRequest,
    isAccepting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!request.photo.isNullOrBlank()) {
                    AsyncImage(
                        model = request.photo,
                        contentDescription = request.senderName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.senderName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "${request.activity.replaceFirstChar { it.uppercaseChar() }} • ${formatDistance(request.distanceKm)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                MatchScoreBadge(score = request.matchScore)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${request.age} yrs • ${request.gender.replaceFirstChar { it.uppercaseChar() }}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            if (request.interests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.interests.joinToString(" • "),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Rating ${request.rating} • ${request.feedbackCount} reviews",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = request.status.replaceFirstChar { it.uppercaseChar() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (request.status.equals("pending", true))
                    Color(0xFFFF7B00) else Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReject,
                    enabled = !isAccepting,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F0F0),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Reject", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onAccept,
                    enabled = !isAccepting,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7B00),
                        contentColor = Color.White
                    )
                ) {
                    if (isAccepting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(18.dp)
                        )
                    } else {
                        Text("Accept", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }


}

// ============================================================
// MATCH SCORE BADGE — pill with gradient tint, icon, and score/label
// ============================================================
@Composable
private fun MatchScoreBadge(score: Int) {
    val label: String
    val icon: ImageVector
    val colorStart: Color
    val colorEnd: Color

    when {
        score >= 80 -> {
            label = "Great match"
            icon = Icons.Filled.Build
            colorStart = Color(0xFF34A853)
            colorEnd = Color(0xFF1E7D3A)
        }
        score >= 60 -> {
            label = "Good match"
            icon = Icons.Filled.ThumbUp
            colorStart = Color(0xFFFF9A3D)
            colorEnd = Color(0xFFFF7B00)
        }
        score >= 40 -> {
            label = "Okay match"
            icon = Icons.Filled.Star
            colorStart = Color(0xFFFBC02D)
            colorEnd = Color(0xFFF9A825)
        }
        else -> {
            label = "Low match"
            icon = Icons.Filled.ArrowDropDown
            colorStart = Color(0xFFBDBDBD)
            colorEnd = Color(0xFF9E9E9E)
        }
    }

    val gradient = Brush.horizontalGradient(colors = listOf(colorStart, colorEnd))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))   // was 20.dp — square-ish corners now
            .background(gradient)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "$score%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

private fun formatDistance(km: Double): String {
    return if (km < 1.0) {
        "${(km * 1000).toInt()} m away"
    } else {
        "${"%.1f".format(km)} km away"
    }
}