package com.example.juttela.ViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.ProfileData
import com.example.juttela.DataSource.Models.UpdateProfileRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class UpdateProfileState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val profile: ProfileData? = null
)

class UpdateProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(UpdateProfileState())
        private set

    fun updateProfile(
        userId: String,
        name: String,
        profileImageUrl: String,
        age: Int,
        gender: String,
        about: String?,
        interests: List<String>,
        onResult: (Boolean, String, ProfileData?) -> Unit
    ) {
        val cleanedInterests = interests
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(3)

        if (userId.isBlank()) {
            onResult(false, "User id is required", null)
            return
        }

        if (name.isBlank()) {
            onResult(false, "Name is required", null)
            return
        }

        if (cleanedInterests.isEmpty()) {
            onResult(false, "Please select at least 1 interest", null)
            return
        }

        if (interests.size > 3) {
            onResult(false, "You can select only 3 interests", null)
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                Log.d(
                    "UpdateProfileViewModel",
                    "Updating profile: userId=$userId, name=$name, age=$age, gender=$gender, interests=$cleanedInterests"
                )

                val response = repository.ProfileUpdateRepo(
                    UpdateProfileRequest(
                        userId = userId,
                        name = name.trim(),
                        profileImageUrl = profileImageUrl,
                        age = age,
                        gender = gender,
                        about = about ?: "",
                        interests = cleanedInterests
                    )
                )

                Log.d(
                    "UpdateProfileViewModel",
                    "Success: ${response.success}, message: ${response.message}"
                )

                state = UpdateProfileState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    profile = response.data
                )

                onResult(response.success, response.message, response.data)

            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("UpdateProfileViewModel", "HTTP ${e.code()} updating profile. Body: $errorBody")
                    errorBody ?: e.message()
                } else {
                    Log.e("UpdateProfileViewModel", "Error updating profile: ${e.message}", e)
                    e.message ?: "Unknown Error"
                }

                state = UpdateProfileState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error"
                )

                onResult(false, errorMessage ?: "Unknown Error", null)
            }
        }
    }
}