package com.example.juttela.Screens

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.juttela.R
import com.example.juttela.Utils.GoogleAuthManager
import com.example.juttela.ViewModels.GoogleAuthViewModel
import com.example.juttela.ViewModels.SignupViewModel

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignupViewModel = viewModel(),
    googleAuthViewModel: GoogleAuthViewModel = viewModel(),
    googleAuthManager: GoogleAuthManager
) {

    val context = LocalContext.current

    val signupState = viewModel.state
    val googleState = googleAuthViewModel.state
    var passwordVisible by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    /*
     * Local loading flag for the Google flow.
     * Set true the instant the button is tapped (before the
     * account picker Activity even launches), cleared once
     * a result comes back and the backend call finishes.
     */
    var isGoogleFlowLoading by remember { mutableStateOf(false) }

    val isLoading = signupState.loading || googleState.loading || isGoogleFlowLoading

    /*
     * Legacy Google Sign-In uses an Activity Result launcher
     * instead of a suspend function — this replaces
     * startActivityForResult/onActivityResult from the old
     * Activity-based approach.
     */
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode != Activity.RESULT_OK) {

            Log.d("GoogleAuth", "Google sign-in cancelled or failed, resultCode=${result.resultCode}")
            isGoogleFlowLoading = false
            return@rememberLauncherForActivityResult
        }

        val signInResult = googleAuthManager.handleSignInResult(result.data)

        signInResult.onSuccess { idToken ->

            Log.d("GoogleAuth", "Google ID token received")

            /*
             * Send ID token to backend.
             * isGoogleFlowLoading stays true here; googleState.loading
             * takes over to keep the spinner going during the backend call.
             */
            googleAuthViewModel.googleAuth(
                idToken = idToken
            ) { success, message, userId, user ->

                Log.d("GoogleAuth", "Backend success=$success")
                Log.d("GoogleAuth", "Backend message=$message")
                Log.d("GoogleAuth", "Backend userId=$userId")

                if (success) {
                    Log.d("GoogleAuth", "User=${user?.name}")
                }

                isGoogleFlowLoading = false
            }

        }.onFailure { e ->

            Log.e("GoogleAuth", "Google sign-in error", e)

            Toast.makeText(
                context,
                e.message ?: "Google sign-in failed",
                Toast.LENGTH_SHORT
            ).show()

            isGoogleFlowLoading = false
        }
    }

    /*
     * Navigate after successful normal signup
     * OR successful Google authentication.
     */
    LaunchedEffect(signupState.success, googleState.success) {
        if (signupState.success || googleState.success) {
            navController.navigate("main") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    LaunchedEffect(signupState.message) {
        if (signupState.message.isNotEmpty()) {
            Toast.makeText(context, signupState.message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(googleState.message) {
        if (googleState.message.isNotEmpty()) {
            Toast.makeText(context, googleState.message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Create Your Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * NAME
             */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter Your Full Name") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * EMAIL
             */
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Your Email") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * PASSWORD
             */
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Your Password") },
                singleLine = true,
                enabled = !isLoading,

                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },

                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),

                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * NORMAL SIGNUP
             */
            Button(
                onClick = {

                    val mobileId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                    Log.d(
                        "SIGNUP_DEBUG",
                        "name=$name, email=$email, mobileId=$mobileId"
                    )

                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.signup(name, email, password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7B00),
                    contentColor = Color.White
                )
            ) {
                if (signupState.loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * OR
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            /*
             * ==============================
             * CONTINUE WITH GOOGLE
             * ==============================
             */
            Button(
                onClick = {

                    // Guard against double-tap starting a second flow
                    if (isGoogleFlowLoading) return@Button

                    isGoogleFlowLoading = true

                    Log.d("GoogleAuth", "Google button clicked")

                    val signInIntent = googleAuthManager.getSignInIntent()
                    googleSignInLauncher.launch(signInIntent)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8E8E8),
                    contentColor = Color.Black
                )
            ) {
                if (isGoogleFlowLoading || googleState.loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Continue with Google",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}