package com.example.juttela.ViewModels



import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.GetProfileRequest
import com.example.juttela.DataSource.Models.ProfileDataNew
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GetProfileState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val profile: ProfileDataNew? = null
)

class GetProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(GetProfileState())
        private set

    fun getProfile(
        userId: String,
        onResult: (success: Boolean, message: String, profile: ProfileDataNew?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d(
                    "GetProfileViewModel",
                    "Getting profile: userId=$userId"
                )

                val response = repository.GetProfileRepo(
                    GetProfileRequest(
                        userId = userId
                    )
                )

                Log.d(
                    "GetProfileViewModel",
                    "Success: ${response.success}, message: ${response.message}"
                )

                state = GetProfileState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    profile = response.data
                )

                onResult(response.success, response.message, response.data)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("GetProfileViewModel", "HTTP ${e.code()} getting profile. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("GetProfileViewModel", "Error getting profile: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = GetProfileState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}