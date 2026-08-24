package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.MessageData
import com.example.juttela.DataSource.Models.SendMessageRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SendMessageState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val lastSentMessage: MessageData? = null
)

class SendMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(SendMessageState())
        private set

    fun sendMessage(
        senderId: String,
        senderName: String,
        receiverId: String,
        text: String,
        onResult: (success: Boolean, message: String, data: MessageData?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d(
                    "SendMessageViewModel",
                    "Sending message: senderId=$senderId, senderName=$senderName, receiverId=$receiverId, text=$text"
                )

                val response = repository.SendMessageRepo(
                    SendMessageRequest(
                        senderId = senderId,
                        senderName = senderName,
                        receiverId = receiverId,
                        text = text
                    )
                )

                Log.d(
                    "SendMessageViewModel",
                    "Success: ${response.success}, messageId: ${response.data.id}"
                )

                state = SendMessageState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    lastSentMessage = response.data
                )

                onResult(response.success, response.message, response.data)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SendMessageViewModel", "HTTP ${e.code()} sending message. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("SendMessageViewModel", "Error sending message: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = SendMessageState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}