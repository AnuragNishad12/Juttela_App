package com.example.juttela.ViewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GetSmartConnection
import com.example.juttela.DataSource.Models.GetSmartConnectionsRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GetSmartConnectionsState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val connections: List<GetSmartConnection> = emptyList()
)

class GetSmartConnectionsViewModel : ViewModel() {

    private val repository = AuthRepository()

    var state by mutableStateOf(GetSmartConnectionsState())
        private set

    fun getSmartConnections(
        userId: String,
        onResult: ((success: Boolean, message: String, connections: List<GetSmartConnection>) -> Unit)? = null
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)

            try {
                Log.d("SMART_CONNECTIONS", "Fetching for userId=$userId")

                val response = repository.GetSmartConnectionsRequest(
                    GetSmartConnectionsRequest(userId = userId)
                )

                val connections = response.data.connections

                Log.d(
                    "SMART_CONNECTIONS",
                    "success=${response.success}, count=${connections.size}"
                )

                state = GetSmartConnectionsState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    connections = connections
                )

                onResult?.invoke(response.success, response.message, connections)
            } catch (e: Exception) {
                val message = if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    Log.e("SMART_CONNECTIONS", "HTTP ${e.code()} body=$body", e)
                    body ?: e.message()
                } else {
                    Log.e("SMART_CONNECTIONS", "Error: ${e.message}", e)
                    e.message
                } ?: "Something went wrong"

                state = GetSmartConnectionsState(
                    loading = false,
                    success = false,
                    message = message,
                    connections = emptyList()
                )

                onResult?.invoke(false, message, emptyList())
            }
        }
    }
}