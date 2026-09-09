package com.example.juttela.Utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.juttela.DataSource.Models.LiveDeal
import kotlin.math.roundToInt

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "live-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot-alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE11D2E))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha))
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "LIVE",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

//@Composable
//fun LiveDealCard(
//    deal: LiveDeal,
//    onClick: () -> Unit,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current
//    val config = LocalConfiguration.current
//
//    val cardWidthPx = with(density) { 148.dp.toPx() }
//    val cardHeightPx = with(density) { 176.dp.toPx() }
//    val maxX = (with(density) { config.screenWidthDp.dp.toPx() } - cardWidthPx).coerceAtLeast(0f)
//    val maxY = (with(density) { config.screenHeightDp.dp.toPx() } - cardHeightPx).coerceAtLeast(0f)
//
//    var offsetX by remember { mutableFloatStateOf(with(density) { 16.dp.toPx() }) }
//    var offsetY by remember { mutableFloatStateOf(with(density) { 210.dp.toPx() }) }
//    var totalDrag by remember { mutableFloatStateOf(0f) }
//
//    Card(
//        modifier = modifier
//            .width(148.dp)
//            .height(176.dp)
//            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { totalDrag = 0f },
//                    onDragEnd = {
//                        if (totalDrag < 12f) {
//                            onClick()
//                        }
//                    },
//                    onDrag = { change, dragAmount ->
//                        change.consume()
//                        totalDrag += dragAmount.getDistance()
//                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
//                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
//                    }
//                )
//            },
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Box(Modifier.fillMaxSize()) {
//            AsyncImage(
//                model = deal.imageUrl,
//                contentDescription = deal.title,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//                    .align(Alignment.BottomCenter)
//                    .background(
//                        Brush.verticalGradient(
//                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
//                        )
//                    )
//            )
//
//            LiveBadge(
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(8.dp)
//            )
//
//            IconButton(
//                onClick = onDismiss,
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .size(28.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                    tint = Color.White,
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//
//            Column(
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(8.dp)
//            ) {
//                Text(
//                    text = deal.title,
//                    color = Color.White,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Spacer(Modifier.height(4.dp))
//                Text(
//                    text = deal.offerText,
//                    color = Color(0xFF111111),
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.Medium,
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(4.dp))
//                        .background(Color.White)
//                        .padding(horizontal = 6.dp, vertical = 2.dp)
//                )
//            }
//        }
//    }
//}