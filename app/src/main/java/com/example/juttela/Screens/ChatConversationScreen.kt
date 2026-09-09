package com.example.juttela.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.juttela.DataSource.Models.Message
import com.example.juttela.R
import com.example.juttela.Services.LocationTrackingService
import com.example.juttela.Utils.DailyChatLimitStore
import com.example.juttela.Utils.MeetupSessionPrefs
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.ViewModels.AddUserToSessionViewModel
import com.example.juttela.ViewModels.CancelSessionViewModel
import com.example.juttela.ViewModels.GetConversationViewModel
import com.example.juttela.ViewModels.SendMessageViewModel
import com.example.juttela.ViewModels.SessionStartViewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatConversationScreen(
    navController: NavHostController,
    otherUserId: String,
    otherUserName: String,
    activity: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token = stringResource(id = R.string.mapbox_access_token)
    val getConversationViewModel: GetConversationViewModel = viewModel()
    val sendMessageViewModel: SendMessageViewModel = viewModel()
    val sessionStartViewModel: SessionStartViewModel = viewModel()
    val addUserToSessionViewModel: AddUserToSessionViewModel = viewModel()

    val currentUserId = remember { UserPrefs.getUserId(context) }
    val currentUserName = remember { UserPrefs.getUserName(context) }

    var inputText by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf<String?>(null) }
    val chatFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var showMeetupDialog by remember { mutableStateOf(false) }
    var pinnedLat by remember { mutableStateOf<Double?>(null) }
    var pinnedLng by remember { mutableStateOf<Double?>(null) }
    var myLat by remember { mutableStateOf<Double?>(null) }
    var myLng by remember { mutableStateOf<Double?>(null) }
    var pinnedAddress by remember { mutableStateOf<String?>(null) }
    val cancelSessionViewModel: CancelSessionViewModel = viewModel()

    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val isPro by subscriptionViewModel.isPro.collectAsState()

    val chatLimitStore = remember { DailyChatLimitStore(context) }
    var remainingMessages by remember { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mutableIntStateOf(chatLimitStore.remainingMessages())
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    }
    var remainingLocations by remember { mutableIntStateOf(chatLimitStore.remainingLocations()) }

    // active session tracking state — restored from local prefs per
    // conversation, so it persists across app restarts on THIS device
    var activeSessionId by remember {
        mutableStateOf(MeetupSessionPrefs.getSessionIdForUser(context, otherUserId))
    }
    var pendingSession by remember { mutableStateOf<Pair<String, String>?>(null) }

    // ---------------------------------------------------------------
    // Blocking progress state for the "accept activity location" flow:
    // create session -> attach both users -> send confirmation ->
    // request location perms -> start tracking service. Instead of a
    // burst of toasts along the way, we show one full-screen spinner
    // from the moment the user taps Accept until tracking is actually
    // live (or the flow fails/needs a permission decision).
    // ---------------------------------------------------------------
    var isProcessingMeetup by remember { mutableStateOf(false) }

    // sessionIds we've already auto-started tracking for on this device,
    // so we don't re-trigger the permission flow every time the message
    // list refreshes and we see the same confirmation message again
    val handledSessionIds = remember { mutableStateOf(setOf<String>()) }
    var activityName by remember(activity) { mutableStateOf(activity) }

    val state = getConversationViewModel.state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    LaunchedEffect(state.messages) {
        val latestCancellation = state.messages
            .asReversed()
            .firstNotNullOfOrNull { msg -> parseSessionCancelledMessage(msg.text) }

        if (latestCancellation != null && latestCancellation == activeSessionId) {
            LocationTrackingService.stop(context)
            MeetupSessionPrefs.markCancelled(context, latestCancellation)
            MeetupSessionPrefs.clear(context)
            activeSessionId = null
            showToast("The other person cancelled the meetup")
        }
    }

    fun startTrackingService(sessionId: String, userId: String) {
        LocationTrackingService.start(context, sessionId, userId)
        activeSessionId = sessionId
        MeetupSessionPrefs.save(context, sessionId, otherUserId)
        // Flow is complete — tracking is live, dismiss the progress overlay.
        isProcessingMeetup = false
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pending = pendingSession
        pendingSession = null
        if (granted && pending != null) {
            startTrackingService(pending.first, pending.second)
        } else {
            isProcessingMeetup = false
            showToast("Background location denied — cannot track this meetup")
            Log.e("Chat", "Background location permission denied — cannot track in background")
        }
    }

    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val pending = pendingSession

        if (!fineGranted || pending == null) {
            isProcessingMeetup = false
            showToast("Location permission denied")
            Log.e("Chat", "Foreground location permission denied")
            pendingSession = null
            return@rememberLauncherForActivityResult
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bgAlreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (bgAlreadyGranted) {
                startTrackingService(pending.first, pending.second)
                pendingSession = null
            } else {
                // Still waiting on the background-location decision —
                // keep the overlay up, backgroundPermissionLauncher's
                // callback above will resolve it either way.
                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else {
            startTrackingService(pending.first, pending.second)
            pendingSession = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requireFreeMessageSlot(): Boolean {
        if (isPro) return true
        if (chatLimitStore.canSendMessage()) return true
        showToast("Daily message limit reached. Upgrade to Pro for unlimited chat.")
        navController.navigate("subscription")
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requireFreeLocationSlot(): Boolean {
        if (isPro) return true
        if (chatLimitStore.canSendLocation()) return true
        showToast("Daily location limit reached. Upgrade to Pro to share more pins.")
        navController.navigate("subscription")
        return false
    }

    fun requestLocationPermissionsAndStart(sessionId: String, userId: String) {
        pendingSession = sessionId to userId

        val fineAlreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineAlreadyGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bgAlreadyGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (bgAlreadyGranted) {
                    startTrackingService(sessionId, userId)
                    pendingSession = null
                } else {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                startTrackingService(sessionId, userId)
                pendingSession = null
            }
        } else {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            foregroundLocationLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun cancelActiveSession() {
        val sessionId = activeSessionId
        LocationTrackingService.stop(context)
        activeSessionId = null

        if (!sessionId.isNullOrBlank()) {
            MeetupSessionPrefs.markCancelled(context, sessionId)
            handledSessionIds.value = handledSessionIds.value + sessionId

            val nameToSend = currentUserName?.trim().orEmpty()
            if (nameToSend.isNotEmpty()) {
                sendMessageViewModel.sendMessage(
                    senderId = currentUserId ?: "",
                    senderName = nameToSend,
                    receiverId = otherUserId,
                    text = "❌ Meetup cancelled\nSessionId: $sessionId"
                ) { _, _, _ -> }
            }
        }
        MeetupSessionPrefs.clear(context)

        if (sessionId.isNullOrBlank()) return
        cancelSessionViewModel.cancelSession(sessionId) { success, message, _ ->
            showToast(if (success) "Meetup cancelled" else "Stopped sharing here, but server cancel failed: $message")
        }
    }

    fun getAcceptorCurrentLocation(onResult: (Location?) -> Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            onResult(null)
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val last = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: SecurityException) {
            null
        }

        if (last != null) {
            onResult(last)
            return
        }

        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            onResult(null)
            return
        }

        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    onResult(location)
                }
            }
            lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("Chat", "Failed to request current location", e)
            onResult(null)
        }
    }


    fun createAndAttachSession(pinLat: Double, pinLng: Double, myUserId: String) {
        sessionStartViewModel.startSession(
            lat = pinLat,
            lng = pinLng,
            invitedBy = otherUserId
        ) { success, message, session ->
            if (!success || session == null) {
                isProcessingMeetup = false
                showToast(message.ifBlank { "Failed to start session" })
                Log.e("Chat", "Failed to start session: $message")
                return@startSession
            }

            val sessionId = session.sessionId
            Log.d("Chat", "Session created: $sessionId invitedBy=$otherUserId")

            MeetupSessionPrefs.save(context, sessionId, otherUserId)
            activeSessionId = sessionId
            handledSessionIds.value = handledSessionIds.value + sessionId

            addUserToSessionViewModel.addUserToSession(sessionId, myUserId) { attachedMe, msgMe, _ ->
                if (!attachedMe) {
                    isProcessingMeetup = false
                    showToast("Failed to attach you: $msgMe")
                    Log.e("Chat", "Failed to attach current user: $msgMe")
                    return@addUserToSession
                }

                addUserToSessionViewModel.addUserToSession(sessionId, otherUserId) { attachedOther, msgOther, _ ->
                    if (!attachedOther) {
                        isProcessingMeetup = false
                        showToast("Failed to attach $otherUserName: $msgOther")
                        Log.e("Chat", "Failed to attach other user: $msgOther")
                        return@addUserToSession
                    }

                    val nameToSend = currentUserName?.trim().orEmpty()
                    if (nameToSend.isNotEmpty()) {
                        val confirmationText = buildString {
                            append("✅ Meetup accepted\n")
                            append("SessionId: $sessionId\n")
                            append("We're now sharing live location until we meet up!")
                        }
                        sendMessageViewModel.sendMessage(
                            senderId = myUserId,
                            senderName = nameToSend,
                            receiverId = otherUserId,
                            text = confirmationText
                        ) { sent, sendMsg, _ ->
                            if (!sent) {
                                Log.e("Chat", "Failed to send session confirmation: $sendMsg")
                            }
                            getConversationViewModel.getConversation(
                                myUserId, otherUserId, showLoading = false
                            )
                        }
                    }

                    // isProcessingMeetup stays true here — cleared only once
                    // startTrackingService actually runs, or a permission is
                    // denied along the way.
                    requestLocationPermissionsAndStart(sessionId, myUserId)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        if (currentUserId != null) {
            getConversationViewModel.getConversation(currentUserId, otherUserId, showLoading = true)
            getConversationViewModel.startPolling(currentUserId, otherUserId)
        }
        onDispose {
            getConversationViewModel.stopPolling()
        }
    }

    LaunchedEffect(otherUserId) {
        activeSessionId = MeetupSessionPrefs.getSessionIdForUser(context, otherUserId)
    }

    // ---------------------------------------------------------------
    // SENDER-SIDE AUTO-START
    // Watches incoming messages for the "✅ Meetup accepted" confirmation.
    // If this device hasn't already started tracking for that sessionId,
    // it kicks off its own permission flow + tracking service — this is
    // what makes the SENDER's phone start reporting GPS too, not just
    // the acceptor's.
    // ---------------------------------------------------------------
    LaunchedEffect(state.messages) {
        val myUserId = currentUserId ?: return@LaunchedEffect
        val latestConfirmation = state.messages
            .asReversed()
            .firstNotNullOfOrNull { msg -> parseSessionAcceptedMessage(msg.text) }

        if (latestConfirmation != null &&
            latestConfirmation !in handledSessionIds.value &&
            latestConfirmation != activeSessionId &&
            !MeetupSessionPrefs.wasCancelled(context, latestConfirmation)
        ) {
            handledSessionIds.value = handledSessionIds.value + latestConfirmation
            Log.d("Chat", "Detected session confirmation, starting tracking: $latestConfirmation")
            requestLocationPermissionsAndStart(latestConfirmation, myUserId)
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(selectedAction) {
        if (selectedAction == "chat") {
            chatFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(savedStateHandle) {
        if (savedStateHandle == null) return@LaunchedEffect

        savedStateHandle.getStateFlow("picked_lat", Double.NaN).collect { pinLat ->
            val pinLng = savedStateHandle.get<Double>("picked_lng") ?: Double.NaN
            val userLat = savedStateHandle.get<Double>("my_lat") ?: Double.NaN
            val userLng = savedStateHandle.get<Double>("my_lng") ?: Double.NaN

            if (pinLat.isNaN() || pinLng.isNaN()) return@collect

            pinnedLat = pinLat
            pinnedLng = pinLng
            myLat = userLat.takeIf { !it.isNaN() }
            myLng = userLng.takeIf { !it.isNaN() }
            pinnedAddress = null
            showMeetupDialog = true

            savedStateHandle.remove<Double>("picked_lat")
            savedStateHandle.remove<Double>("picked_lng")
            savedStateHandle.remove<Double>("my_lat")
            savedStateHandle.remove<Double>("my_lng")
        }
    }

    LaunchedEffect(showMeetupDialog, pinnedLat, pinnedLng) {
        val lat = pinnedLat
        val lng = pinnedLng
        if (showMeetupDialog && lat != null && lng != null) {
            pinnedAddress = reverseGeocode(lng, lat, token)
        }
    }

    fun sendPlain(text: String) {
        val nameToSend = currentUserName?.trim().orEmpty()
        if (currentUserId == null || nameToSend.isEmpty()) return
        sendMessageViewModel.sendMessage(
            senderId = currentUserId,
            senderName = nameToSend,
            receiverId = otherUserId,
            text = text
        ) { success, message, _ ->
            if (success) {
                getConversationViewModel.getConversation(
                    currentUserId,
                    otherUserId,
                    showLoading = false
                )
            } else {
                showToast("Send failed: $message")
                Log.e("Chat", "Send failed: $message")
            }
        }
    }

    fun startMeetupSession(pinLat: Double, pinLng: Double) {
        val myUserId = currentUserId
        if (myUserId == null) {
            showToast("Cannot start session: user id missing")
            Log.e("Chat", "Cannot start session: currentUserId is null")
            return
        }

        // Kick off the blocking overlay right when the user taps Accept —
        // everything from here (GPS fix, session create, attach, perms,
        // tracking start) happens behind the spinner instead of toasts.
        isProcessingMeetup = true

        getAcceptorCurrentLocation { myLocation ->
            if (myLocation != null) {
                myLat = myLocation.latitude
                myLng = myLocation.longitude
                Log.d(
                    "Chat",
                    "Acceptor GPS=${myLocation.latitude},${myLocation.longitude} | pin=$pinLat,$pinLng"
                )
            } else {
                Log.w("Chat", "Acceptor current location unavailable — falling back to meetup pin")
            }

            createAndAttachSession(pinLat, pinLng, myUserId)
        }
    }

    fun onSendMeetupLocation() {
        val pinLat = pinnedLat
        val pinLng = pinnedLng
        val nameToSend = currentUserName?.trim().orEmpty()
        if (pinLat == null || pinLng == null || currentUserId == null || nameToSend.isEmpty()) return

        scope.launch {
            val address = pinnedAddress ?: reverseGeocode(pinLng, pinLat, token) ?: "Dropped pin"
            val now = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                .format(java.util.Date())
            val activityLabel = activityName.trim().ifEmpty { activity.trim().ifEmpty { "Activity" } }
            val mapsLink = mapsSearchUrl(pinLat, pinLng)

            val messageText = buildString {
                append("📍 Meetup location\n")
                append("With: $otherUserName\n")
                append("Activity: $activityLabel\n")
                append("When: $now\n")
                append("Address: $address\n")
                append("Pin: ${"%.5f".format(pinLat)}, ${"%.5f".format(pinLng)}\n")
                if (myLat != null && myLng != null) {
                    append("My location: ${"%.5f".format(myLat)}, ${"%.5f".format(myLng)}\n")
                }
                append(mapsLink)
            }

            sendMessageViewModel.sendMessage(
                senderId = currentUserId,
                senderName = nameToSend,
                receiverId = otherUserId,
                text = messageText
            ) { success, message, _ ->
                if (success) {
                    showMeetupDialog = false
                    activityName = ""
                    showToast("Meetup location sent")
                    getConversationViewModel.getConversation(
                        currentUserId,
                        otherUserId,
                        showLoading = false
                    )
                } else {
                    showToast("Meetup send failed: $message")
                    Log.e("Chat", "Meetup send failed: $message")
                }
            }
        }
    }

    fun onSendClicked() {
        val textToSend = inputText.trim()
        val nameToSend = currentUserName?.trim().orEmpty()

        if (textToSend.isEmpty() || currentUserId == null || nameToSend.isEmpty()) {
            showToast("Cannot send: missing user or message")
            Log.e("Chat", "Missing field: userId=$currentUserId name='$nameToSend'")
            return
        }

        sendMessageViewModel.sendMessage(
            senderId = currentUserId,
            senderName = nameToSend,
            receiverId = otherUserId,
            text = textToSend
        ) { success, message, _ ->
            if (success) {
                inputText = ""
                getConversationViewModel.getConversation(
                    currentUserId,
                    otherUserId,
                    showLoading = false
                )
            } else {
                showToast("Send failed: $message")
                Log.e("Chat", "Send failed: $message")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .background(Color(0xFFF8F8F8))
        ) {
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
                Text(
                    text = otherUserName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                if (activeSessionId != null) {
                    IconButton(onClick = {
                        navController.navigate("live_tracking/$activeSessionId")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "View live meetup map",
                            tint = Color(0xFFFF7B00)
                        )
                    }
                }
            }

            if (activeSessionId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sharing your live location for this meetup",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cancel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.clickable { cancelActiveSession() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF4EA))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.padding(top = 1.dp).size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Meet smart, meet safe.\nSkip typing out addresses in chat — tap Send Location instead. It's quicker, clearer, and keeps your meetup details secure.",
                        color = Color(0xFF626262),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.messages) { msg ->
                                    MessageBubble(
                                        message = msg,
                                        isMine = msg.senderId == currentUserId,
                                        onOpenMaps = { lat, lng -> openMaps(context, lat, lng) },
                                        onConfirmLocation = { lat, lng -> startMeetupSession(lat, lng) },
                                        onRejectLocation = { sendPlain("Not this location") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, spotColor = Color.Black.copy(alpha = 0.08f))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChip(
                        text = "Send activity location",
                        selected = selectedAction == "location",
                        selectedIcon = Icons.Filled.LocationOn,
                        unselectedIcon = Icons.Outlined.LocationOn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedAction = if (selectedAction == "location") null else "location"
                        }
                    )
                    ActionChip(
                        text = "Chat",
                        selected = selectedAction == "chat",
                        selectedIcon = Icons.AutoMirrored.Filled.Chat,
                        unselectedIcon = Icons.Outlined.Chat,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedAction = if (selectedAction == "chat") null else "chat"
                        }
                    )
                }

                AnimatedVisibility(
                    visible = selectedAction == "location",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF4EA))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFF7B00),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Share activity location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Send a pin the other person can open", fontSize = 12.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFF7B00))
                                .clickable { navController.navigate("map_picker") }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Send", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedAction == "chat",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f).focusRequester(chatFocusRequester),
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
        }

        if (showMeetupDialog && pinnedLat != null && pinnedLng != null) {
            val nowText = remember {
                java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date())
            }

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showMeetupDialog = false },
                title = { Text("Meetup location", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column {
                        Text("Username: ${currentUserName ?: "-"}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Meeting with: $otherUserName", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Address: ${pinnedAddress ?: "Finding address…"}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Pin: ${"%.5f".format(pinnedLat)}, ${"%.5f".format(pinnedLng)}", fontSize = 14.sp)
                        if (myLat != null && myLng != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("My location: ${"%.5f".format(myLat)}, ${"%.5f".format(myLng)}", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Date & time: $nowText", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Activity: $activityName", fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFF7B00))
                            .clickable { onSendMeetupLocation() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Send meetup location", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    Text(
                        text = "Cancel",
                        modifier = Modifier.clickable { showMeetupDialog = false }.padding(8.dp),
                        color = Color.Gray
                    )
                }
            )
        }

        // Blocking progress overlay for the accept-meetup flow. Not
        // dismissable by back press or outside tap — this is a short,
        // multi-step network sequence that shouldn't be interrupted
        // halfway (e.g. session created but tracking not started yet).
        if (isProcessingMeetup) {
            Dialog(
                onDismissRequest = { /* not dismissable while in progress */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(horizontal = 28.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF7B00), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Setting up your meetup…",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Starting live location sharing",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean,
    onOpenMaps: (Double, Double) -> Unit,
    onConfirmLocation: (Double, Double) -> Unit,
    onRejectLocation: () -> Unit
) {
    val location = remember(message.text) { parseLocationMessage(message.text) }
    val sessionConfirmed = remember(message.text) { parseSessionAcceptedMessage(message.text) != null }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        when {
            location != null -> {
                Column(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.06f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isMine) Color(0xFFFF7B00) else Color.White)
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text.substringBefore("http").trim(),
                        color = if (isMine) Color.White else Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isMine) Color.White else Color(0xFFFF7B00))
                            .clickable { onOpenMaps(location.first, location.second) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open in Maps",
                            color = if (isMine) Color(0xFFFF7B00) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    if (!isMine) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .clickable { onRejectLocation() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Not this location", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFF7B00))
                                    .clickable {
                                        onConfirmLocation(location.first, location.second)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Accept Activity location", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            sessionConfirmed -> {
                // simple system-style bubble for the session confirmation
                // message — no action buttons, just a status line
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            else -> {
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
    }
}

@Composable
private fun ActionChip(
    text: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFFF7B00) else Color.White
    val fg = if (selected) Color.White else Color.Black
    val border = if (selected) Color(0xFFFF7B00) else Color(0xFFE0E0E0)

    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .border(1.dp, border, RoundedCornerShape(21.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InitialsAvatar(name: String, size: Dp = 40.dp) {
    val palette = listOf(
        Color(0xFF4A6CF7),
        Color(0xFFFF7B00),
        Color(0xFF2E7D32),
        Color(0xFF8E24AA),
        Color(0xFFEF5350),
        Color(0xFF00897B)
    )
    val color = palette[(name.hashCode().let { if (it < 0) -it else it }) % palette.size]
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun mapsSearchUrl(lat: Double, lng: Double): String {
    return "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
}

private fun openMaps(context: Context, lat: Double, lng: Double) {
    val geo = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
    val web = Uri.parse(mapsSearchUrl(lat, lng))
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, geo))
    } catch (_: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun parseLocationMessage(text: String): Pair<Double, Double>? {
    if (!text.contains("📍") && !text.contains("Meetup location", ignoreCase = true)) return null
    val pin = Regex("""Pin:\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)""").find(text)
    if (pin != null) return pin.groupValues[1].toDouble() to pin.groupValues[2].toDouble()
    val url = Regex("""query=(-?\d+\.?\d*),(-?\d+\.?\d*)""").find(text)
    if (url != null) return url.groupValues[1].toDouble() to url.groupValues[2].toDouble()
    val old = Regex("""[?&]q=(-?\d+\.?\d*),(-?\d+\.?\d*)""").find(text)
    if (old != null) return old.groupValues[1].toDouble() to old.groupValues[2].toDouble()
    return null
}

// extracts the sessionId from a "✅ Meetup accepted" confirmation
// message, or null if this message isn't one of those
private fun parseSessionAcceptedMessage(text: String): String? {
    if (!text.contains("Meetup accepted", ignoreCase = true)) return null
    val match = Regex("""SessionId:\s*([a-fA-F0-9-]{36})""").find(text) ?: return null
    return match.groupValues[1]
}

private fun parseSessionCancelledMessage(text: String): String? {
    if (!text.contains("Meetup cancelled", ignoreCase = true)) return null
    return Regex("""SessionId:\s*([a-fA-F0-9-]{36})""").find(text)?.groupValues?.get(1)
}

private suspend fun reverseGeocode(lng: Double, lat: Double, token: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$lng,$lat.json?limit=1&access_token=$token"
            val body = java.net.URL(url).readText()
            val features = JSONObject(body).optJSONArray("features") ?: return@withContext null
            if (features.length() == 0) null else features.getJSONObject(0).optString("place_name").ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}