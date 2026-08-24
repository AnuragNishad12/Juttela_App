package com.example.juttela.Screens

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.navigation.NavHostController
import com.example.juttela.DataSource.Models.Message
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.GetConversationViewModel
import com.example.juttela.ViewModels.SendMessageViewModel

@Composable
fun ChatConversationScreen(
    navController: NavHostController,
    otherUserId: String,
    otherUserName: String
) {
    val context = LocalContext.current
    val getConversationViewModel: GetConversationViewModel = viewModel()
    val sendMessageViewModel: SendMessageViewModel = viewModel()

    val currentUserId = remember { UserPrefs.getUserId(context) }
    val currentUserName = remember { UserPrefs.getUserName(context) } // needed so the backend can show "X sent you a message"

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val state = getConversationViewModel.state

    DisposableEffect(Unit) {
        if (currentUserId != null) {
            getConversationViewModel.getConversation(currentUserId, otherUserId, showLoading = true)
            getConversationViewModel.startPolling(currentUserId, otherUserId)
        }
        onDispose {
            getConversationViewModel.stopPolling()
        }
    }

    // Auto-scroll to bottom whenever new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    fun onSendClicked() {
        val textToSend = inputText.trim()
        if (textToSend.isEmpty() || currentUserId == null || currentUserName == null) return

        inputText = ""

        sendMessageViewModel.sendMessage(
            senderId = currentUserId,
            senderName = currentUserName,
            receiverId = otherUserId,
            text = textToSend
        ) { success, _, _ ->
            if (success) {
                getConversationViewModel.getConversation(currentUserId, otherUserId, showLoading = false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(Color(0xFFF8F8F8))
    ) {
        // ==================== HEADER ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, spotColor = Color.Black.copy(alpha = 0.08f))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            InitialsAvatar(name = otherUserName, size = 42.dp)

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = otherUserName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "Tap to view profile",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // ==================== MESSAGES ====================
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading && state.messages.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF7B00), strokeWidth = 3.dp)
                    }
                }

                state.messages.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF7B00).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFFFF7B00),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Say hi to $otherUserName!",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start planning your activity together",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.messages) { msg ->
                            MessageBubble(
                                message = msg,
                                isMine = msg.senderId == currentUserId
                            )
                        }
                    }
                }
            }
        }

        // ==================== INPUT BAR ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, spotColor = Color.Black.copy(alpha = 0.08f))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF7B00),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedContainerColor = Color(0xFFF8F8F8)
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isBlank()) Color(0xFFE0E0E0) else Color(0xFFFF7B00)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { onSendClicked() }, enabled = inputText.isNotBlank()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// MESSAGE BUBBLE — width-capped, subtle shadow, incoming bubbles get
// a light border so they read clearly against the gray background.
// ============================================================
@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    ),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(if (isMine) Color(0xFFFF7B00) else Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (isMine) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ============================================================
// INITIALS AVATAR — colored circle with the first letter of the name,
// color picked deterministically per person.
// ============================================================
@Composable
private fun InitialsAvatar(name: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
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
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}