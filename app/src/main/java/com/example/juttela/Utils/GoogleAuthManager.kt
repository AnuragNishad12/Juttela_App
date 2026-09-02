package com.example.juttela.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleAuthManager(
    private val context: Context
) {

    // Replace with your juttela project's WEB client ID
    // (the "Juttela Backend" one — Type: Web application)
    private val webClientId =
        "266941250229-r0p156r8ivk471njm2hlsi8iubqe4ejd.apps.googleusercontent.com"

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    /*
     * Returns the Intent to launch via
     * rememberLauncherForActivityResult in Compose.
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /*
     * Call this from the ActivityResult callback
     * to extract the ID token from the returned Intent data.
     */
    fun handleSignInResult(data: Intent?): Result<String> {

        return try {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val idToken = account.idToken

            Log.d("GOOGLE_SIGN_IN", "Email: ${account.email}")
            Log.d("GOOGLE_SIGN_IN", "Name: ${account.displayName}")

            if (idToken.isNullOrEmpty()) {

                Log.e("GOOGLE_SIGN_IN", "ID token is null or empty")
                Result.failure(Exception("Google returned an empty ID token"))

            } else {

                Log.d("GOOGLE_SIGN_IN", "ID Token received")
                Result.success(idToken)
            }

        } catch (e: ApiException) {

            Log.e(
                "GOOGLE_SIGN_IN",
                "Google sign in failed. StatusCode = ${e.statusCode}",
                e
            )

            Result.failure(Exception("Google Sign-In failed (code ${e.statusCode})"))

        } catch (e: Exception) {

            Log.e("GOOGLE_SIGN_IN", "Unexpected error", e)
            Result.failure(e)
        }
    }

    /*
     * Optional: call before sign-in if you want to force
     * the account picker every time instead of auto-selecting
     * the last used account.
     */
    fun signOut(onComplete: () -> Unit = {}) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }
}