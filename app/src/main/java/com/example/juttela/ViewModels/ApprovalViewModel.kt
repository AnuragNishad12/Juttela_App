package com.example.juttela.ViewModels

import com.example.juttela.Repository.AuthRepository

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.ApprovalModel

import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ApprovalState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val approval: ApprovalModel? = null
)

class ApprovalViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(ApprovalState())
        private set

    fun getUserApproval(
        userId: String,
        onResult: (success: Boolean, message: String, approval: ApprovalModel?) -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(
                loading = true,
                message = ""
            )

            try {
                Log.d(
                    "ApprovalViewModel",
                    "Fetching approval for userId=$userId"
                )

                val response = repository.getUserApproval(userId)

                state = ApprovalState(
                    loading = false,
                    success = true,
                    message = "Approval fetched successfully",
                    approval = response
                )

                Log.d(
                    "ApprovalViewModel",
                    "Approval status: ${response.approvalStatus}"
                )

                onResult(
                    true,
                    "Approval fetched successfully",
                    response
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {
                    val errorBody = e.response()
                        ?.errorBody()
                        ?.string()

                    Log.e(
                        "ApprovalViewModel",
                        "HTTP ${e.code()}: $errorBody"
                    )

                    errorBody ?: e.message()
                } else {
                    Log.e(
                        "ApprovalViewModel",
                        "Error fetching approval: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown error"
                }

                state = ApprovalState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown error",
                    approval = null
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
