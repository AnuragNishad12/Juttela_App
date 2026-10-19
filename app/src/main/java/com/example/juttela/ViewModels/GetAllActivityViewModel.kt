package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GetAllActivity
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GetAllActivityState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val response: GetAllActivity? = null
)

class GetAllActivityViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(GetAllActivityState())
        private set

    fun getAllActivities(
        onResult: (success: Boolean, message: String, response: GetAllActivity?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, message = "")

            try {
                Log.d("GetAllActivityViewModel", "Fetching all activities")

                val response = repository.GetAllActivityRepo()

                Log.d(
                    "GetAllActivityViewModel",
                    "Success: ${response.success}, Count: ${response.count}"
                )

                state = GetAllActivityState(
                    loading = false,
                    success = response.success,
                    message = if (response.success) "Activities fetched successfully" else "Failed to fetch activities",
                    response = response
                )

                onResult(
                    response.success,
                    state.message,
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "GetAllActivityViewModel",
                        "HTTP ${e.code()}: $errorBody"
                    )

                    errorBody ?: e.message()
                } else {
                    Log.e(
                        "GetAllActivityViewModel",
                        "Error fetching activities: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown error"
                }

                state = GetAllActivityState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown error"
                )

                onResult(
                    false,
                    errorMessage ?: "Unknown error",
                    null
                )
            }
        }
    }
}