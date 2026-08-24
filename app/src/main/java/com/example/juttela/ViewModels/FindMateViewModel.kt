package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GeoAddRequestModel
import com.example.juttela.DataSource.Models.Match
import com.example.juttela.Repository.AuthRepository
import com.example.juttela.Utils.UserPrefs
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class FindMateState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val matches: List<Match> = emptyList(),
    val matchCount: Int = 0
)

class FindMateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(FindMateState())
        private set

    fun findMate(
        activity: String,
        longitude: Double,
        latitude: Double,
        onResult: (success: Boolean, message: String, matchCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                val context = getApplication<Application>().applicationContext
                val userId = UserPrefs.getUserId(context)
                val name = UserPrefs.getUserName(context)

                if (userId == null || name == null) {
                    state = state.copy(loading = false)
                    onResult(false, "User not found locally - please sign up again", 0)
                    return@launch
                }

                val response = repository.GeoAddUsersRepo(
                    GeoAddRequestModel(
                        userId = userId,
                        activity = activity,
                        longitude = longitude,
                        latitude = latitude,
                        name = name
                    )
                )

                state = FindMateState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    matches = response.data.matches,
                    matchCount = response.data.matchCount
                )

                onResult(response.success, response.message, response.data.matchCount)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    errorBody ?: e.message()
                } else {
                    e.message ?: "Unknown Error"
                }

                state = state.copy(loading = false)
                onResult(false, errorMessage ?: "Unknown Error", 0)
            }
        }
    }
}