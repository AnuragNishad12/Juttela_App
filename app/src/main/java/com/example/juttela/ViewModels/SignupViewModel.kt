package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.SignupRequest
import com.example.juttela.DataSource.Models.User
import com.example.juttela.Repository.AuthRepository
import com.example.juttela.Utils.UserPrefs
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SignupState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val user: User? = null
)

class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(SignupState())
        private set

    fun signup(
        name: String,
        email: String,
        password: String,
        mobileId: String
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                val response = repository.signup(
                    SignupRequest(
                        name,
                        email,
                        password,
                        mobileId.toString()
                    )
                )

                state = SignupState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    user = response.data
                )


                if (response.success) {
                    val userId = response.data?.id
                    val userName = response.data?.name

                    if (userId != null) {
                        UserPrefs.saveUserId(getApplication(), userId)
                        Log.d("SignupViewModel", "Saved userId locally: $userId")
                        try {
                            OneSignal.login(userId)
                            Log.d("SignupViewModel", "Linked userId to OneSignal: $userId")
                        } catch (e: Exception) {
                            Log.e("SignupViewModel", "OneSignal login failed: ${e.message}", e)
                        }
                    } else {
                        Log.e("SignupViewModel", "Signup succeeded but no userId found in response")
                    }

                    if (userName != null) {
                        UserPrefs.saveUserName(getApplication(), userName)
                        Log.d("SignupViewModel", "Saved userName locally: $userName")
                    }
                }

                Log.d(
                    "SignupViewModel",
                    "Response: success=${response.success}, message=${response.message}, user=${response.data}"
                )
            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SignupViewModel", "HTTP ${e.code()} error body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("SignupViewModel", "Error: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = SignupState(
                    loading = false,
                    message = errorMessage ?: "Unknown Error"
                )
            }
        }
    }
}