package com.example.juttela.Utils
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

class DailyProCardStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("daily_pro_card", Context.MODE_PRIVATE)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun today(): String =
        java.time.LocalDate.now().toString() // "2026-09-05"

    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldShowToday(): Boolean =
        prefs.getString("last_shown_date", null) != today()

    @RequiresApi(Build.VERSION_CODES.O)
    fun markShownToday() {
        prefs.edit().putString("last_shown_date", today()).apply()
    }
}

class DailyFindLimitStore(context: Context) {

    companion object {
        const val DAILY_FIND_LIMIT = 15
    }

    private val prefs = context.applicationContext
        .getSharedPreferences("daily_find_limit", Context.MODE_PRIVATE)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun today(): String = java.time.LocalDate.now().toString()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ensureToday() {
        val storedDate = prefs.getString("date", null)
        if (storedDate != today()) {
            prefs.edit()
                .putString("date", today())
                .putInt("count", 0)
                .apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun remaining(limit: Int = DAILY_FIND_LIMIT): Int {
        ensureToday()
        return (limit - prefs.getInt("count", 0)).coerceAtLeast(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun canUse(limit: Int = DAILY_FIND_LIMIT): Boolean = remaining(limit) > 0

    @RequiresApi(Build.VERSION_CODES.O)
    fun recordUse() {
        ensureToday()
        prefs.edit()
            .putInt("count", prefs.getInt("count", 0) + 1)
            .apply()
    }
}

class DailyChatLimitStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("daily_chat_limits", Context.MODE_PRIVATE)

    companion object {
        const val MESSAGE_LIMIT = 50
        const val LOCATION_LIMIT = 5
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun today(): String = java.time.LocalDate.now().toString()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ensureToday() {
        if (prefs.getString("date", null) != today()) {
            prefs.edit()
                .putString("date", today())
                .putInt("messages", 0)
                .putInt("locations", 0)
                .apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun remainingMessages(): Int {
        ensureToday()
        return (MESSAGE_LIMIT - prefs.getInt("messages", 0)).coerceAtLeast(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun remainingLocations(): Int {
        ensureToday()
        return (LOCATION_LIMIT - prefs.getInt("locations", 0)).coerceAtLeast(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun canSendMessage(): Boolean = remainingMessages() > 0
    @RequiresApi(Build.VERSION_CODES.O)
    fun canSendLocation(): Boolean = remainingLocations() > 0

    @RequiresApi(Build.VERSION_CODES.O)
    fun recordMessage() {
        ensureToday()
        prefs.edit().putInt("messages", prefs.getInt("messages", 0) + 1).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun recordLocation() {
        ensureToday()
        prefs.edit().putInt("locations", prefs.getInt("locations", 0) + 1).apply()
    }
}