package com.example.juttela.ViewModels
import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.RatingDataOriginal
import com.example.juttela.DataSource.Models.RatingRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RatingState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val rating: RatingDataOriginal? = null
)

class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(RatingState())
        private set

    fun submitRating(
        fromUserId: String,
        toUserId: String,
        stars: Int,
        onResult: (success: Boolean, message: String, rating: RatingDataOriginal?) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "RatingViewModel",
                    "Submitting rating: fromUserId=$fromUserId, toUserId=$toUserId, stars=$stars"
                )

                val response = repository.RatingRepo(
                    RatingRequest(
                        fromUserId = fromUserId,
                        toUserId = toUserId,
                        stars = stars
                    )
                )

                Log.d(
                    "RatingViewModel",
                    "Rating success: ${response.message}"
                )

                state = RatingState(
                    loading = false,
                    success = true,
                    message = response.message,
                    rating = response.data
                )

                onResult(
                    true,
                    response.message,
                    response.data
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()?.errorBody()?.string()

                    Log.e(
                        "RatingViewModel",
                        "HTTP ${e.code()} submitting rating. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "RatingViewModel",
                        "Error submitting rating: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = RatingState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    rating = null
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