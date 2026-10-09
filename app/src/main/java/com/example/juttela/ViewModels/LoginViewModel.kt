package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.LoginRequest
import com.example.juttela.DataSource.Models.LoginResponse
import com.example.juttela.DataSource.Models.UserDataLogin
import com.example.juttela.Repository.AuthRepository
import com.example.juttela.Utils.UserPrefs
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LoginState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val userId: String? = null,
    val user: UserDataLogin? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(LoginState())
        private set

    fun login(
        email: String,
        password: String,
        onResult: (
            success: Boolean,
            message: String,
            userId: String?
        ) -> Unit
    ) {

        viewModelScope.launch {

            state = state.copy(
                loading = true,
                message = ""
            )

            try {

                Log.d(
                    "LoginViewModel",
                    "Logging in with email: $email"
                )

                val response = repository.LoginDataModelRepo(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )

                Log.d(
                    "LoginViewModel",
                    "Login response: success=${response.success}, userId=${response.userId}"
                )

                if (response.success && response.userId != null) {

                    /*
                     * Clear previous user's data first
                     */
                    UserPrefs.clear(getApplication<Application>())

                    /*
                     * Store new user's ID
                     */
                    UserPrefs.saveUserId(
                        getApplication<Application>(),
                        response.userId
                    )

                    Log.d(
                        "LoginViewModel",
                        "New user ID saved: ${response.userId}"
                    )
                }

                state = LoginState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    userId = response.userId,
                    user = response.user
                )

                onResult(
                    response.success,
                    response.message,
                    response.userId
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "LoginViewModel",
                        "HTTP ${e.code()} login error. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "LoginViewModel",
                        "Login error: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = LoginState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(
                    false,
                    errorMessage ?: "Unknown Error",
                    null
                )
            }
        }
    }
}