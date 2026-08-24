package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GetMessagesRequest
import com.example.juttela.DataSource.Models.Message
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GetConversationState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val messages: List<Message> = emptyList()
)

class GetConversationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(GetConversationState())
        private set

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun getConversation(currentUserId: String, otherUserId: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                state = state.copy(loading = true)
            }
            try {
                val response = repository.GetMessageRepo(
                    GetMessagesRequest(
                        currentUserId = currentUserId,
                        otherUserId = otherUserId
                    )
                )

                Log.d(
                    "GetConversationViewModel",
                    "Success: ${response.success}, count: ${response.data.messages.size}"
                )

                state = GetConversationState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    messages = response.data.messages
                )

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("GetConversationViewModel", "HTTP ${e.code()} fetching conversation. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("GetConversationViewModel", "Error fetching conversation: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                // On a silent poll failure, keep showing existing messages rather than wiping the screen
                state = state.copy(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )
            }
        }
    }

    // Starts polling every few seconds while the chat screen is open
    fun startPolling(currentUserId: String, otherUserId: String, intervalMs: Long = 3000L) {
        stopPolling() // avoid stacking multiple polling loops
        pollingJob = viewModelScope.launch {
            while (isActive) {
                getConversation(currentUserId, otherUserId, showLoading = false)
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}