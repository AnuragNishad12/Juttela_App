package com.example.juttela.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.example.juttela.DataSource.Models.SessionSnapshotResponse
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.SessionSnapshotViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val SNAPSHOT_POLL_INTERVAL_MS = 30_000L

private val PIN_COLOR = Color(0xFFFF7B00)
private val ME_COLOR = Color(0xFF1E88E5)
private val ARRIVED_COLOR = Color(0xFF2E7D32)
private val CARD_BG = Color.White
private val MUTED = Color(0xFF6B7280)
private val LINE_COLOR = Color(0xFF1E88E5)

@Composable
fun LiveTrackingMapScreen(
    navController: NavController,
    sessionId: String
) {
    val context = LocalContext.current
    val currentUserId = remember { UserPrefs.getUserId(context) }
    val snapshotViewModel: SessionSnapshotViewModel = viewModel()

    var snapshot by remember { mutableStateOf<SessionSnapshotResponse?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isPolling by remember { mutableStateOf(true) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.5)
            center(Point.fromLngLat(77.2090, 28.6139))
        }
    }

    LaunchedEffect(sessionId) {
        while (isPolling) {
            snapshotViewModel.getSessionSnapshot(sessionId) { success, message, response ->
                if (success && response != null) {
                    snapshot = response
                    errorText = null
                } else {
                    errorText = message
                }
            }
            delay(SNAPSHOT_POLL_INTERVAL_MS)
        }
    }

    DisposableEffect(Unit) {
        onDispose { isPolling = false }
    }

    val me = snapshot?.users?.firstOrNull { it.userId == currentUserId }
    val pinPoint = snapshot?.let { Point.fromLngLat(it.pin.lng, it.pin.lat) }
    val myPoint = if (me?.lat != null && me.lng != null) {
        Point.fromLngLat(me.lng, me.lat)
    } else null

    val distanceMeters = when {
        me?.distanceMeters != null -> me.distanceMeters
        pinPoint != null && myPoint != null -> haversineMeters(
            myPoint.latitude(), myPoint.longitude(),
            pinPoint.latitude(), pinPoint.longitude()
        )
        else -> null
    }
    val arrived = me?.arrived == true

    LaunchedEffect(pinPoint, myPoint, distanceMeters) {
        val pin = pinPoint ?: return@LaunchedEffect
        val center = if (myPoint != null) {
            Point.fromLngLat(
                (pin.longitude() + myPoint.longitude()) / 2.0,
                (pin.latitude() + myPoint.latitude()) / 2.0
            )
        } else pin

        mapViewportState.setCameraOptions {
            center(center)
            zoom(zoomForDistance(distanceMeters ?: 400.0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(Color(0xFFF6F7F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text("On the way", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text("Your live location → meetup pin", color = MUTED, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState
            ) {
                if (pinPoint != null) {
                    // target halo + pin
                    CircleAnnotation(point = pinPoint) {
                        circleRadius = 18.0
                        circleColor = PIN_COLOR.copy(alpha = 0.18f)
                        circleStrokeWidth = 0.0
                    }
                    CircleAnnotation(point = pinPoint) {
                        circleRadius = 9.0
                        circleColor = PIN_COLOR
                        circleStrokeWidth = 3.0
                        circleStrokeColor = Color.White
                    }

                    if (myPoint != null) {
                        PolylineAnnotation(points = listOf(myPoint, pinPoint)) {
                            lineColor = LINE_COLOR
                            lineWidth = 3.5
                        }

                        CircleAnnotation(point = myPoint) {
                            circleRadius = 16.0
                            circleColor = ME_COLOR.copy(alpha = 0.20f)
                            circleStrokeWidth = 0.0
                        }
                        CircleAnnotation(point = myPoint) {
                            circleRadius = 8.0
                            circleColor = ME_COLOR
                            circleStrokeWidth = 3.0
                            circleStrokeColor = Color.White
                        }
                    }
                }
            }

            if (snapshot == null && errorText == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PIN_COLOR)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CARD_BG)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                if (errorText != null) {
                    Text(errorText ?: "", fontSize = 13.sp, color = Color(0xFFC62828))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        icon = { Icon(Icons.Filled.MyLocation, null, tint = ME_COLOR, modifier = Modifier.size(14.dp)) },
                        text = "You",
                        tint = ME_COLOR
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("to", color = MUTED, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(
                        icon = { Icon(Icons.Filled.Flag, null, tint = PIN_COLOR, modifier = Modifier.size(14.dp)) },
                        text = "Meetup pin",
                        tint = PIN_COLOR
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when {
                        arrived -> "You’ve arrived"
                        distanceMeters != null -> formatDistance(distanceMeters)
                        else -> "Getting your location…"
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (arrived) ARRIVED_COLOR else Color(0xFF111827)
                )

                Text(
                    text = when {
                        arrived -> "You’re at the meetup point"
                        distanceMeters != null -> "straight-line distance to the pin"
                        else -> "Waiting for a GPS fix"
                    },
                    fontSize = 13.sp,
                    color = MUTED
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (arrived) Color(0xFFE8F5E9) else Color(0xFFFFF4EA))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (arrived) "Target reached" else "Target is the orange pin. Other people in this session are hidden.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (arrived) ARRIVED_COLOR else Color(0xFF9A4B00)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    icon: @Composable () -> Unit,
    text: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000.0)
    } else {
        "${meters.roundToInt()} m"
    }
}

private fun zoomForDistance(meters: Double): Double {
    return when {
        meters < 80 -> 17.2
        meters < 200 -> 16.2
        meters < 500 -> 15.2
        meters < 1200 -> 14.2
        meters < 3000 -> 13.2
        else -> 12.4
    }
}

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earth = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
    return 2 * earth * kotlin.math.asin(kotlin.math.sqrt(a))
}