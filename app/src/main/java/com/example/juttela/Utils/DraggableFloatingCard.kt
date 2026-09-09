package com.example.juttela.Utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableFloatingCard(
    modifier: Modifier = Modifier,
    initialOffset: Offset = Offset(24f, 500f),
    snapToEdge: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    var offsetX by remember { mutableFloatStateOf(initialOffset.x) }
    var offsetY by remember { mutableFloatStateOf(initialOffset.y) }
    val snapX = remember { Animatable(initialOffset.x) }

    fun maxX(): Float = (parentSize.width - cardSize.width).coerceAtLeast(0).toFloat()
    fun maxY(): Float = (parentSize.height - cardSize.height).coerceAtLeast(0).toFloat()

    LaunchedEffect(parentSize, cardSize) {
        if (parentSize.width == 0 || cardSize.width == 0) return@LaunchedEffect
        offsetX = offsetX.coerceIn(0f, maxX())
        offsetY = offsetY.coerceIn(0f, maxY())
        snapX.snapTo(offsetX)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { parentSize = it }
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .align(Alignment.TopStart)
                .onSizeChanged { cardSize = it }
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(parentSize, cardSize) {
                    detectDragGestures(
                        onDragEnd = {
                            if (!snapToEdge || parentSize.width == 0 || cardSize.width == 0) return@detectDragGestures
                            scope.launch {
                                val target = if (offsetX < maxX() / 2f) 8f else (maxX() - 8f).coerceAtLeast(0f)
                                snapX.snapTo(offsetX)
                                snapX.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) { offsetX = value }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX())
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY())
                    }
                }
                .pointerInput(onClick) {
                    detectTapGestures(onTap = { onClick?.invoke() })
                }
        ) {
            // Note: onDismiss is no longer drawn here as an overlay — it now
            // lives *inside* the premium card layout itself (top-right corner
            // of the card content), so it never floats awkwardly outside
            // the card's rounded edge or gets clipped by shadows.
            content()
        }
    }
}

@Composable
fun ProSubscriptionCard(
    title: String = "Juttela Pro",
    subtitle: String = "Unlock smarter matches nearby",
    priceLabel: String = "Upgrade Now",
    benefits: List<String> = listOf(
        "Age & gender filters",
        "Priority smart match",
        "See who liked you"
    ),
    onDismiss: (() -> Unit)? = null,
    onUpgradeClick: (() -> Unit)? = null
) {
    // Subtle infinite shimmer sweep across the gold accent to sell "premium"
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val goldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF6D488), Color(0xFFD8A24A), Color(0xFFF6D488))
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color(0xFFD8A24A).copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1C1C22), Color(0xFF0B0B0E))
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD8A24A).copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.06f),
                        Color(0xFFD8A24A).copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Soft glow blob top-left for depth
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = (-24).dp, y = (-24).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD8A24A).copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: PRO chip (with shimmer) + dismiss, inside the card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            width = 0.8.dp,
                            brush = goldGradient,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF6D488),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PREMIUM",
                        color = Color(0xFFF6D488),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (onDismiss != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Benefits list with gold checkmarks
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                benefits.forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD8A24A).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color(0xFFF6D488),
                                modifier = Modifier.size(9.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = benefit,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CTA button — gold gradient with shimmer sweep
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD8A24A), Color(0xFFF6D488), Color(0xFFD8A24A)),
                            startX = shimmerOffset * 300f - 100f,
                            endX = shimmerOffset * 300f + 200f
                        )
                    )
                    .pointerInput(onUpgradeClick) {
                        detectTapGestures(onTap = { onUpgradeClick?.invoke() })
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF1C1C22),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = priceLabel,
                        color = Color(0xFF1C1C22),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableProSubscriptionCard(
    modifier: Modifier = Modifier,
    initialOffset: Offset = Offset(24f, 460f),
    snapToEdge: Boolean = false,
    title: String = "Juttela Pro",
    subtitle: String = "Unlock smarter matches nearby",
    priceLabel: String = "Upgrade Now",
    benefits: List<String> = listOf(
        "Age & gender filters",
        "Priority smart match",
    ),
    onDismiss: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    DraggableFloatingCard(
        modifier = modifier.fillMaxSize(),
        initialOffset = initialOffset,
        snapToEdge = snapToEdge,
        onDismiss = null, // dismiss lives inside ProSubscriptionCard now
        onClick = onClick
    ) {
        ProSubscriptionCard(
            title = title,
            subtitle = subtitle,
            priceLabel = priceLabel,
            benefits = benefits,
            onDismiss = onDismiss,
            onUpgradeClick = onClick
        )
    }
}