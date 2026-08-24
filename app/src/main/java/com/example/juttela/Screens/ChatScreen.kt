package com.example.juttela.Screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.juttela.DataSource.Models.Connection
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.ConnectionsViewModel

@Composable
fun ChatScreen(navController: NavHostController) {
    val context = LocalContext.current
    val connectionsViewModel: ConnectionsViewModel = viewModel()
    val state = connectionsViewModel.state

    LaunchedEffect(Unit) {
        val userId = UserPrefs.getUserId(context)
        if (userId != null) {
            connectionsViewModel.getMyConnections(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ==================== HEADER ====================
        Text(
            text = "Chat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (state.connections.isNotEmpty())
                "${state.connections.size} people you're connected with"
            else
                "People you're connected with",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            state.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF7B00), strokeWidth = 3.dp)
                }
            }

            !state.success -> {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "Something went wrong",
                    subtitle = state.message.ifEmpty { "Please try again in a moment" }
                )
            }

            state.connections.isEmpty() -> {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "No connections yet",
                    subtitle = "Once you connect with someone, they'll show up here"
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(state.connections) { connection ->
                        ConnectionCard(
                            connection = connection,
                            onClick = {
                                val encodedName = Uri.encode(connection.otherUserName)
                                navController.navigate("chatConversation/${connection.otherUserId}/$encodedName")
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// CONNECTION CARD — elevated card, initials avatar, activity pill, chevron
// ============================================================
@Composable
private fun ConnectionCard(
    connection: Connection,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(name = connection.otherUserName)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.otherUserName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                ActivityPill(activity = connection.activity)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open chat",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================
// INITIALS AVATAR — colored circle with the first letter of the name,
// color picked deterministically per person so avatars stay consistent
// but vary across the list.
// ============================================================
@Composable
private fun InitialsAvatar(name: String) {
    val palette = listOf(
        Color(0xFF4A6CF7), // blue
        Color(0xFFFF7B00), // orange
        Color(0xFF2E7D32), // green
        Color(0xFF8E24AA), // purple
        Color(0xFFEF5350), // red
        Color(0xFF00897B)  // teal
    )
    val color = palette[(name.hashCode().let { if (it < 0) -it else it }) % palette.size]
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ============================================================
// ACTIVITY PILL — small rounded tag with an icon, replaces plain gray text
// ============================================================
@Composable
private fun ActivityPill(activity: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF1E0))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.DirectionsRun,
                contentDescription = null,
                tint = Color(0xFFFF7B00),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = activity.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF7B00)
            )
        }
    }
}

// ============================================================
// EMPTY / ERROR STATE — icon in a soft circle, title, subtitle
// ============================================================
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF7B00).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFF7B00),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}