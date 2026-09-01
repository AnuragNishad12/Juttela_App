package com.example.juttela.Screens.Subscription

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import com.revenuecat.purchases.Package

private val Ink = Color(0xFF111111)
private val Muted = Color(0xFF6B6B6B)
private val Soft = Color(0xFF9A9A9A)
private val Line = Color(0xFFE6E6E6)
private val Wash = Color(0xFFF5F5F5)
private val Paper = Color(0xFFFFFFFF)
private val Page = Color(0xFFFAFAFA)

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onClose: () -> Unit = {}
) {
    val offerings by viewModel.offerings.collectAsState()
    val error by viewModel.error.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedPackage by remember { mutableStateOf<Package?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadOfferings()
    }

    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess) {
            Toast.makeText(context, "Purchase successful! Welcome to Juttela Pro", Toast.LENGTH_LONG).show()
            onClose()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "Purchase failed: $error", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Page)
    ) {
        when {
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "Something went wrong",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = error ?: "",
                            fontSize = 12.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            offerings == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Ink,
                        strokeWidth = 1.6.dp,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            else -> {
                val currentOffering = offerings?.current
                val packages = currentOffering?.availablePackages.orEmpty()

                LaunchedEffect(packages) {
                    if (selectedPackage == null && packages.isNotEmpty()) {
                        selectedPackage = packages.first()
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.padding(start = 0.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = Soft,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "MEMBERSHIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Soft,
                        letterSpacing = 2.4.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Juttela Pro",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        letterSpacing = (-0.4).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Never miss a mate again",
                        fontSize = 13.sp,
                        color = Muted,
                        letterSpacing = 0.1.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Paper)
                            .border(1.dp, Line, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        BenefitRow(icon = Icons.Outlined.Bolt, text = "Smart matches tailored to you")
                        Hairline()
                        BenefitRow(icon = Icons.Outlined.Chat, text = "Unlimited chats with your connections")
                        Hairline()
                        BenefitRow(icon = Icons.Outlined.Send, text = "Send unlimited activity requests")
                        Hairline()
                        BenefitRow(icon = Icons.Outlined.People, text = "See unlimited people nearby")
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Choose your plan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        packages.forEach { pkg ->
                            PackageCard(
                                pkg = pkg,
                                isSelected = selectedPackage?.identifier == pkg.identifier,
                                onClick = { selectedPackage = pkg }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Paper)
                        .border(width = 1.dp, color = Line)
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
                            selectedPackage?.let { pkg ->
                                activity?.let { act ->
                                    viewModel.purchasePackage(activity = act, packageToPurchase = pkg)
                                }
                            }
                        },
                        enabled = selectedPackage != null && !isPurchasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ink,
                            contentColor = Paper,
                            disabledContainerColor = Color(0xFFD4D4D4),
                            disabledContentColor = Paper
                        )
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                color = Paper,
                                strokeWidth = 1.8.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Continue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cancel anytime. Renews automatically.",
                        fontSize = 10.sp,
                        color = Soft,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun Hairline() {
    HorizontalDivider(thickness = 0.6.dp, color = Line)
}

@Composable
private fun BenefitRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(1.dp, Line, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Ink,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun PackageCard(
    pkg: Package,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Ink else Line,
        label = "planBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Wash else Paper,
        label = "planBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = pkg.product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = pkg.product.price.formatted,
                    fontSize = 11.sp,
                    color = Muted,
                    letterSpacing = 0.1.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}