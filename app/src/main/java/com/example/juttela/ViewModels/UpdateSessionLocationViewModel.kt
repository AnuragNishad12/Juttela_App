package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.UpdateSessionLocationRequest
import com.example.juttela.DataSource.Models.UpdateSessionLocationResponse
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class UpdateSessionLocationState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val location: UpdateSessionLocationResponse? = null
)

class UpdateSessionLocationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(UpdateSessionLocationState())
        private set

    fun updateLocation(
        sessionId: String,
        userId: String,
        lat: Double,
        lng: Double,
        onResult: (
            success: Boolean,
            message: String,
            response: UpdateSessionLocationResponse?
        ) -> Unit
    ) {

        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "UpdateSessionLocationVM",
                    "Updating location: sessionId=$sessionId, userId=$userId, lat=$lat, lng=$lng"
                )

                val response = repository.UpdateSessionLocationRepo(
                    sessionId = sessionId,
                    request = UpdateSessionLocationRequest(
                        userId = userId,
                        lat = lat,
                        lng = lng
                    )
                )

                Log.d(
                    "UpdateSessionLocationVM",
                    "Success: distance=${response.distanceMeters}, arrived=${response.arrived}, justArrived=${response.justArrived}"
                )

                val message = if (response.arrived) {
                    "You have arrived"
                } else {
                    "Location updated successfully"
                }

                state = UpdateSessionLocationState(
                    loading = false,
                    success = true,
                    message = message,
                    location = response
                )

                onResult(
                    true,
                    message,
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "UpdateSessionLocationVM",
                        "HTTP ${e.code()} updating location. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "UpdateSessionLocationVM",
                        "Error updating location: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = UpdateSessionLocationState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    location = null
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