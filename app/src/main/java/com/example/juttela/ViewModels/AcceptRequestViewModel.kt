package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.AcceptUsersModel
import com.example.juttela.DataSource.Models.ConnectionData
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AcceptRequestState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val connection: ConnectionData? = null
)

class AcceptRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(AcceptRequestState())
        private set

    fun acceptRequest(
        currentUserId: String,
        currentUserName: String,
        requestId: String,
        onResult: (success: Boolean, message: String, connection: ConnectionData?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d(
                    "AcceptRequestViewModel",
                    "Accepting request: requestId=$requestId, currentUserId=$currentUserId"
                )

                val response = repository.AcceptRequest(
                    AcceptUsersModel(
                        currentUserId = currentUserId,
                        currentUserName = currentUserName,
                        requestId = requestId
                    )
                )

                Log.d(
                    "AcceptRequestViewModel",
                    "Success: ${response.success}, connectedWith: ${response.data.otherUserName}"
                )

                state = AcceptRequestState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    connection = response.data
                )

                onResult(response.success, response.message, response.data)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("AcceptRequestViewModel", "HTTP ${e.code()} accepting request. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("AcceptRequestViewModel", "Error accepting request: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = AcceptRequestState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}