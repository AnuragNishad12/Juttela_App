package com.example.juttela.Screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.juttela.R
import com.example.juttela.Utils.MeetupSessionPrefs
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private const val MAX_DISTANCE_METERS = 3000.0

data class PlaceHint(
    val name: String,
    val point: Point
)

@Composable
fun ActivityLocationMapScreen(
    navController: NavController,
    sessionId: String? = null
) {
    val context = LocalContext.current
    val orange = Color(0xFFFF7B00)
    val blue = Color(0xFF1E88E5)
    val green = Color(0xFF2E7D32)
    val token = stringResource(id = R.string.mapbox_access_token)
    val isLiveMode = !sessionId.isNullOrBlank()

    val meetup = remember(sessionId) {
        sessionId?.let { MeetupSessionPrefs.getBySessionId(context, it) }
    }

    var userPoint by remember { mutableStateOf<Point?>(null) }
    var selectedPoint by remember {
        mutableStateOf(
            meetup?.let { Point.fromLngLat(it.pinLng, it.pinLat) }
        )
    }
    var otherPoint by remember {
        mutableStateOf(
            if (meetup?.otherLat != null && meetup.otherLng != null) {
                Point.fromLngLat(meetup.otherLng, meetup.otherLat)
            } else null
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PlaceHint>>(emptyList()) }
    var followMe by remember { mutableStateOf(!isLiveMode) }

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(sessionId) {
        val saved = sessionId?.let { MeetupSessionPrefs.getBySessionId(context, it) } ?: return@LaunchedEffect
        selectedPoint = Point.fromLngLat(saved.pinLng, saved.pinLat)
        if (saved.otherLat != null && saved.otherLng != null) {
            otherPoint = Point.fromLngLat(saved.otherLng, saved.otherLat)
        }
    }

    LaunchedEffect(query, userPoint, isLiveMode) {
        if (isLiveMode) {
            results = emptyList()
            return@LaunchedEffect
        }
        val origin = userPoint
        if (query.trim().length < 3 || origin == null) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(350)
        results = searchNearby(query.trim(), origin, token)
            .filter { distanceMeters(origin, it.point) <= MAX_DISTANCE_METERS }
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(13.5)
            center(
                selectedPoint ?: Point.fromLngLat(77.2090, 28.6139)
            )
        }
    }

    fun trySelect(point: Point) {
        if (isLiveMode) return
        val origin = userPoint
        if (origin == null) {
            errorText = "Wait for your location first"
            return
        }
        val meters = distanceMeters(origin, point)
        if (meters > MAX_DISTANCE_METERS) {
            selectedPoint = null
            errorText = "Pin must be within 3 km of your location (${"%.1f".format(meters / 1000)} km away)"
        } else {
            selectedPoint = point
            errorText = null
            results = emptyList()
            followMe = false
        }
    }

    val latestSelect by rememberUpdatedState({ point: Point -> trySelect(point) })

    LaunchedEffect(userPoint, selectedPoint, otherPoint, followMe, isLiveMode) {
        if (followMe) return@LaunchedEffect

        val points = listOfNotNull(userPoint, selectedPoint, otherPoint)
        if (points.isEmpty()) return@LaunchedEffect

        val mid = Point.fromLngLat(
            points.map { it.longitude() }.average(),
            points.map { it.latitude() }.average()
        )
        val farthest = points.maxOf { distanceMeters(mid, it) }
        val zoom = when {
            farthest < 150 -> 16.0
            farthest < 400 -> 15.0
            farthest < 1200 -> 14.0
            else -> 13.2
        }
        mapViewportState.setCameraOptions {
            center(mid)
            this.zoom(zoom)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp)
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    text = if (isLiveMode) "Live meetup" else "Send activity location",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Text(
                    text = if (isLiveMode) {
                        "Blue = you   Green = other   Orange = target"
                    } else {
                        "Blue = you   Orange = target pin"
                    },
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        if (!isLiveMode) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search a place within 3 km") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = orange,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            if (results.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    items(results) { place ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = place.name
                                    trySelect(place.point)
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(place.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            val origin = userPoint
                            if (origin != null) {
                                Text(
                                    "${"%.0f".format(distanceMeters(origin, place.point))} m away",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                onMapClickListener = OnMapClickListener { point ->
                    if (!isLiveMode) latestSelect(point)
                    true
                }
            ) {
                if (locationGranted) {
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            enabled = true
                            locationPuck = createDefault2DPuck(withBearing = true)
                            puckBearingEnabled = true
                            puckBearing = PuckBearing.HEADING
                        }

                        val listener = OnIndicatorPositionChangedListener { point ->
                            val firstFix = userPoint == null
                            userPoint = point
                            if (firstFix && followMe) {
                                mapViewportState.transitionToFollowPuckState()
                            }
                        }
                        mapView.location.addOnIndicatorPositionChangedListener(listener)

                        if (!isLiveMode) {
                            mapView.gestures.addOnMapClickListener { point ->
                                latestSelect(point)
                                true
                            }
                        }
                    }
                }

                userPoint?.let { origin ->
                    if (!isLiveMode) {
                        PolygonAnnotation(points = listOf(createCirclePoints(origin, MAX_DISTANCE_METERS))) {
                            fillColor = Color(0x22FF7B00)
                            fillOutlineColor = orange
                        }
                    }

                    CircleAnnotation(point = origin) {
                        circleRadius = 18.0
                        circleColor = Color(0x331E88E5)
                        circleStrokeWidth = 0.0
                    }
                    CircleAnnotation(point = origin) {
                        circleRadius = 8.0
                        circleColor = blue
                        circleStrokeWidth = 3.0
                        circleStrokeColor = Color.White
                    }
                }

                otherPoint?.let { other ->
                    CircleAnnotation(point = other) {
                        circleRadius = 18.0
                        circleColor = Color(0x332E7D32)
                        circleStrokeWidth = 0.0
                    }
                    CircleAnnotation(point = other) {
                        circleRadius = 8.0
                        circleColor = green
                        circleStrokeWidth = 3.0
                        circleStrokeColor = Color.White
                    }
                }

                selectedPoint?.let { pin ->
                    CircleAnnotation(point = pin) {
                        circleRadius = 28.0
                        circleColor = Color(0x33FF7B00)
                        circleStrokeWidth = 0.0
                    }
                    CircleAnnotation(point = pin) {
                        circleRadius = 12.0
                        circleColor = orange
                        circleStrokeWidth = 4.0
                        circleStrokeColor = Color.White
                    }
                }

                val me = userPoint
                val pin = selectedPoint
                val other = otherPoint
                if (me != null && pin != null) {
                    PolylineAnnotation(points = listOf(me, pin)) {
                        lineColor = blue
                        lineWidth = 3.0
                    }
                }
                if (other != null && pin != null) {
                    PolylineAnnotation(points = listOf(other, pin)) {
                        lineColor = green
                        lineWidth = 3.0
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (locationGranted) {
                        followMe = true
                        mapViewportState.transitionToFollowPuckState()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color.White
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "My location", tint = orange)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            val me = userPoint
            val pin = selectedPoint
            val other = otherPoint

            Text(
                text = if (me == null) {
                    "You: waiting for GPS…"
                } else {
                    "You: ${"%.5f".format(me.latitude())}, ${"%.5f".format(me.longitude())}"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = blue
            )

            if (isLiveMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (other == null) {
                        "Other: waiting for their live location"
                    } else {
                        "Other: ${"%.5f".format(other.latitude())}, ${"%.5f".format(other.longitude())}"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = green
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    errorText != null -> errorText!!
                    pin == null -> "Target pin: tap inside the 3 km circle"
                    else -> "Target: ${"%.5f".format(pin.latitude())}, ${"%.5f".format(pin.longitude())}"
                },
                fontSize = 13.sp,
                color = if (errorText != null) Color(0xFFC62828) else orange,
                fontWeight = FontWeight.SemiBold
            )

            if (me != null && pin != null && errorText == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You → target: ${"%.0f".format(distanceMeters(me, pin))} m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = orange
                )
            }

            if (!isLiveMode) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val p = selectedPoint ?: return@Button
                        val mePoint = userPoint
                        val handle = navController.previousBackStackEntry?.savedStateHandle
                        handle?.set("picked_lat", p.latitude())
                        handle?.set("picked_lng", p.longitude())
                        if (mePoint != null) {
                            handle?.set("my_lat", mePoint.latitude())
                            handle?.set("my_lng", mePoint.longitude())
                        }
                        navController.popBackStack()
                    },
                    enabled = selectedPoint != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Send this location", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun distanceMeters(a: Point, b: Point): Double {
    val earth = 6371000.0
    val dLat = Math.toRadians(b.latitude() - a.latitude())
    val dLng = Math.toRadians(b.longitude() - a.longitude())
    val lat1 = Math.toRadians(a.latitude())
    val lat2 = Math.toRadians(b.latitude())
    val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * earth * asin(min(1.0, sqrt(h)))
}

private fun createCirclePoints(center: Point, radiusMeters: Double, steps: Int = 64): List<Point> {
    val points = ArrayList<Point>(steps + 1)
    val lat = Math.toRadians(center.latitude())
    val lng = Math.toRadians(center.longitude())
    val angDist = radiusMeters / 6371000.0
    for (i in 0..steps) {
        val bearing = 2 * Math.PI * i / steps
        val destLat = asin(
            sin(lat) * cos(angDist) + cos(lat) * sin(angDist) * cos(bearing)
        )
        val destLng = lng + atan2(
            sin(bearing) * sin(angDist) * cos(lat),
            cos(angDist) - sin(lat) * sin(destLat)
        )
        points += Point.fromLngLat(Math.toDegrees(destLng), Math.toDegrees(destLat))
    }
    return points
}

private suspend fun searchNearby(query: String, origin: Point, token: String): List<PlaceHint> {
    return withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url =
                "https://api.mapbox.com/geocoding/v5/mapbox.places/$encoded.json" +
                        "?proximity=${origin.longitude()},${origin.latitude()}" +
                        "&limit=6&access_token=$token"
            val body = java.net.URL(url).readText()
            val features = JSONObject(body).optJSONArray("features") ?: return@withContext emptyList()
            buildList {
                for (i in 0 until features.length()) {
                    val f = features.getJSONObject(i)
                    val center = f.getJSONArray("center")
                    add(
                        PlaceHint(
                            name = f.optString("place_name"),
                            point = Point.fromLngLat(center.getDouble(0), center.getDouble(1))
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}