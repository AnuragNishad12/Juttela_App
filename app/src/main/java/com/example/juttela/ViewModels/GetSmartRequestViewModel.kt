package com.example.juttela.ViewModels


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.SmartGetRequest
import com.example.juttela.DataSource.Models.SmartGetRequestBody
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch

data class GetSmartRequestState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val requests: List<SmartGetRequest> = emptyList()
)

class GetSmartRequestViewModel : ViewModel() {

    private val repository = AuthRepository()

    var state by mutableStateOf(GetSmartRequestState())
        private set

    fun getSmartRequests(userId: String) {
        viewModelScope.launch {
            state = state.copy(loading = true)

            try {
                Log.d("SMART_GET_REQUEST", "Fetching for userId=$userId")

                val response = repository.GetSmartRepo(
                    SmartGetRequestBody(userId = userId)
                )

                Log.d(
                    "SMART_GET_REQUEST",
                    "success=${response.success}, count=${response.data.requests.size}"
                )

                state = GetSmartRequestState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    requests = response.data.requests
                )
            } catch (e: Exception) {
                val message = if (e is retrofit2.HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    Log.e("SMART_GET_REQUEST", "HTTP ${e.code()} body=$body", e)
                    body ?: e.message()
                } else {
                    Log.e("SMART_GET_REQUEST", "Error: ${e.message}", e)
                    e.message
                } ?: "Something went wrong"

                state = GetSmartRequestState(
                    loading = false,
                    success = false,
                    message = message,
                    requests = emptyList()
                )
            }
        }
    }
}