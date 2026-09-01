package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.SessionSnapshotResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SessionSnapshotState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val snapshot: SessionSnapshotResponse? = null
)

class SessionSnapshotViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(SessionSnapshotState())
        private set

    fun getSessionSnapshot(
        sessionId: String,
        onResult: (
            success: Boolean,
            message: String,
            snapshot: SessionSnapshotResponse?
        ) -> Unit
    ) {

        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "SessionSnapshotViewModel",
                    "Getting snapshot: sessionId=$sessionId"
                )

                val response = repository.GetSessionSnapshotRepo(
                    sessionId = sessionId
                )

                Log.d(
                    "SessionSnapshotViewModel",
                    "Snapshot received: users=${response.users.size}"
                )

                state = SessionSnapshotState(
                    loading = false,
                    success = true,
                    message = "Snapshot retrieved successfully",
                    snapshot = response
                )

                onResult(
                    true,
                    "Snapshot retrieved successfully",
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "SessionSnapshotViewModel",
                        "HTTP ${e.code()} getting snapshot. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "SessionSnapshotViewModel",
                        "Error getting snapshot: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = SessionSnapshotState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    snapshot = null
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