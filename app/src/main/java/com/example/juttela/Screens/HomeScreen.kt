package com.example.juttela.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.juttela.DataSource.Models.AgePreference
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Brush
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.juttela.DataSource.Models.Match
import com.example.juttela.DataSource.Models.ProfileDataNew
import com.example.juttela.DataSource.Models.SendingSmartRequest
import com.example.juttela.DataSource.Models.SmartMatch
import com.example.juttela.R
import com.example.juttela.Utils.CloudinaryUploader
import com.example.juttela.Utils.NearbyMatchesDialog
import com.example.juttela.Utils.SmartNearbyMatchesDialog
import com.example.juttela.Utils.SmartPreferenceDialog
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.Utils.getGreeting
import com.example.juttela.ViewModels.FindMateViewModel
import com.example.juttela.ViewModels.GetProfileViewModel
import com.example.juttela.ViewModels.RequestViewModel
import com.example.juttela.ViewModels.SmartMatchViewModel
import com.example.juttela.ViewModels.SmartRequestViewModel
import com.example.juttela.ViewModels.SubscriptionViewModel
import com.example.juttela.ViewModels.UpdateProfileViewModel
import com.example.juttela.components.ActivityDropdown
import com.example.juttela.components.PopularNearYou
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val findMateViewModel: FindMateViewModel = viewModel()
    val requestViewModel: RequestViewModel = viewModel()
    val getProfileViewModel: GetProfileViewModel = viewModel()
    val updateProfileViewModel: UpdateProfileViewModel = viewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val isPro by subscriptionViewModel.isPro.collectAsState()
    var showPreferenceDialog by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<ProfileDataNew?>(null) }

    var activity by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showMatchesDialog by remember { mutableStateOf(false) }

    val smartMatchViewModel: SmartMatchViewModel = viewModel()
    val smartRequestViewModel: SmartRequestViewModel = viewModel()
    var showSmartDialog by remember { mutableStateOf(false) }
    var smartMatches by remember { mutableStateOf<List<SmartMatch>>(emptyList()) }

    // Profile related states
    var showProfileScreen by remember { mutableStateOf(false) }
    var showCompleteProfileDialog by remember { mutableStateOf(false) }
    var currentProfile by remember { mutableStateOf<ProfileDataNew?>(null) }

    // Tracks which userIds we've already sent a request to (this session), and which one is mid-send
    var sentRequestIds by remember { mutableStateOf(setOf<String>()) }
    var sendingRequestId by remember { mutableStateOf<String?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }



    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onSmartRequestClicked(match: SmartMatch) {
        if (sentRequestIds.contains(match.userId) || sendingRequestId == match.userId) {
            return
        }

        val currentUserId = UserPrefs.getUserId(context)
        val currentUserName = UserPrefs.getUserName(context)

        if (currentUserId == null || currentUserName == null) {
            Toast.makeText(
                context,
                "Something went wrong with your profile. Please sign in again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Use the profile we already loaded for the Pro flow
        val me = selectedProfile
        if (me == null) {
            Toast.makeText(
                context,
                "Profile not loaded. Please try again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        sendingRequestId = match.userId

        smartRequestViewModel.sendSmartRequest(
            SendingSmartRequest(
                senderId = currentUserId.toString(),
                senderName = currentUserName,
                recipientId = match.userId,

                // === CURRENT USER data (the sender) ===
                name = me.name ?: currentUserName,
                photo = me.profileImageUrl,
                age = me.age,
                gender = me.gender.toString(),
                interests = me.interests ?: emptyList(),
                rating = me.rating.average,               // or me.rating if it's already a number
                feedbackCount = me.feedbackCount.takeIf { it > 0 } ?: me.rating.count,

                // === from the match card ===
                activity = match.activity,
                distanceKm = match.distanceKm,
                matchScore = match.matchScore
            )
        ) { success, message ->
            sendingRequestId = null

            if (success) {
                sentRequestIds = sentRequestIds + match.userId
                Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun fetchLocation(onResult: (Boolean) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(false)
            return
        }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
                .addOnFailureListener {
                    onResult(false)
                }
        } catch (e: SecurityException) {
            onResult(false)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocation { success ->
                if (!success) {
                    Toast.makeText(
                        context,
                        "Couldn't get your location. Please enable GPS and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                context,
                "Location permission is required to find people nearby.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            fetchLocation { }
        }
    }

    LaunchedEffect(Unit) {
        subscriptionViewModel.checkProStatus()
    }

    fun onFindMateClicked() {
        if (activity.isBlank()) {
            Toast.makeText(context, "Please select an activity first", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(
                context,
                "Fetching your location, please try again in a moment",
                Toast.LENGTH_SHORT
            ).show()
            fetchLocation { success ->
                if (!success) {
                    Toast.makeText(
                        context,
                        "Couldn't get your location. Please enable GPS.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        // Reset sent-state for a fresh search
        sentRequestIds = emptySet()

        findMateViewModel.findMate(
            activity = activity,
            longitude = longitude!!,
            latitude = latitude!!
        ) { success, message, matchCount ->
            if (success && matchCount > 0) {
                showMatchesDialog = true
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onSmartMatchClicked(
        profile: ProfileDataNew,
        agePreference: AgePreference,
        genderPreference: String
    ) {
        if (activity.isBlank()) {
            Toast.makeText(context, "Please select an activity first", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(
                context,
                "Fetching your location, please try again in a moment",
                Toast.LENGTH_SHORT
            ).show()
            fetchLocation { success ->
                if (!success) {
                    Toast.makeText(
                        context,
                        "Couldn't get your location. Please enable GPS.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        val currentUserId = UserPrefs.getUserId(context)
        val currentUserName = UserPrefs.getUserName(context) ?: profile.name ?: "Jutella User"

        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        sentRequestIds = emptySet()

        smartMatchViewModel.smartMatch(
            name = currentUserName,
            userId = currentUserId.toString(),
            activity = activity,
            longitude = longitude!!,
            latitude = latitude!!,
            radiusKm = 5.0,
            agePreference = agePreference,
            genderPreference = genderPreference
        ) { success, message, matches ->
            if (!success) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                return@smartMatch
            }

            if (matches.isEmpty()) {
                Toast.makeText(context, "No smart matches nearby", Toast.LENGTH_LONG).show()
                return@smartMatch
            }

            smartMatches = matches
            showSmartDialog = true
        }
    }

    fun onRequestClicked(match: Match) {
        // Guard: don't allow re-sending to someone already requested, or double-tap mid-send
        if (sentRequestIds.contains(match.userId) || sendingRequestId == match.userId) {
            return
        }

        val currentUserId = UserPrefs.getUserId(context)
        val currentUserName = UserPrefs.getUserName(context)

        if (currentUserId == null || currentUserName == null) {
            Toast.makeText(
                context,
                "Something went wrong with your profile. Please sign in again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        sendingRequestId = match.userId

        requestViewModel.sendRequest(
            senderId = currentUserId,
            senderName = currentUserName,
            recipientId = match.userId,
            activity = match.activity,
            distanceKm = match.distanceKm
        ) { success, message ->
            sendingRequestId = null

            if (success) {
                sentRequestIds = sentRequestIds + match.userId
                Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ========== PROFILE CLICK ==========
    fun onProfileClicked() {
        val userId = UserPrefs.getUserId(context)
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        getProfileViewModel.getProfile(userId) { success, message, profile ->
            if (success && profile != null) {
                val isProfileIncomplete =
                    profile.profileImageUrl.isNullOrBlank() ||
                            profile.age == null ||
                            profile.gender.isNullOrBlank()

                if (isProfileIncomplete) {
                    showCompleteProfileDialog = true
                } else {
                    currentProfile = profile
                    showProfileScreen = true
                }
            } else {
                // No Profile document yet → open create profile dialog
                showCompleteProfileDialog = true
            }
        }
    }

    // ========== PRO "FIND MATE" ENTRY POINT ==========
    // Shared logic used by the Pro button; loads the profile first, then opens preferences.
    fun onProFindMateClicked() {
        val currentUserId = UserPrefs.getUserId(context)
        getProfileViewModel.getProfile(currentUserId.toString()) { success, message, profile ->
            if (!success || profile == null) {
                Toast.makeText(
                    context,
                    "Please create a profile before using Juttela Pro matching",
                    Toast.LENGTH_SHORT
                ).show()
                showCompleteProfileDialog = true
                return@getProfile
            }

            selectedProfile = profile
            showPreferenceDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // ==================== MAIN HOME CONTENT ====================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = getGreeting(),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Find your Mate",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Profile Icon - now clickable
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCEBFF))
                        .clickable { onProfileClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.juttelaprofile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF4A6CF7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.juttelalocation),
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delhi, India",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "What do you want to do ?",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))

            ActivityDropdown(
                selectedActivity = activity,
                onActivitySelected = { activity = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ==================== FIND MATE BUTTON (PRO vs FREE) ====================
            val isBusyFinding = findMateViewModel.state.loading || getProfileViewModel.state.loading

            if (isPro) {
                // Subscribed users see the premium gold button only.
                JuttelaProButton(
                    text = "Find nearby people",
                    loading = isBusyFinding,
                    enabled = !isBusyFinding,
                    onClick = { onProFindMateClicked() }
                )
            } else {
                // Free users see the original orange button only.
                Button(
                    onClick = { onFindMateClicked() },
                    enabled = !isBusyFinding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7B00),
                        contentColor = Color.White
                    )
                ) {
                    if (isBusyFinding) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Find nearby people",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Arrow",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            PopularNearYou(
                onActivityClick = { item ->
                    activity = item.title
                }
            )
        }

        // Loading overlay while fetching profile
        if (getProfileViewModel.state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF7B00))
            }
        }

        // ==================== PROFILE DETAILS SCREEN ====================
        if (showProfileScreen && currentProfile != null) {
            ProfileDetailsScreen(
                profile = currentProfile!!,
                onBack = { showProfileScreen = false },
                onLogout = {
                    // Clear user data
//                    UserPrefs.clear(context)          // make sure you have this method
//                    showProfileScreen = false
//                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
//                    // You can also navigate to Login screen here if you have NavController
                },
                onDeleteAccount = {
                    // TODO: Call delete account API when available
                    Toast.makeText(context, "Delete Account clicked", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ==================== COMPLETE PROFILE DIALOG ====================
        if (showCompleteProfileDialog) {
            CompleteProfileDialog(
                updateProfileViewModel = updateProfileViewModel,
                onDismiss = { showCompleteProfileDialog = false },
                onSuccess = {
                    showCompleteProfileDialog = false
                    Toast.makeText(context, "Profile completed successfully!", Toast.LENGTH_SHORT).show()
                    // Optionally re-fetch profile
                    onProfileClicked()
                }
            )
        }

        if (showMatchesDialog) {
            NearbyMatchesDialog(
                matches = findMateViewModel.state.matches,
                sentRequestIds = sentRequestIds,
                sendingRequestId = sendingRequestId,
                onDismiss = { showMatchesDialog = false },
                onRequestClick = { match -> onRequestClicked(match) }
            )
        }

        if (showPreferenceDialog) {
            SmartPreferenceDialog(
                onDismiss = { showPreferenceDialog = false },
                onSubmit = { agePreference, genderPreference ->
                    showPreferenceDialog = false
                    selectedProfile?.let { profile ->
                        onSmartMatchClicked(
                            profile = profile,
                            agePreference = agePreference,
                            genderPreference = genderPreference
                        )
                    }
                }
            )
        }

        if (showSmartDialog) {
            SmartNearbyMatchesDialog(
                matches = smartMatches,
                sentRequestIds = sentRequestIds,
                sendingRequestId = sendingRequestId,
                onDismiss = { showSmartDialog = false },
                onRequestClick = { match ->
                    onSmartRequestClicked(match)
                }
            )
        }

    }
}

// ============================================================
// PREMIUM "JUTTELA PRO" BUTTON
// Gold gradient pill with shimmer + crown icon, used only when isPro == true.
// ============================================================
@Composable
fun JuttelaProButton(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    // Subtle infinite shimmer sweep across the gradient
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD76A), // soft gold
            Color(0xFFF7A93B), // amber
            Color(0xFFE8842B)  // deep amber/orange
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0f),
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0f)
        ),
        start = Offset(shimmerOffset * 600f - 200f, 0f),
        end = Offset(shimmerOffset * 600f + 200f, 100f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0xFFF0A93B).copy(alpha = 0.5f),
                spotColor = Color(0xFFF0A93B).copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(goldGradient)
            .background(shimmerBrush)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color(0xFF3D2200),
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = "Pro",
                    tint = Color(0xFF7A3E00),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color(0xFF3D2200),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        color = Color(0xFF3D2200),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}


// ============================================================
// PROFILE DETAILS SCREEN
// ============================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailsScreen(
    profile: ProfileDataNew,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val interests = profile.interests ?: emptyList()
    val ratingAverage = profile.rating.average
    val ratingCount = profile.feedbackCount.takeIf { it > 0 } ?: profile.rating.count

    fun formatInterest(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "My Profile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF1E0), Color.White)
                        )
                    )
                    .padding(vertical = 22.dp, horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.5.dp, Color(0xFFFF7B00), CircleShape)
                            .padding(3.dp)
                    ) {
                        AsyncImage(
                            model = profile.profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.juttelaprofile),
                            error = painterResource(id = R.drawable.juttelaprofile)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = profile.name ?: "No Name",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (!profile.email.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile.email,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileStatPill(
                            text = if (ratingCount > 0) {
                                "★ $ratingAverage"
                            } else {
                                "★ New"
                            }
                        )
                        ProfileStatPill(
                            text = if (ratingCount > 0) {
                                "$ratingCount reviews"
                            } else {
                                "No reviews yet"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Interests",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (interests.isEmpty()) {
                    Text(
                        text = "No interests added yet",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.forEach { interest ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFFF1E0))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formatInterest(interest),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFF7B00)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(vertical = 6.dp)
            ) {
                ProfileInfoRow(
                    icon = Icons.Default.Face,
                    label = "Age",
                    value = profile.age?.toString() ?: "Not set"
                )
                ProfileDivider()
                ProfileInfoRow(
                    icon = Icons.Default.AccountCircle,
                    label = "Gender",
                    value = profile.gender?.replaceFirstChar { it.uppercase() } ?: "Not set"
                )
                ProfileDivider()
                ProfileInfoRow(
                    icon = Icons.Default.Info,
                    label = "About",
                    value = profile.about ?: "No bio yet"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7B00),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDeleteAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                ),
                border = BorderStroke(1.2.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileStatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFF7B00).copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFF7B00)
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
}


// ============================================================
// COMPLETE PROFILE DIALOG — updated with real Cloudinary upload
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileDialog(
    updateProfileViewModel: UpdateProfileViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val interestList = listOf(
        "running", "walking", "cycling", "roller_skating", "skateboarding",
        "yoga", "gym", "hiking", "rock_climbing", "swimming",
        "football", "badminton", "basketball", "volleyball", "tennis",
        "table_tennis", "cricket", "frisbee", "bowling", "billiards",
        "coffee", "lunch", "dinner", "breakfast", "ice_cream",
        "drinks", "sushi", "street_food", "dessert", "shopping",
        "movies", "watch_series", "gaming", "board_games", "chess",
        "karaoke", "theatre", "painting", "photography", "fishing",
        "study", "coding", "book_club", "puzzle_solving", "coworking",
        "conversation", "casual_chat", "make_friends", "meet_new_people", "dating",
        "dog_walking", "sunrise_sunset", "local_events",
        "music", "dancing", "travel", "camping", "fitness",
        "cooking", "reading", "podcasts", "standup", "nightlife"
    )

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(listOf<String>()) }
    var interestMenuExpanded by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val isBusy = isUploadingImage || updateProfileViewModel.state.loading

    fun formatInterest(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    fun onInterestSelected(interest: String) {
        when {
            selectedInterests.contains(interest) -> {
                selectedInterests = selectedInterests - interest
            }
            selectedInterests.size >= 3 -> {
                Toast.makeText(context, "You can select only 3 interests", Toast.LENGTH_SHORT).show()
            }
            else -> {
                selectedInterests = selectedInterests + interest
            }
        }
        interestMenuExpanded = false
    }

    fun onSaveClicked() {
        val userId = UserPrefs.getUserId(context)
        if (userId == null) {
            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select a profile photo", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }
        if (age.isBlank() || gender.isBlank()) {
            Toast.makeText(context, "Please fill in your age and gender", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedInterests.isEmpty()) {
            Toast.makeText(context, "Please select at least 1 interest", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isUploadingImage = true
            val uploadedUrl = CloudinaryUploader.uploadImage(context, selectedImageUri!!)
            isUploadingImage = false

            if (uploadedUrl.isNullOrBlank()) {
                Toast.makeText(context, "Image upload failed, please try again", Toast.LENGTH_LONG).show()
                return@launch
            }

            updateProfileViewModel.updateProfile(
                userId = userId,
                name = name.trim(),
                profileImageUrl = uploadedUrl,
                age = age.toIntOrNull() ?: 18,
                gender = gender.lowercase(),
                about = about.ifBlank { null },
                interests = selectedInterests
            ) { success, message, _ ->
                if (success) {
                    onSuccess()
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Complete Your Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "A few details so people know who they're meeting",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCEBFF))
                        .clickable(enabled = !isBusy) { imagePicker.launch("image/*") }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.juttelaprofile),
                            contentDescription = "Add Photo",
                            tint = Color(0xFF4A6CF7),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    if (isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (selectedImageUri != null) "Tap to change photo" else "Tap to add photo *",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isBusy,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7B00))
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Age", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isBusy,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7B00))
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender (male / female / other)", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isBusy,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7B00))
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = interestMenuExpanded,
                    onExpandedChange = {
                        if (!isBusy) interestMenuExpanded = !interestMenuExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = if (selectedInterests.isEmpty()) {
                            ""
                        } else {
                            selectedInterests.joinToString(", ") { formatInterest(it) }
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Interests (max 3)", fontSize = 13.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = interestMenuExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isBusy,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7B00))
                    )

                    ExposedDropdownMenu(
                        expanded = interestMenuExpanded,
                        onDismissRequest = { interestMenuExpanded = false }
                    ) {
                        interestList.forEach { interest ->
                            val isSelected = selectedInterests.contains(interest)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = formatInterest(interest),
                                        color = if (isSelected) Color(0xFFFF7B00) else Color.Black
                                    )
                                },
                                onClick = { onInterestSelected(interest) },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFFFF7B00)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                if (selectedInterests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${selectedInterests.size}/3 selected",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedInterests.forEach { interest ->
                            AssistChip(
                                onClick = { onInterestSelected(interest) },
                                label = { Text(formatInterest(interest), fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = about,
                    onValueChange = { about = it },
                    label = { Text("About you (optional)", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    enabled = !isBusy,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7B00))
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = { onSaveClicked() },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7B00),
                        contentColor = Color.White
                    )
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("Save Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = { if (!isBusy) onDismiss() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}