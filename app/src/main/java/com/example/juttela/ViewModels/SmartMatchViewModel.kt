package com.example.juttela.ViewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.AgePreference
import com.example.juttela.DataSource.Models.SmartMatch
import com.example.juttela.DataSource.Models.SmartMatchRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch

data class SmartMatchUiState(
    val loading: Boolean = false,
    val matches: List<SmartMatch> = emptyList(),
    val matchCount: Int = 0,
    val activity: String = "",
    val searchRadiusKm: Double = 0.0,
    val error: String? = null
)

class SmartMatchViewModel : ViewModel() {

    private val repository = AuthRepository()

    var state by mutableStateOf(SmartMatchUiState())
        private set

    fun smartMatch(
        name: String,
        userId: String,
        activity: String,
        longitude: Double,
        latitude: Double,
        radiusKm: Double = 5.0,
        agePreference: AgePreference,
        genderPreference: String = "any",
        onResult: (success: Boolean, message: String, matches: List<SmartMatch>) -> Unit
    ) {
        state = state.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val request = SmartMatchRequest(
                    userId = userId,
                    name = name,
                    activity = activity,
                    longitude = longitude,
                    latitude = latitude,
                    radiusKm = radiusKm,
                    agePreference = agePreference,
                    genderPreference = genderPreference
                )

                Log.d("SmartMatchViewModel", "Request: $request")

                val response = repository.SmartMatchRepo(request)

                Log.d(
                    "SmartMatchViewModel",
                    "Response success=${response.success}, message=${response.message}, data=${response.data}"
                )

                if (response.success) {
                    val matches = response.data?.matches ?: emptyList()

                    state = state.copy(
                        loading = false,
                        matches = matches,
                        matchCount = response.data?.matchCount ?: matches.size,
                        activity = response.data?.activity ?: activity,
                        searchRadiusKm = response.data?.searchRadiusKm ?: radiusKm,
                        error = null
                    )

                    Log.d("SmartMatchViewModel", "Matches count=${matches.size}")
                    onResult(true, response.message, matches)
                } else {
                    state = state.copy(
                        loading = false,
                        matches = emptyList(),
                        matchCount = 0,
                        error = response.message
                    )

                    Log.e("SmartMatchViewModel", "API failed: ${response.message}")
                    onResult(false, response.message, emptyList())
                }
            } catch (e: Exception) {
                val message = e.message ?: "Something went wrong"

                Log.e("SmartMatchViewModel", "Error: $message", e)

                state = state.copy(
                    loading = false,
                    matches = emptyList(),
                    matchCount = 0,
                    error = message
                )

                onResult(false, message, emptyList())
            }
        }
    }

    fun clearMatches() {
        state = SmartMatchUiState()
    }
}