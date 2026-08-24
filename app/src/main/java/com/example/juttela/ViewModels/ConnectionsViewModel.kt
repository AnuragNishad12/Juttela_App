package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.Connection
import com.example.juttela.DataSource.Models.ConnectionsRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ConnectionsState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val connections: List<Connection> = emptyList()
)

class ConnectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(ConnectionsState())
        private set

    fun getMyConnections(userId: String) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d("ConnectionsViewModel", "Fetching connections for userId=$userId")

                val response = repository.GetConnections(
                    ConnectionsRequest(userId = userId)
                )

                Log.d(
                    "ConnectionsViewModel",
                    "Success: ${response.success}, count: ${response.data.connections.size}"
                )

                state = ConnectionsState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    connections = response.data.connections
                )

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("ConnectionsViewModel", "HTTP ${e.code()} fetching connections. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("ConnectionsViewModel", "Error fetching connections: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = ConnectionsState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )
            }
        }
    }
}