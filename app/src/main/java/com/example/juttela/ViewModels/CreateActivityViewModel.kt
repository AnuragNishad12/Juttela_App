package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.ActivityRequest
import com.example.juttela.DataSource.Models.ActivityResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CreateActivityState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val activity: ActivityResponse? = null
)

class CreateActivityViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(CreateActivityState())
        private set

    fun createActivity(
        request: ActivityRequest,
        onResult: (success: Boolean, message: String, activity: ActivityResponse?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, message = "")

            try {
                Log.d(
                    "CreateActivityViewModel",
                    "Creating activity: ${request.key}"
                )

                val response = repository.createActivityRepo(request)

                Log.d(
                    "CreateActivityViewModel",
                    "Success: ${response.success}, Message: ${response.message}"
                )

                state = CreateActivityState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    activity = response
                )

                onResult(
                    response.success,
                    response.message,
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "CreateActivityViewModel",
                        "HTTP ${e.code()}: $errorBody"
                    )

                    errorBody ?: e.message()
                } else {
                    Log.e(
                        "CreateActivityViewModel",
                        "Error creating activity: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown error"
                }

                state = CreateActivityState(
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