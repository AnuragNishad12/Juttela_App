package com.example.juttela.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.SmartAcceptData
import com.example.juttela.DataSource.Models.SmartAcceptRequest
import com.example.juttela.DataSource.Models.SmartAcceptResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SmartAcceptState(
    val isLoading: Boolean = false,
    val response: SmartAcceptResponse? = null,
    val error: String? = null
)

class SmartAcceptViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _state = MutableStateFlow(SmartAcceptState())
    val state: StateFlow<SmartAcceptState> = _state

    fun acceptSmartRequest(
        currentUserId: String,
        currentUserName: String,
        requestId: String,
        onResult: (success: Boolean, message: String, data: SmartAcceptData?) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val request = SmartAcceptRequest(
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    requestId = requestId
                )

                Log.d("SMART_ACCEPT", "Request: $request")

                val response = repository.SmartAcceptRequestRepo(request)

                Log.d(
                    "SMART_ACCEPT",
                    "Success=${response.success}, message=${response.message}, data=${response.data}"
                )

                _state.value = SmartAcceptState(
                    isLoading = false,
                    response = response,
                    error = null
                )

                onResult(response.success, response.message, response.data)
            } catch (e: Exception) {
                val message = if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    Log.e("SMART_ACCEPT", "HTTP ${e.code()} body=$body", e)
                    body ?: e.message()
                } else {
                    Log.e("SMART_ACCEPT", "Error: ${e.message}", e)
                    e.message
                } ?: "Something went wrong"

                _state.value = SmartAcceptState(
                    isLoading = false,
                    response = null,
                    error = message
                )

                onResult(false, message, null)
            }
        }
    }
}