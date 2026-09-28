package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.RejectRequestBody
import com.example.juttela.DataSource.Models.RejectRequestData
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RejectSmartRequestState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val data: RejectRequestData? = null
)

class RejectSmartRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(RejectSmartRequestState())
        private set

    fun rejectSmartRequest(
        currentUserId: String,
        requestId: String,
        onResult: (success: Boolean, message: String, data: RejectRequestData?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d(
                    "RejectSmartRequestVM",
                    "Rejecting smart request: requestId=$requestId, currentUserId=$currentUserId"
                )

                val response = repository.RejectSmartRequestRepo(
                    RejectRequestBody(
                        currentUserId = currentUserId,
                        requestId = requestId
                    )
                )

                Log.d(
                    "RejectSmartRequestVM",
                    "Success: ${response.success}, otherUser: ${response.data?.otherUserName}"
                )

                state = RejectSmartRequestState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    data = response.data
                )

                onResult(response.success, response.message, response.data)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "RejectSmartRequestVM",
                        "HTTP ${e.code()} rejecting smart request. Body: $errorBody"
                    )
                    errorBody ?: e.message()
                } else {
                    Log.e("RejectSmartRequestVM", "Error rejecting smart request: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = RejectSmartRequestState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}