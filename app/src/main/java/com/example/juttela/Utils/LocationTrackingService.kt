package com.example.juttela.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.juttela.DataSource.Models.UpdateSessionLocationRequest
import com.example.juttela.Repository.AuthRepository
import com.example.juttela.Utils.MeetupSessionPrefs
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

/**
 * Runs independently of any Activity/Compose screen — survives the app
 * being backgrounded or swiped away. Sends this user's GPS to the
 * session's /location endpoint every ~30s until the server reports
 * `arrived == true`, the session no longer exists (404), or until
 * explicitly stopped (user cancels the meetup).
 */
class LocationTrackingService : Service() {

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 5001
        private const val UPDATE_INTERVAL_MS = 30_000L

        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_USER_ID = "extra_user_id"

        const val ACTION_START = "com.example.juttela.action.START_TRACKING"
        const val ACTION_STOP = "com.example.juttela.action.STOP_TRACKING"

        const val ACTION_ARRIVED = "com.example.juttela.action.LOCATION_ARRIVED"
        const val ACTION_LOCATION_UPDATED = "com.example.juttela.action.LOCATION_UPDATED"
        const val EXTRA_DISTANCE = "extra_distance"

        // Sent when the server reports the session no longer exists (404) —
        // the UI listens for this to clear its local activeSessionId/prefs
        // so it never tries to restart tracking for a dead session again.
        const val ACTION_SESSION_ENDED = "com.example.juttela.action.SESSION_ENDED"

        fun start(context: Context, sessionId: String, userId: String) {
            Log.d(TAG, "start() called for session=$sessionId user=$userId")
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_USER_ID, userId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val repository = AuthRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sessionId: String? = null
    private var userId: String? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ------------------------------------------------------------
        // CRITICAL FIX: START_STICKY services can be restarted by the
        // system with intent == null after the process is killed. If we
        // don't call startForeground() unconditionally in that path, the
        // OS still expects a foreground promotion (since this service was
        // previously foreground) and kills the app 5s later with
        // ForegroundServiceDidNotStartInTimeException. So we ALWAYS start
        // foreground first, using whatever session info we have — falling
        // back to persisted prefs on a system-triggered restart — and
        // only decide afterward whether there's anything to actually track.
        // ------------------------------------------------------------

        val action = intent?.action

        // Try to recover session/user id: from the intent extras normally,
        // or from persisted prefs if this is an OS restart with intent == null
        // or a stray restart with a stripped action.
        val recoveredSessionId = intent?.getStringExtra(EXTRA_SESSION_ID)
            ?: sessionId
            ?: MeetupSessionPrefs.let { prefs ->
                // MeetupSessionPrefs is keyed by otherUserId, not sessionId,
                // so we can only recover if we already cached it in-memory
                // from a previous onStartCommand this process lifetime.
                null
            }
        val recoveredUserId = intent?.getStringExtra(EXTRA_USER_ID) ?: userId

        try {
            startForegroundCompat(buildNotification("Tracking your location…"))
        } catch (e: Exception) {
            Log.e(TAG, "startForeground FAILED: ${e.message}", e)
            stopSelf()
            return START_NOT_STICKY
        }

        when (action) {
            ACTION_STOP -> {
                Log.d(TAG, "Stop requested")
                stopTracking()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                if (recoveredSessionId == null || recoveredUserId == null) {
                    Log.e(TAG, "Missing sessionId or userId on ACTION_START, stopping service")
                    stopTracking()
                    return START_NOT_STICKY
                }
                sessionId = recoveredSessionId
                userId = recoveredUserId
                Log.d(TAG, "Foreground service started for session=$recoveredSessionId user=$recoveredUserId")
                startTrackingLoop()
            }

            else -> {
                // System-triggered restart (intent == null) or an unknown
                // action. If we don't have enough state in memory to resume
                // tracking, there's nothing safe to do — we already called
                // startForeground() above to satisfy the contract, so just
                // shut down cleanly instead of running a zombie service.
                if (sessionId == null || userId == null) {
                    Log.w(TAG, "Restarted with no recoverable session state — stopping")
                    stopTracking()
                    return START_NOT_STICKY
                } else {
                    // We still have session state from before the restart —
                    // resume tracking.
                    startTrackingLoop()
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startTrackingLoop() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (true) {
                val currentSessionId = sessionId
                val currentUserId = userId
                if (currentSessionId == null || currentUserId == null) break

                try {
                    val location = getCurrentLocation()
                    if (location != null) {
                        val sessionStillActive = sendLocationUpdate(currentSessionId, currentUserId, location)
                        if (!sessionStillActive) {
                            // 404 or arrival — loop must stop, stopTracking()
                            // was already called inside sendLocationUpdate
                            break
                        }
                    } else {
                        Log.w(TAG, "Could not get a location fix this cycle")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in tracking loop: ${e.message}", e)
                }

                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        return try {
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            fusedLocationClient.getCurrentLocation(request, null).await()
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing: ${e.message}")
            null
        }
    }

    // returns false if the session is over (404, or arrived) and the
    // tracking loop should stop; true if it should keep going
    private suspend fun sendLocationUpdate(sessionId: String, userId: String, location: Location): Boolean {
        try {
            val response = repository.UpdateSessionLocationRepo(
                sessionId = sessionId,
                request = UpdateSessionLocationRequest(
                    userId = userId,
                    lat = location.latitude,
                    lng = location.longitude
                )
            )

            Log.d(
                TAG,
                "Update sent: distance=${response.distanceMeters}, arrived=${response.arrived}, justArrived=${response.justArrived}"
            )

            updateNotification("Distance to pin: ${response.distanceMeters.toInt()}m")

            sendBroadcast(Intent(ACTION_LOCATION_UPDATED).apply {
                putExtra(EXTRA_DISTANCE, response.distanceMeters)
                setPackage(packageName)
            })

            if (response.arrived) {
                Log.d(TAG, "Arrived — stopping tracking")
                sendBroadcast(Intent(ACTION_ARRIVED).apply { setPackage(packageName) })
                updateNotification("You've arrived at the pin")
                stopTracking()
                return false
            }

            return true

        } catch (e: HttpException) {
            if (e.code() == 404) {
                // session was cancelled or expired server-side — this is
                // TERMINAL, not a transient failure. Retrying every 30s
                // forever would just spam 404s. Stop and tell the UI.
                Log.e(TAG, "Session no longer exists (404) — stopping tracking for $sessionId")
                sendBroadcast(Intent(ACTION_SESSION_ENDED).apply {
                    putExtra(EXTRA_SESSION_ID, sessionId)
                    setPackage(packageName)
                })
                stopTracking()
                return false
            }
            Log.e(TAG, "Failed to send location update: HTTP ${e.code()}", e)
            return true // other HTTP errors: retry next cycle
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send location update: ${e.message}", e)
            return true // network hiccup etc: retry next cycle
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        sessionId = null
        userId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Meetup location tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows while your location is being shared for an active meetup"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        // getLaunchIntentForPackage can return null (e.g. no LAUNCHER
        // activity resolved yet) — guard it so buildNotification never
        // throws inside the startForeground() call path.
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (openAppIntent != null) {
            PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Meetup in progress")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(contentText))
    }
}