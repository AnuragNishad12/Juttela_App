package com.example.juttela.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import android.util.Log

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    var isLoading by mutableStateOf(false)
        private set

    var success by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun checkUserId(userId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d("AUTH_VM", "Request User ID: $userId")

                val response = repository.getUserById(
                    userId = userId
                )

                Log.d("AUTH_VM", "Success: ${response.success}")
                Log.d("AUTH_VM", "Message: ${response.message}")

                success = response.success
                message = response.message
            } catch (e: Exception) {
                Log.e("AUTH_VM", "API Error", e)
                success = false
                message = e.message ?: "Network Error"
            } finally {
                isLoading = false
            }
        }
    }
}