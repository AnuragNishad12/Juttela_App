package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.CancelSessionResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CancelSessionState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val response: CancelSessionResponse? = null
)

class CancelSessionViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(CancelSessionState())
        private set

    fun cancelSession(
        sessionId: String,
        onResult: (success: Boolean, message: String, response: CancelSessionResponse?) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "CancelSessionViewModel",
                    "Cancelling session: sessionId=$sessionId"
                )

                val response = repository.CancelSessionRepo(
                    sessionId = sessionId
                )

                Log.d(
                    "CancelSessionViewModel",
                    "Cancel session success: ${response.cancelled}"
                )

                state = CancelSessionState(
                    loading = false,
                    success = response.cancelled,
                    message = if (response.cancelled) {
                        "Session cancelled successfully"
                    } else {
                        "Failed to cancel session"
                    },
                    response = response
                )

                onResult(
                    response.cancelled,
                    state.message,
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "CancelSessionViewModel",
                        "HTTP ${e.code()} cancelling session. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "CancelSessionViewModel",
                        "Error cancelling session: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = CancelSessionState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    response = null
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