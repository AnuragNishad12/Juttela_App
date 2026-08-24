package com.example.juttela.Screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juttela.DataSource.Models.SmartGetRequest
import com.example.juttela.DataSource.Models.UserRequest
import com.example.juttela.Utils.SmartRequestCard
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.AcceptRequestViewModel
import com.example.juttela.ViewModels.GetSmartRequestViewModel
import com.example.juttela.ViewModels.RequestViewModel
import com.example.juttela.ViewModels.SubscriptionViewModel

@Composable
fun RequestScreen() {
    val context = LocalContext.current
    val requestViewModel: RequestViewModel = viewModel()
    val getSmartRequestViewModel: GetSmartRequestViewModel = viewModel()
    val acceptRequestViewModel: AcceptRequestViewModel = viewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()

    val isPro by subscriptionViewModel.isPro.collectAsState()
    val freeState = requestViewModel.getState
    val smartState = getSmartRequestViewModel.state

    var acceptingRequestId by remember { mutableStateOf<String?>(null) }

    fun refreshRequests() {
        val userId = UserPrefs.getUserId(context) ?: return
        if (isPro) {
            getSmartRequestViewModel.getSmartRequests(userId)
        } else {
            requestViewModel.getMyRequests(userId)
        }
    }

    // BUG FIX: this screen gets its own SubscriptionViewModel instance, separate from
    // HomeScreen's. Without this call, isPro stays at its default value forever and
    // the Pro branch below never runs — even for an actually-subscribed user.
    LaunchedEffect(Unit) {
        subscriptionViewModel.checkProStatus()
    }

    // Re-fetches whenever isPro changes: fires once with the initial value, then again
    // if checkProStatus() above updates it (e.g. false -> true after the check completes).
    LaunchedEffect(isPro) {
        val userId = UserPrefs.getUserId(context)
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return@LaunchedEffect
        }

        if (isPro) {
            getSmartRequestViewModel.getSmartRequests(userId)
        } else {
            requestViewModel.getMyRequests(userId)
        }
    }

    fun onAcceptClicked(requestId: String, senderName: String) {
        val currentUserId = UserPrefs.getUserId(context)
        val currentUserName = UserPrefs.getUserName(context)

        if (currentUserId == null || currentUserName == null) {
            Toast.makeText(context, "Something went wrong. Please sign in again.", Toast.LENGTH_LONG).show()
            return
        }

        acceptingRequestId = requestId

        acceptRequestViewModel.acceptRequest(
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            requestId = requestId
        ) { success, message, connection ->
            acceptingRequestId = null

            if (success && connection != null) {
                Toast.makeText(
                    context,
                    "You're now connected with ${connection.otherUserName}",
                    Toast.LENGTH_SHORT
                ).show()
                refreshRequests()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val loading = if (isPro) smartState.loading else freeState.loading
    val success = if (isPro) smartState.success else freeState.success
    val message = if (isPro) smartState.message else (freeState.message ?: "Something went wrong")
    val isEmpty = if (isPro) smartState.requests.isEmpty() else freeState.requests.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = if (isPro) "Pro Requests" else "Requests",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "People who want to join you",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF7B00), strokeWidth = 3.dp)
                }
            }

            !success -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message, color = Color.Gray, fontSize = 15.sp)
                }
            }

            isEmpty -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No requests yet", color = Color.Gray, fontSize = 16.sp)
                }
            }

            isPro -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(smartState.requests) { request ->
                        SmartRequestCard(
                            request = request,
                            isAccepting = acceptingRequestId == request.id,
                            onAccept = {
                                onAcceptClicked(request.id, request.senderName)
                            },
                            onReject = {
                                Toast.makeText(
                                    context,
                                    "Rejected ${request.senderName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(freeState.requests) { request ->
                        RequestCard(
                            request = request,
                            isAccepting = acceptingRequestId == request.id,
                            onAccept = { onAcceptClicked(request.id, request.senderName) },
                            onReject = {
                                Toast.makeText(
                                    context,
                                    "Rejected ${request.senderName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: UserRequest,
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
            Text(
                text = request.senderName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${request.activity.replaceFirstChar { it.uppercase() }}  •  ${formatDistance(request.distanceKm)}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = request.status.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (request.status.equals("pending", ignoreCase = true))
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
                    Text(
                        text = "Reject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        Text(
                            text = "Accept",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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