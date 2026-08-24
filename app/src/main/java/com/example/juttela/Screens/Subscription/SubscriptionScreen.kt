package com.example.juttela.Screens.Subscription

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import com.revenuecat.purchases.Package

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
            Toast.makeText(context, "Purchase successful! Welcome to Juttela Pro 🎉", Toast.LENGTH_LONG).show()
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
            .background(Color(0xFFF8F8F8))
    ) {
        when {
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Something went wrong",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = error ?: "",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            offerings == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF7B00))
                }
            }

            else -> {
                val currentOffering = offerings?.current
                val packages = currentOffering?.availablePackages.orEmpty()

                // Auto-select the first package once loaded, so the CTA is never disabled by default
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
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    TextButton(onClick = onClose) {
                        Text("Close", color = Color.Gray, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ===== Header =====
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF7B00), Color(0xFFFF9A3D))
                                )
                            )
                            .padding(vertical = 26.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Juttela Pro",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Never miss a mate again",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== Benefits =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(vertical = 6.dp)
                    ) {
                        BenefitRow(icon = Icons.Default.Bolt, text = "Smart matches tailored to you")
                        BenefitRow(icon = Icons.Default.Chat, text = "Unlimited chats with your connections")
                        BenefitRow(icon = Icons.Default.Send, text = "Send unlimited activity requests")
                        BenefitRow(icon = Icons.Default.People, text = "See unlimited people nearby")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Choose your plan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ===== Package selection cards =====
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        packages.forEach { pkg ->
                            PackageCard(
                                pkg = pkg,
                                isSelected = selectedPackage?.identifier == pkg.identifier,
                                onClick = { selectedPackage = pkg }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ===== Bottom CTA, pinned =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 22.dp, vertical = 16.dp)
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
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7B00),
                            contentColor = Color.White
                        )
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Cancel anytime. Renews automatically.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF1E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFF7B00),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun PackageCard(
    pkg: Package,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFFFF1E0) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFF7B00) else Color(0xFFE5E5E5),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pkg.product.price.formatted,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFFFF7B00),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}