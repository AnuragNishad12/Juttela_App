package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GoogleAuthRequest
import com.example.juttela.DataSource.Models.UserAuthData
import com.example.juttela.Repository.AuthRepository
import com.example.juttela.Utils.UserPrefs
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GoogleAuthState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val userId: String? = null,
    val user: UserAuthData? = null
)

class GoogleAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(GoogleAuthState())
        private set

    fun googleAuth(
        idToken: String,
        onResult: (
            success: Boolean,
            message: String,
            userId: String?,
            user: UserAuthData?
        ) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(
                loading = true,
                message = ""
            )

            try {

                Log.d(
                    "GoogleAuthViewModel",
                    "Starting Google authentication"
                )

                val response = repository.GoogleAuthRepo(
                    GoogleAuthRequest(
                        idToken = idToken
                    )
                )

                Log.d(
                    "GoogleAuthViewModel",
                    "Google auth success=${response.success}, " +
                            "message=${response.message}, " +
                            "userId=${response.userId}"
                )

                if (response.success) {

                    response.userId?.let { userId ->
                        UserPrefs.saveUserId(getApplication(), userId)
                        Log.d("UserPrefs", "User ID saved: $userId")
                    } ?: run {
                        Log.d("UserPrefs", "User ID NOT saved: userId is null")
                    }

                    response.user?.name?.let { name ->
                        UserPrefs.saveUserName(getApplication(), name)
                        Log.d("UserPrefs", "User name saved: $name")
                    } ?: run {
                        Log.d("UserPrefs", "User name NOT saved: name is null")
                    }
                }

                state = GoogleAuthState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    userId = response.userId,
                    user = response.user
                )

                onResult(
                    response.success,
                    response.message,
                    response.userId,
                    response.user
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e
                        .response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "GoogleAuthViewModel",
                        "HTTP ${e.code()} Google authentication failed. " +
                                "Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "GoogleAuthViewModel",
                        "Google authentication error: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = GoogleAuthState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    userId = null,
                    user = null
                )

                onResult(
                    false,
                    errorMessage ?: "Unknown Error",
                    null,
                    null
                )
            }
        }
    }
}