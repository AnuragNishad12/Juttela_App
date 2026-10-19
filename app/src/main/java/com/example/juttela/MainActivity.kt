package com.example.juttela

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppNavigation()
        }

        checkForUpdate()
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                showUpdateDialog()
            }
        }
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle("New Update Available")
            .setMessage("A new version of the app is available. Please update from Google Play to get the latest features and improvements.")
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                openPlayStore()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openPlayStore() {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=com.anurag.juttela&hl=en_IN"
        try {
            // Try opening in Play Store app
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.anurag.juttela"))
            )
        } catch (e: Exception) {
            // Fallback to the exact URL you provided
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            )
        }
    }
}