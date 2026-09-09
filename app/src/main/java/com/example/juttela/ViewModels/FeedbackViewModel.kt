package com.example.juttela.ViewModels
import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.FeedbackData
import com.example.juttela.DataSource.Models.FeedbackDataOriginal
import com.example.juttela.DataSource.Models.FeedbackRequest
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class FeedbackState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val feedback: FeedbackDataOriginal? = null
)

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(FeedbackState())
        private set

    fun submitFeedback(
        fromUserId: String,
        toUserId: String,
        feedbackMessage: String,
        onResult: (
            success: Boolean,
            message: String,
            feedback: FeedbackDataOriginal?
        ) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(loading = true)

            try {

                Log.d(
                    "FeedbackViewModel",
                    "Submitting feedback: fromUserId=$fromUserId, toUserId=$toUserId"
                )

                val response = repository.FeedBackRepo(
                    FeedbackRequest(
                        fromUserId = fromUserId,
                        toUserId = toUserId,
                        message = feedbackMessage
                    )
                )

                Log.d(
                    "FeedbackViewModel",
                    "Feedback success: ${response.message}"
                )

                state = FeedbackState(
                    loading = false,
                    success = true,
                    message = response.message,
                    feedback = response.data
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
                        "FeedbackViewModel",
                        "HTTP ${e.code()} submitting feedback. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "FeedbackViewModel",
                        "Error submitting feedback: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = FeedbackState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    feedback = null
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