package com.example.juttela.Utils


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

// ---------- Ticket shape with side notches at the tear line ----------
private class TicketShape(
    private val corner: Dp = 24.dp,
    private val notchRadius: Dp = 11.dp,
    private val notchY: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { corner.toPx() }
        val r = with(density) { notchRadius.toPx() }
        val base = Path().apply { addRoundRect(RoundRect(size.toRect(), CornerRadius(c))) }
        if (notchY <= 0f) return Outline.Generic(base)
        val left = Path().apply { addOval(Rect(Offset(0f, notchY), r)) }
        val right = Path().apply { addOval(Rect(Offset(size.width, notchY), r)) }
        val step1 = Path.combine(PathOperation.Difference, base, left)
        return Outline.Generic(Path.combine(PathOperation.Difference, step1, right))
    }
}

private data class PassStyle(
    val colors: List<Color>,
    val accent: Color,
    val icon: ImageVector,
    val tag: String,
    val title: String,
    val subtitle: String,
    val button: String?,
    val step: Int            // 0 = none, 1 = submitted, 2 = in review, 3 = verified
)

private fun styleFor(status: ApprovalPrefs.VerificationStatus) = when (status) {
    ApprovalPrefs.VerificationStatus.NOT_VERIFIED -> PassStyle(
        listOf(Color(0xFF1F1B2E), Color(0xFF3B2A4F)), Color(0xFFE53935), Icons.Default.Shield,
        "IDENTITY PASS", "Become a Verified User",
        "Get the trust badge, stand out in nearby results and build safer connections.",
        "Start Verification", 0
    )
    ApprovalPrefs.VerificationStatus.PENDING -> PassStyle(
        listOf(Color(0xFF1B2A3A), Color(0xFF244B63)), Color(0xFF4FC3F7), Icons.Default.HourglassTop,
        "IN REVIEW", "Verification Pending",
        "We're checking your details. This usually takes a short while — you'll be notified.",
        null, 2
    )
    ApprovalPrefs.VerificationStatus.APPROVED -> PassStyle(
        listOf(Color(0xFF3A2A06), Color(0xFF7A5A12)), Color(0xFFE53935), Icons.Default.Verified,
        "VERIFIED", "You're Verified",
        "Your identity is confirmed. The badge is now visible on your profile.",
        null, 3
    )
    ApprovalPrefs.VerificationStatus.REJECTED -> PassStyle(
        listOf(Color(0xFF3A1414), Color(0xFF5E1F26)), Color(0xFFFF6B6B), Icons.Default.ErrorOutline,
        "ACTION NEEDED", "Verification Rejected",
        "We couldn't confirm your details. Please resubmit with clearer information.",
        "Try Again", 1
    )
}

@Composable
fun VerificationPassCard(
    status: ApprovalPrefs.VerificationStatus,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = styleFor(status)
    var topHeightPx by remember { mutableStateOf(0) }
    val shape = remember(topHeightPx) { TicketShape(notchY = topHeightPx.toFloat()) }

    // shimmer sweep (used for APPROVED + subtle on others)
    val infinite = rememberInfiniteTransition(label = "pass")
    val shimmer by infinite.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "shimmer"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, spotColor = s.accent.copy(alpha = 0.45f))
            .clip(shape)
            .background(Brush.linearGradient(s.colors))
            .then(
                if (status == ApprovalPrefs.VerificationStatus.APPROVED || status == ApprovalPrefs.VerificationStatus.NOT_VERIFIED)
                    Modifier.background(
                        Brush.linearGradient(
                            0f + shimmer * 0.5f to Color.Transparent,
                            0.25f + shimmer * 0.5f to Color.White.copy(alpha = 0.10f),
                            0.5f + shimmer * 0.5f to Color.Transparent,
                            start = Offset.Zero, end = Offset(900f, 400f)
                        )
                    ) else Modifier
            )
    ) {
        // ===== TOP: badge + text =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { topHeightPx = it.height }
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PassBadge(style = s, spinning = status == ApprovalPrefs.VerificationStatus.PENDING)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    s.tag, color = s.accent, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(s.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(s.subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        // ===== Dashed tear line =====
        Canvas(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp)) {
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(0f, 0f), end = Offset(size.width, 0f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
            )
        }

        // ===== BOTTOM: stepper + action =====
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            VerificationStepper(current = s.step, accent = s.accent, rejected = status == ApprovalPrefs.VerificationStatus.REJECTED)

            if (s.button != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(s.accent, s.accent.copy(alpha = 0.75f))))
                        .clickable(onClick = onVerifyClick),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.button, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------- Badge with rotating sweep ring ----------
@Composable
private fun PassBadge(style: PassStyle, spinning: Boolean) {
    val t = rememberInfiniteTransition(label = "badge")
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "a")
    val pulse by t.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "p"
    )

    Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().rotate(if (spinning) angle else 0f)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, style.accent, Color.Transparent, style.accent.copy(alpha = 0.2f))
                ),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Box(
            Modifier
                .size(46.dp)
                .then(if (!spinning) Modifier.rotate(0f) else Modifier)
                .background(style.accent.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                style.icon, null, tint = style.accent,
                modifier = Modifier.size((24 * (if (spinning) 1f else pulse)).dp)
            )
        }
    }
}

// ---------- 3-step tracker ----------
@Composable
private fun VerificationStepper(current: Int, accent: Color, rejected: Boolean) {
    val labels = listOf("Submit", "Review", "Verified")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { i, label ->
            val done = current > i
            val active = current == i + 1
            val dotColor = when {
                rejected && i == 1 -> Color(0xFFFF6B6B)
                done || active -> accent
                else -> Color.White.copy(alpha = 0.2f)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(if (active) 16.dp else 12.dp)
                        .background(dotColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (done && !active || (current == 3 && i == 2))
                        Icon(Icons.Default.Check, null, tint = Color.Black.copy(0.7f), modifier = Modifier.size(9.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label, fontSize = 10.sp,
                    color = Color.White.copy(alpha = if (done || active) 0.9f else 0.4f),
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (i < labels.lastIndex) {
                Box(
                    Modifier.weight(1f).height(2.dp).padding(horizontal = 6.dp).offset(y = (-7).dp)
                        .background(if (current > i + 1) accent else Color.White.copy(alpha = 0.15f))
                )
            }
        }
    }
}