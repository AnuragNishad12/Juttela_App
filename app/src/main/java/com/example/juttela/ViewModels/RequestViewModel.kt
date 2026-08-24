package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GetIdRequest
import com.example.juttela.DataSource.Models.RequestModels
import com.example.juttela.DataSource.Models.UserDataRequest
import com.example.juttela.DataSource.Models.UserRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SendRequestState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val requests: List<UserDataRequest> = emptyList()
)

data class GetRequestsState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val requests: List<UserRequest> = emptyList()
)

class RequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var sendState by mutableStateOf(SendRequestState())
        private set

    var getState by mutableStateOf(GetRequestsState())
        private set

    fun sendRequest(
        senderId: String,
        senderName: String,
        recipientId: String,
        activity: String,
        distanceKm: Double,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            sendState = sendState.copy(loading = true)
            try {
                Log.d("RequestViewModel", "Sending request: senderId=$senderId, recipientId=$recipientId, activity=$activity, distanceKm=$distanceKm")

                val response = repository.SendRequestRepo(
                    RequestModels(
                        senderId = senderId,
                        senderName = senderName,
                        recipientId = recipientId,
                        activity = activity,
                        distanceKm = distanceKm
                    )
                )

                Log.d("RequestViewModel", "Send response: success=${response.success}, message=${response.message}")
                Log.d("RequestViewModel", "Recipient's full request list now: ${response.data.requests}")

                sendState = SendRequestState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    requests = response.data.requests
                )

                onResult(response.success, response.message)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("RequestViewModel", "HTTP ${e.code()} sending request. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("RequestViewModel", "Error sending request: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                sendState = SendRequestState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error")
            }
        }
    }

    fun getMyRequests(userId: String) {
        viewModelScope.launch {
            getState = getState.copy(loading = true)
            try {
                Log.d("RequestViewModel", "Fetching requests for userId=$userId")

                val response = repository.GetIdResponseRepo(
                    GetIdRequest(userId = userId)
                )

                Log.d("RequestViewModel", "GetRequests response: success=${response.success}, count=${response.data.requests.size}")

                getState = GetRequestsState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    requests = response.data.requests
                )

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("RequestViewModel", "HTTP ${e.code()} fetching requests. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("RequestViewModel", "Error fetching requests: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                getState = GetRequestsState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )
            }
        }
    }
}