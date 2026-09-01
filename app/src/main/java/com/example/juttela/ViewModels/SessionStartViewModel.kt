package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.LocationPinRequest
import com.example.juttela.DataSource.Models.LocationSessionResponse
import com.example.juttela.DataSource.Models.LocationSessionStartRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SessionStartState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val session: LocationSessionResponse? = null
)

class SessionStartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(SessionStartState())
        private set

    fun startSession(
        lat: Double,
        lng: Double,
        invitedBy: String? = null,
        onResult: (
            success: Boolean,
            message: String,
            session: LocationSessionResponse?
        ) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)

            try {
                Log.d(
                    "SessionStartViewModel",
                    "Starting session: lat=$lat, lng=$lng, invitedBy=$invitedBy"
                )

                val response = repository.SessionsStartRepo(
                    LocationSessionStartRequest(
                        pin = LocationPinRequest(
                            lat = lat,
                            lng = lng
                        ),
                        invitedBy = invitedBy
                    )
                )

                Log.d(
                    "SessionStartViewModel",
                    "Session created: sessionId=${response.sessionId}, invitedBy=${response.invitedBy}"
                )

                state = SessionStartState(
                    loading = false,
                    success = true,
                    message = "Session started successfully",
                    session = response
                )

                onResult(true, "Session started successfully", response)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SessionStartViewModel", "HTTP ${e.code()} starting session. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("SessionStartViewModel", "Error starting session: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = SessionStartState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    session = null
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}