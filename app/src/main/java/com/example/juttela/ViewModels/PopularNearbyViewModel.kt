package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.NearbyActivitiesRequest
import com.example.juttela.DataSource.Models.NearbyActivity
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PopularNearbyState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val searchRadiusKm: Int = 5,
    val items: List<NearbyActivity> = emptyList()
)

class PopularNearbyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(PopularNearbyState())
        private set

    fun loadPopularNearby(
        userId: String,
        longitude: Double,
        latitude: Double,
        radiusKm: Int = 3,
        limit: Int = 4,
        onResult: (
            success: Boolean,
            message: String,
            items: List<NearbyActivity>
        ) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, message = "")

            try {
                Log.d(
                    "PopularNearbyViewModel",
                    "Fetching popular nearby: userId=$userId, lat=$latitude, lng=$longitude, radius=$radiusKm"
                )

                val response = repository.PopularActivityNearBy(
                    NearbyActivitiesRequest(
                        userId = userId,
                        longitude = longitude,
                        latitude = latitude,
                        radiusKm = radiusKm,
                        limit = limit
                    )
                )

                Log.d("PopularNearbyViewModel", "FULL RESPONSE = $response")
                Log.d("PopularNearbyViewModel", "success=${response.success}")
                Log.d("PopularNearbyViewModel", "searchRadiusKm=${response.data.searchRadiusKm}")
                Log.d("PopularNearbyViewModel", "itemCount=${response.data.items.size}")

                response.data.items.forEachIndexed { index, item ->
                    Log.d(
                        "PopularNearbyViewModel",
                        "item[$index] activity=${item.activity}, label=${item.label}, peopleNearby=${item.peopleNearby}, source=${item.source}"
                    )
                }

                val items = response.data.items
                val message = if (response.success) {
                    "Popular activities loaded"
                } else {
                    "Failed to load popular activities"
                }

                state = PopularNearbyState(
                    loading = false,
                    success = response.success,
                    message = message,
                    searchRadiusKm = response.data.searchRadiusKm,
                    items = items
                )

                onResult(response.success, message, items)
            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "PopularNearbyViewModel",
                        "HTTP ${e.code()} loading popular nearby. Body: $errorBody"
                    )
                    errorBody ?: e.message()
                } else {
                    Log.e(
                        "PopularNearbyViewModel",
                        "Error loading popular nearby: ${e.message}",
                        e
                    )
                    e.message ?: "Unknown Error"
                }

                state = PopularNearbyState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    items = emptyList()
                )

                onResult(false, errorMessage ?: "Unknown Error", emptyList())
            }
        }
    }
}