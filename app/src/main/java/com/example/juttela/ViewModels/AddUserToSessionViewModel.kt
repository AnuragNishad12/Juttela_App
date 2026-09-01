package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.AddUserToSessionRequest
import com.example.juttela.DataSource.Models.AddUserToSessionResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AddUserToSessionState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val session: AddUserToSessionResponse? = null
)

class AddUserToSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(AddUserToSessionState())
        private set

    fun addUserToSession(
        sessionId: String,
        userId: String,
        onResult: (
            success: Boolean,
            message: String,
            session: AddUserToSessionResponse?
        ) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "AddUserToSessionViewModel",
                    "Adding user: sessionId=$sessionId, userId=$userId"
                )

                val response = repository.AddUserToSessionRepo(
                    sessionId = sessionId,
                    request = AddUserToSessionRequest(
                        userId = userId
                    )
                )

                Log.d(
                    "AddUserToSessionViewModel",
                    "Success: attached=${response.attached}, userId=${response.userId}"
                )

                state = AddUserToSessionState(
                    loading = false,
                    success = response.attached,
                    message = if (response.attached) {
                        "User added to session successfully"
                    } else {
                        "Failed to add user to session"
                    },
                    session = response
                )

                onResult(
                    response.attached,
                    state.message,
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "AddUserToSessionViewModel",
                        "HTTP ${e.code()} adding user. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "AddUserToSessionViewModel",
                        "Error adding user: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = AddUserToSessionState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    session = null
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