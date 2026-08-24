package com.example.juttela.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.SendingSmartRequest
import com.example.juttela.DataSource.Models.SendingSmartRequestResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SmartRequestState(
    val isLoading: Boolean = false,
    val response: SendingSmartRequestResponse? = null,
    val error: String? = null
)

class SmartRequestViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _state = MutableStateFlow(SmartRequestState())
    val state: StateFlow<SmartRequestState> = _state

    fun sendSmartRequest(
        request: SendingSmartRequest,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                Log.d("SMART_REQUEST", "Sending: $request")

                val response = repository.SendSmartRequest(request)

                Log.d(
                    "SMART_REQUEST",
                    "Success=${response.success}, message=${response.message}"
                )

                _state.value = SmartRequestState(
                    isLoading = false,
                    response = response,
                    error = null
                )

                onResult(response.success, response.message)
            } catch (e: Exception) {
                val message = if (e is retrofit2.HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    Log.e("SMART_REQUEST", "HTTP ${e.code()} body=$body", e)
                    body ?: e.message()
                } else {
                    Log.e("SMART_REQUEST", "Error sending smart request", e)
                    e.message
                } ?: "Something went wrong"

                _state.value = SmartRequestState(
                    isLoading = false,
                    response = null,
                    error = message
                )

                onResult(false, message)
            }
        }
    }
}