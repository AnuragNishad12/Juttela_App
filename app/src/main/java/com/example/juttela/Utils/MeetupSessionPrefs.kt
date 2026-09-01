package com.example.juttela.Utils

import android.content.Context

data class ActiveMeetup(
    val sessionId: String,
    val otherUserId: String,
    val pinLat: Double,
    val pinLng: Double,
    val otherLat: Double?,
    val otherLng: Double?
)

object MeetupSessionPrefs {
    private const val PREF = "meetup_session_prefs"
    private const val KEY_SESSION_ID = "active_session_id"
    private const val KEY_OTHER_USER_ID = "active_other_user_id"
    private const val KEY_PIN_LAT = "pin_lat"
    private const val KEY_PIN_LNG = "pin_lng"
    private const val KEY_OTHER_LAT = "other_lat"
    private const val KEY_OTHER_LNG = "other_lng"
    private const val KEY_CANCELLED_IDS = "cancelled_session_ids"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun save(
        context: Context,
        sessionId: String,
        otherUserId: String,
        pinLat: Double,
        pinLng: Double,
        otherLat: Double? = null,
        otherLng: Double? = null
    ) {
        prefs(context).edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putString(KEY_OTHER_USER_ID, otherUserId)
            .putString(KEY_PIN_LAT, pinLat.toString())
            .putString(KEY_PIN_LNG, pinLng.toString())
            .putString(KEY_OTHER_LAT, otherLat?.toString() ?: "")
            .putString(KEY_OTHER_LNG, otherLng?.toString() ?: "")
            .apply()
    }

    fun save(context: Context, sessionId: String, otherUserId: String) {
        val existing = getForUser(context, otherUserId)
        save(
            context = context,
            sessionId = sessionId,
            otherUserId = otherUserId,
            pinLat = existing?.pinLat ?: 0.0,
            pinLng = existing?.pinLng ?: 0.0,
            otherLat = existing?.otherLat,
            otherLng = existing?.otherLng
        )
    }

    fun getForUser(context: Context, otherUserId: String): ActiveMeetup? {
        val p = prefs(context)
        if (p.getString(KEY_OTHER_USER_ID, null) != otherUserId) return null
        val sessionId = p.getString(KEY_SESSION_ID, null) ?: return null
        val pinLat = p.getString(KEY_PIN_LAT, null)?.toDoubleOrNull() ?: return null
        val pinLng = p.getString(KEY_PIN_LNG, null)?.toDoubleOrNull() ?: return null
        return ActiveMeetup(
            sessionId = sessionId,
            otherUserId = otherUserId,
            pinLat = pinLat,
            pinLng = pinLng,
            otherLat = p.getString(KEY_OTHER_LAT, null)?.toDoubleOrNull(),
            otherLng = p.getString(KEY_OTHER_LNG, null)?.toDoubleOrNull()
        )
    }

    fun getBySessionId(context: Context, sessionId: String): ActiveMeetup? {
        val p = prefs(context)
        if (p.getString(KEY_SESSION_ID, null) != sessionId) return null
        val otherUserId = p.getString(KEY_OTHER_USER_ID, null) ?: return null
        return getForUser(context, otherUserId)
    }

    fun getSessionIdForUser(context: Context, otherUserId: String): String? {
        return getForUser(context, otherUserId)?.sessionId
    }

    fun clear(context: Context) {
        val cancelled = prefs(context).getStringSet(KEY_CANCELLED_IDS, emptySet()) ?: emptySet()
        prefs(context).edit()
            .clear()
            .putStringSet(KEY_CANCELLED_IDS, cancelled.toSet())
            .apply()
    }

    fun markCancelled(context: Context, sessionId: String) {
        val next = (prefs(context).getStringSet(KEY_CANCELLED_IDS, emptySet()) ?: emptySet()) + sessionId
        prefs(context).edit()
            .putStringSet(KEY_CANCELLED_IDS, next)
            .apply()
    }

    fun wasCancelled(context: Context, sessionId: String): Boolean {
        return prefs(context).getStringSet(KEY_CANCELLED_IDS, emptySet())
            ?.contains(sessionId) == true
    }
}