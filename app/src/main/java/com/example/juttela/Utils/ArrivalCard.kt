package com.example.juttela.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.juttela.DataSource.Models.Arrival
import java.text.SimpleDateFormat
import java.util.*

private val JuttelaOrange = Color(0xFFFF7A1A)
private val CardBg = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF9A9A9A)
private val RateBarBg = Color(0xFFF4F4F4)
private val StarEmpty = Color(0xFFD0D0D0)

private val RatingLabels = listOf("", "Terrible", "Bad", "Okay", "Good", "Awesome")

@Composable
fun ArrivalCard(item: Arrival) {
    val context = LocalContext.current
    val key = reviewKey(item)

    var rating by rememberSaveable(key) {
        mutableIntStateOf(ArrivalReviewStore.getRating(context, key))
    }
    var feedback by rememberSaveable(key) {
        mutableStateOf(ArrivalReviewStore.getFeedback(context, key))
    }
    var showDialog by remember { mutableStateOf(false) }
    var dialogPresetRating by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    dialogPresetRating = rating
                    showDialog = true
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.sender.profileImageUrl,
                contentDescription = item.sender.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.sender.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildString {
                        item.sender.age?.let { append("$it yrs") }
                        if (item.sender.gender.isNotBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(item.sender.gender.replaceFirstChar { it.uppercase() })
                        }
                    }.ifBlank { "—" },
                    fontSize = 13.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = TextGray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        RateReviewBar(
            rating = rating,
            hasFeedback = feedback.isNotBlank(),
            onStarClick = { star ->
                dialogPresetRating = star
                showDialog = true
            },
            onWriteReview = {
                dialogPresetRating = rating
                showDialog = true
            }
        )
    }

    if (showDialog) {
        ArrivalReviewDialog(
            item = item,
            initialRating = if (dialogPresetRating > 0) dialogPresetRating else rating,
            initialFeedback = feedback,
            onDismiss = { showDialog = false },
            onSave = { newRating, newFeedback ->
                ArrivalReviewStore.save(context, key, newRating, newFeedback)
                rating = newRating
                feedback = newFeedback
                showDialog = false
                Toast.makeText(context, "Review saved", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun RateReviewBar(
    rating: Int,
    hasFeedback: Boolean,
    onStarClick: (Int) -> Unit,
    onWriteReview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RateBarBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rating > 0) {
            Text(
                text = RatingLabels.getOrElse(rating) { "Rated" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < rating) JuttelaOrange else StarEmpty,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onStarClick(index + 1) }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onWriteReview,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JuttelaOrange),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(JuttelaOrange)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hasFeedback) "Edit review" else "Write review",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = "Rate & Review",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.weight(1f))
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "${index + 1} star",
                        tint = StarEmpty,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onStarClick(index + 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArrivalReviewDialog(
    item: Arrival,
    initialRating: Int,
    initialFeedback: String,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(initialRating) }
    var feedback by remember { mutableStateOf(initialFeedback) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = item.sender.profileImageUrl,
                        contentDescription = item.sender.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.sender.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildString {
                                item.sender.age?.let { append("$it yrs") }
                                if (item.sender.gender.isNotBlank()) {
                                    if (isNotEmpty()) append(" · ")
                                    append(item.sender.gender.replaceFirstChar { it.uppercase() })
                                }
                            }.ifBlank { "—" },
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailLine(label = "Arrived", value = formatArrivedAt(item.arrivedAtIso))

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pin: ${item.pin.lat}, ${item.pin.lng}",
                    fontSize = 13.sp,
                    color = TextDark,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        openInMaps(context, item.pin.lat, item.pin.lng, "Pin location")
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Arrival: ${item.arrival.lat}, ${item.arrival.lng}",
                    fontSize = 13.sp,
                    color = TextDark,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        openInMaps(context, item.arrival.lat, item.arrival.lng, "Arrival location")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rate Your Experience",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        val value = index + 1
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$value star",
                            tint = if (value <= rating) JuttelaOrange else StarEmpty,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = value }
                        )
                    }
                    if (rating > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = RatingLabels[rating],
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JuttelaOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Write a review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("How was Your Experience?", fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JuttelaOrange,
                        cursorColor = JuttelaOrange
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (rating == 0 && feedback.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Add a rating or feedback",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            onSave(rating, feedback.trim())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JuttelaOrange)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextGray)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}

private fun reviewKey(item: Arrival): String {
    return "${item.sender.name}_${item.arrivedAtIso}_${item.pin.lat}_${item.pin.lng}"
}

private object ArrivalReviewStore {
    private const val PREF = "arrival_reviews"

    fun getRating(context: Context, key: String): Int =
        prefs(context).getInt("${key}_rating", 0)

    fun getFeedback(context: Context, key: String): String =
        prefs(context).getString("${key}_feedback", "") ?: ""

    fun save(context: Context, key: String, rating: Int, feedback: String) {
        prefs(context).edit()
            .putInt("${key}_rating", rating)
            .putString("${key}_feedback", feedback)
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
}

private fun openInMaps(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show()
    }
}

private fun formatArrivedAt(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(iso)
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        formatter.format(date!!)
    } catch (e: Exception) {
        iso
    }
}