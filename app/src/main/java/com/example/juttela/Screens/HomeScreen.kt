package com.example.juttela.Screens



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.juttela.DataSource.Models.AgePreference
import androidx.compose.ui.graphics.Brush
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.juttela.DataSource.Models.Match
import com.example.juttela.DataSource.Models.ProfileDataNew
import com.example.juttela.DataSource.Models.SendingSmartRequest
import com.example.juttela.DataSource.Models.SmartMatch
import com.example.juttela.R
import com.example.juttela.Utils.ApprovalPrefs
import com.example.juttela.Utils.CloudinaryUploader
import com.example.juttela.Utils.DailyFindLimitStore
import com.example.juttela.Utils.DailyProCardStore
import com.example.juttela.Utils.DeleteAccountConfirmationDialog
import com.example.juttela.Utils.DraggableProSubscriptionCard
import com.example.juttela.Utils.LocationRow
import com.example.juttela.Utils.NearbyMatchesDialog
import com.example.juttela.Utils.PopularNearYouPlaceholder
import com.example.juttela.Utils.SmartNearbyMatchesDialog
import com.example.juttela.Utils.SmartPreferenceDialog
import com.example.juttela.Utils.UserPrefs
import com.example.juttela.Utils.VerificationPassCard
import com.example.juttela.Utils.getGreeting
import com.example.juttela.ViewModels.ApprovalViewModel
import com.example.juttela.ViewModels.DeleteAccountViewModel
import com.example.juttela.ViewModels.FindMateViewModel
import com.example.juttela.ViewModels.GetAllActivityViewModel
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


private val Orange = Color(0xFFFF7B00)
private val OrangeSoft = Color(0xFFFFF1E0)
private val Gold = Color(0xFFFFD76A)
private val GoldDeep = Color(0xFFE8842B)
private val ScreenBg = Color(0xFFF7F7F9)
private val TextDark = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF8A8A8E)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val findMateViewModel: FindMateViewModel = viewModel()
    val requestViewModel: RequestViewModel = viewModel()
    val getProfileViewModel: GetProfileViewModel = viewModel()
    val updateProfileViewModel: UpdateProfileViewModel = viewModel()
    var showLiveBubble by remember { mutableStateOf(true) }
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val isPro by subscriptionViewModel.isPro.collectAsState()
    val dailyStore = remember { DailyProCardStore(context) }
    val currentUserId = UserPrefs.getUserId(context).orEmpty()
    val isVerified = remember { ApprovalPrefs.isApproved(context) }

    val userId = UserPrefs.getUserId(context)
    val viewModel: GetAllActivityViewModel = viewModel()





    // ---- Smart match preference persistence ----
    // Stores the user's chosen age/gender preference so the preference dialog
    // only needs to be shown once, not on every single "Find nearby people" tap.
    val smartPrefsStore = remember {
        context.getSharedPreferences("juttela_smart_prefs", android.content.Context.MODE_PRIVATE)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // FIXED: AgePreference is a data class { min: Int, max: Int }, not an enum,
    // so it has no .name / .valueOf(). Persist its two Int fields instead and
    // reconstruct the object with AgePreference(min, max) when loading.
    var savedAgePreference by remember {
        mutableStateOf(
            run {
                val min = smartPrefsStore.getInt("age_preference_min", -1)
                val max = smartPrefsStore.getInt("age_preference_max", -1)
                if (min >= 0 && max >= 0) AgePreference(min = min, max = max) else null
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.getAllActivities { _, _, _ -> }
    }

    var savedGenderPreference by remember {
        mutableStateOf(smartPrefsStore.getString("gender_preference", null))
    }

    fun persistSmartPreference(agePreference: AgePreference, genderPreference: String) {
        savedAgePreference = agePreference
        savedGenderPreference = genderPreference
        smartPrefsStore.edit()
            .putInt("age_preference_min", agePreference.min)
            .putInt("age_preference_max", agePreference.max)
            .putString("gender_preference", genderPreference)
            .apply()
    }


    val deleteAccountViewModel: DeleteAccountViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    val deleteState = deleteAccountViewModel.state

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
    val findLimitStore = remember { DailyFindLimitStore(context) }
    var remainingFinds by remember {
        mutableIntStateOf(findLimitStore.remaining())
    }



    var showProfileScreen by remember { mutableStateOf(false) }
    var showCompleteProfileDialog by remember { mutableStateOf(false) }
    var currentProfile by remember { mutableStateOf<ProfileDataNew?>(null) }

    var headerProfileImageUrl by remember {
        mutableStateOf(UserPrefs.getProfileImageUrl(context))
    }

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
            request = SendingSmartRequest(
                senderId = currentUserId.toString(),
                senderName = currentUserName,
                recipientId = match.userId,
                name = me.name ?: currentUserName,
                photo = me.profileImageUrl,
                age = me.age,
                gender = me.gender.orEmpty(),
                interests = me.interests ?: emptyList(),
                rating = me.rating?.average ?: 0.0,
                feedbackCount = me.feedbacks?.count?.takeIf { it > 0 }
                    ?: (me.rating?.count ?: 0),
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

    LaunchedEffect(isPro) {
        Log.d("Subscription", "isPro changed: $isPro")
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            fetchLocation { }
        }
    }

    LaunchedEffect(Unit) {
        subscriptionViewModel.checkProStatus()
    }


    fun onFindMateClicked(onSearchStarted: () -> Unit = {}) {
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

        // All guard clauses passed — the search is definitely about to fire.
        onSearchStarted()

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
            Log.d("PROFILE", "success=$success message=$message")
            Log.d("PROFILE", "userId=${profile?.userId} name=${profile?.name}")
            Log.d("PROFILE", "rating=${profile?.rating}")
            Log.d("PROFILE", "feedbacks=${profile?.feedbacks}")

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
                showCompleteProfileDialog = true
            }
        }
    }

    // ========== PRO "FIND MATE" ENTRY POINT ==========
    // Shared logic used by the Pro button; loads the profile first, then either
    // reuses a previously saved age/gender preference (searching immediately)
    // or opens the preference dialog the first time only.
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

            val age = savedAgePreference
            val gender = savedGenderPreference

            if (age != null && gender != null) {
                // Preference already chosen once before — skip the dialog and search right away.
                onSmartMatchClicked(
                    profile = profile,
                    agePreference = age,
                    genderPreference = gender
                )
            } else {
                showPreferenceDialog = true
            }
        }
    }

    // ==================== OUTER BOX: full screen, holds scroll content + floating bubble + dialogs ====================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // ==================== MAIN HOME CONTENT (scrolls) ====================
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

                // Avatar + badge together in ONE Box
                Box(modifier = Modifier.size(52.dp)) {

                    // Profile Icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFDCEBFF))
                            .clickable { onProfileClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!headerProfileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = headerProfileImageUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.juttelaprofile),
                                error = painterResource(id = R.drawable.juttelaprofile)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.juttelaprofile),
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF4A6CF7)
                            )
                        }
                    }

                    // Verified badge: top-left, on top of the avatar
                    if (isVerified) {
                        Image(
                            painter = painterResource(id = R.drawable.verifieduser),
                            contentDescription = "Verified user",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 7.dp, y = (-6).dp)
                                .size(30.dp)
                        )
                    }
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
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.juttelalocation),
//                            contentDescription = "Location",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(22.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Delhi, India",
//                            fontSize = 16.sp,
//                            color = Color.Gray,
//                            fontWeight = FontWeight.Black
//                        )
//                    }
                    LocationRow()
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
                activities = viewModel.state.response?.data ?: emptyList(),
                selectedActivity = activity,
                onActivitySelected = { activity = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            val isBusyFinding = findMateViewModel.state.loading || getProfileViewModel.state.loading

            // FIXED: quota is now only deducted inside onSearchStarted, which
            // onFindMateClicked() only invokes once every guard clause has passed
            // and the network call is actually about to be made.
            @RequiresApi(Build.VERSION_CODES.O)
            fun onFreeFindClicked() {
                if (!findLimitStore.canUse()) {
                    navController.navigate("subscription")
                    return
                }
                onFindMateClicked(
                    onSearchStarted = {
                        findLimitStore.recordUse()
                        remainingFinds = findLimitStore.remaining()
                    }
                )
            }

            if (isPro) {
                JuttelaProButton(
                    text = "Find nearby people",
                    loading = isBusyFinding,
                    enabled = !isBusyFinding,
                    onClick = { onProFindMateClicked() }
                )

                // Preference was already picked once — let the user change it
                // later without forcing the dialog on every search. Styled as a
                // small, low-emphasis chip (outline + icon, content-sized width)
                // so it reads as a secondary action and never competes visually
                // with the primary gold "Find nearby people" button above it.
                if (savedAgePreference != null && savedGenderPreference != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE0A93B).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { showPreferenceDialog = true }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit preferences",
                                tint = Color(0xFF9A6A1E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change match preferences",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9A6A1E)
                            )
                        }
                    }
                }
            } else {
                val limitReached = remainingFinds <= 0

                Button(
                    onClick = { onFreeFindClicked() },
                    enabled = !isBusyFinding && !limitReached,
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
                                text = if (limitReached) "Daily limit reached" else "Find nearby people",
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

                if (!limitReached) {
                    // FIXED: references the shared DAILY_FIND_LIMIT constant instead
                    // of a hardcoded "15" literal, so the displayed number can never
                    // drift out of sync with the actual limit enforced by the store.
                    Text(
                        text = "$remainingFinds of ${DailyFindLimitStore.DAILY_FIND_LIMIT} free finds left today",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (
                currentUserId.isNotBlank() &&
                latitude != null &&
                longitude != null
            ) {
                PopularNearYou(
                    userId = currentUserId,
                    longitude = longitude!!,
                    latitude = latitude!!,
                    onActivityClick = { nearby ->
                        activity = nearby.activity
                    }
                )
            } else {
                PopularNearYouPlaceholder(
                    onActivityClick = { title -> activity = title }
                )
            }
        }

        LaunchedEffect(isPro) {

            val shouldShowBubble = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                !isPro && dailyStore.shouldShowToday()
            } else {
                false
            }

            if (shouldShowBubble) {
                showLiveBubble = true
                dailyStore.markShownToday()
            } else {
                showLiveBubble = false
            }
        }

        if (showLiveBubble && !isPro) {
            DraggableProSubscriptionCard(
                snapToEdge = false,
                priceLabel = "Upgrade",
                onDismiss = { showLiveBubble = false },
                onClick = {
                    showLiveBubble = false
                    navController.navigate("subscription")
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
        if (showProfileScreen && currentProfile != null && userId != null) {
            ProfileDetailsScreen(
                profile = currentProfile!!,
                isPro = isPro,
                onBack = { showProfileScreen = false },
                onUpgradeClick = {
                    showProfileScreen = false
                    navController.navigate("subscription")
                },
                onLogout = {
                    UserPrefs.clear(context)
                    showProfileScreen = false
                    navController.navigate("signup") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    showDeleteDialog = true
                },

                onVerifyClick = {
                    showProfileScreen = false
                    navController.navigate("verification/$userId")
                }
            )
        }

        if (showDeleteDialog) {
            DeleteAccountConfirmationDialog(
                onConfirm = {
                    showDeleteDialog = false

                    val userId = UserPrefs.getUserId(context)
                    if (userId.isNullOrBlank()) {
                        Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
                        return@DeleteAccountConfirmationDialog
                    }

                    deleteAccountViewModel.deleteAccount(userId) { success, message ->
                        if (success) {
                            UserPrefs.clear(context) // wipe everything from shared prefs
                            showProfileScreen = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()


                            navController.navigate("signup") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }

        if (deleteState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // ==================== COMPLETE PROFILE DIALOG ====================
        if (showCompleteProfileDialog) {
            CompleteProfileDialog(
                updateProfileViewModel = updateProfileViewModel,
                onDismiss = { showCompleteProfileDialog = false },
                onSuccess = {
                    showCompleteProfileDialog = false
                    Toast.makeText(context, "Profile completed successfully!", Toast.LENGTH_SHORT).show()
                    // Refresh the header avatar with the freshly saved image
                    headerProfileImageUrl = UserPrefs.getProfileImageUrl(context)
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
                    persistSmartPreference(agePreference, genderPreference)
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

    } // <-- Outer Box ends HERE
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
    onDeleteAccount: () -> Unit,
    isPro: Boolean = false,
    onVerifyClick: () -> Unit = {} ,
            onUpgradeClick: () -> Unit = {},

) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val approvalViewModel: ApprovalViewModel = viewModel()

    val interests = profile.interests ?: emptyList()
    val ratingAverage = profile.rating?.average ?: 0.0
    val ratingCount = profile.feedbacks?.count?.takeIf { it > 0 }
        ?: (profile.rating?.count ?: 0)
    val feedbackMessages = profile.feedbacks?.list
        ?.map { it.message }
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    fun formatInterest(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }

    // ---------- Approval status from API ----------
    val storedUserId = UserPrefs.getUserId(context)
    Log.d("STORED_USER_ID", storedUserId ?: "none found")

    var approvalStatus by remember { mutableStateOf<String?>(null) }   // null = still loading

    LaunchedEffect(storedUserId) {
        if (!storedUserId.isNullOrBlank()) {
            approvalViewModel.getUserApproval(storedUserId) { success, message, approval ->
                Log.d(
                    "ApprovalResult",
                    "success=$success, message=$message, status=${approval?.approvalStatus}"
                )
                if (success) {
                    approvalStatus =
                        approval?.approvalStatus?.trim()?.lowercase() ?: "not_verified"
                }
            }
        }
    }

    val isApproved = approvalStatus == "approved"

    val cardStatus = when (approvalStatus) {
        "pending"  -> ApprovalPrefs.VerificationStatus.PENDING
        "rejected" -> ApprovalPrefs.VerificationStatus.REJECTED
        else       -> ApprovalPrefs.VerificationStatus.NOT_VERIFIED
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Decorative gradient behind the header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFE2BF), ScreenBg)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // ---------- Top bar ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "My Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Header: avatar, name, email ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(10.dp, CircleShape, spotColor = Orange.copy(alpha = 0.4f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Gold, Orange, GoldDeep))
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
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

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name ?: "No Name",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

                    // Verified tick (only when approved)
                    if (isApproved) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Image(
                            painter = painterResource(id = R.drawable.verifieduser),
                            contentDescription = "Verified",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isPro) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Pro member",
                            tint = GoldDeep,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (!profile.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profile.email,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Stats row ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.08f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStat(
                    modifier = Modifier.weight(1f),
                    value = if (ratingCount > 0) String.format("%.1f", ratingAverage) else "New",
                    label = "Rating",
                    showStar = ratingCount > 0
                )
                StatDivider()
                ProfileStat(
                    modifier = Modifier.weight(1f),
                    value = ratingCount.toString(),
                    label = "Reviews"
                )
                StatDivider()
                ProfileStat(
                    modifier = Modifier.weight(1f),
                    value = interests.size.toString(),
                    label = "Interests"
                )
            }

            // ---------- Identity verification pass ----------
            // shows only after the API answered AND the user is not approved
            if (approvalStatus != null && !isApproved) {
                Spacer(modifier = Modifier.height(18.dp))
                VerificationPassCard(
                    status = cardStatus,
                    onVerifyClick = onVerifyClick
                )
            }

//            Spacer(modifier = Modifier.height(18.dp))
//            VerificationPassCard(
//                status = cardStatus,
//                onVerifyClick = onVerifyClick
//            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Juttela Pro card ----------
            if (isPro) {
                ProMemberCard()
            } else {
                ProUpgradeCard(onUpgradeClick = onUpgradeClick)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Interests ----------
            SectionCard(title = "Interests") {
                if (interests.isEmpty()) {
                    Text(
                        text = "No interests added yet",
                        fontSize = 13.sp,
                        color = TextMuted
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
                                    .background(OrangeSoft)
                                    .border(
                                        1.dp,
                                        Orange.copy(alpha = 0.25f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = formatInterest(interest),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Orange
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- Personal info ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.06f))
                    .clip(RoundedCornerShape(20.dp))
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
                    value = profile.gender?.trim()?.replaceFirstChar { it.uppercase() } ?: "Not set"
                )
                ProfileDivider()
                ProfileInfoRow(
                    icon = Icons.Default.Info,
                    label = "About",
                    value = profile.about?.trim()?.takeIf { it.isNotBlank() } ?: "No bio yet"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- Feedback ----------
            SectionCard(title = "Feedback") {
                if (feedbackMessages.isEmpty()) {
                    Text(
                        text = "No feedback yet",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        feedbackMessages.forEach { message ->
                            FeedbackMessageCard(message = message)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ---------- Log out ----------
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextDark,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- Delete account ----------
            OutlinedButton(
                onClick = onDeleteAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD93025)),
                border = BorderStroke(1.2.dp, Color(0xFFD93025).copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Delete Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // ---------- Logout confirmation ----------
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Log out?",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "You'll need to sign in again to find people nearby.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = Orange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// ============================================================
// PRO CARDS
// ============================================================
@Composable
private fun ProUpgradeCard(onUpgradeClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                12.dp,
                RoundedCornerShape(22.dp),
                ambientColor = GoldDeep.copy(alpha = 0.4f),
                spotColor = GoldDeep.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E1A14), Color(0xFF3B2A0F))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(Gold.copy(alpha = 0.8f), GoldDeep.copy(alpha = 0.3f))),
                RoundedCornerShape(22.dp)
            )
            .clickable { onUpgradeClick() }
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Gold, GoldDeep))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFF3D2200),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Juttela Pro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold
                    )
                    Text(
                        text = "Meet the right people, faster",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ProBenefit("Unlimited daily finds")
            Spacer(modifier = Modifier.height(6.dp))
            ProBenefit("Smart age & gender matching")
            Spacer(modifier = Modifier.height(6.dp))
            ProBenefit("No ads, priority matches")

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Gold, Color(0xFFF7A93B), GoldDeep))),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Upgrade to Pro",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3D2200)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF3D2200),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProBenefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ProMemberCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                10.dp,
                RoundedCornerShape(20.dp),
                ambientColor = GoldDeep.copy(alpha = 0.4f),
                spotColor = GoldDeep.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Gold, Color(0xFFF7A93B), GoldDeep)))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = null,
            tint = Color(0xFF3D2200),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Juttela Pro Member",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3D2200)
            )
            Text(
                text = "Unlimited finds & smart matching unlocked",
                fontSize = 12.sp,
                color = Color(0xFF3D2200).copy(alpha = 0.8f)
            )
        }
    }
}

// ============================================================
// REUSABLE PIECES
// ============================================================
@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ProfileStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    showStar: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            if (showStar) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB400),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color(0xFFEDEDED))
    )
}

@Composable
private fun FeedbackMessageCard(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF8F1))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.FormatQuote,
            contentDescription = null,
            tint = Orange.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message.trim(),
            fontSize = 13.sp,
            color = Color(0xFF333333),
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
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
                .size(38.dp)
                .clip(CircleShape)
                .background(OrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        thickness = 1.dp,
        color = Color(0xFFF1F1F1)
    )
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