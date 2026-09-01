package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.ArrivalsResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ArrivalsState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val arrivals: ArrivalsResponse? = null
)

class ArrivalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(ArrivalsState())
        private set

    fun getArrivals(
        userId: String,
        onResult: (success: Boolean, message: String, response: ArrivalsResponse?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)

            try {
                Log.d(
                    "ArrivalsViewModel",
                    "Getting arrivals for userId=$userId"
                )

                val response = repository.getArrivalsRepo(userId)

                Log.d(
                    "ArrivalsViewModel",
                    "Success: count=${response.count}"
                )

                state = ArrivalsState(
                    loading = false,
                    success = true,
                    message = "Arrivals fetched successfully",
                    arrivals = response
                )

                onResult(
                    true,
                    "Arrivals fetched successfully",
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()

                    Log.e(
                        "ArrivalsViewModel",
                        "HTTP ${e.code()} getting arrivals. Body: $errorBody"
                    )

                    errorBody ?: e.message()
                } else {
                    Log.e(
                        "ArrivalsViewModel",
                        "Error getting arrivals: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = ArrivalsState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    arrivals = null
                )

                onResult(
                    false,
                    errorMessage ?: "Unknown Error",
                    null
                )
            }
        }
    }
}