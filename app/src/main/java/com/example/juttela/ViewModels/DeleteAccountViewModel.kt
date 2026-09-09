package com.example.juttela.ViewModels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.DataSource.Models.DeletedUser
import com.example.juttela.Repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class DeleteAccountState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = "",
    val deletedUser: DeletedUser? = null
)

class DeleteAccountViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    var state by mutableStateOf(DeleteAccountState())
        private set

    fun deleteAccount(
        userId: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {

            state = state.copy(loading = true)

            try {
                Log.d(
                    "DeleteAccountViewModel",
                    "Deleting account: userId=$userId"
                )

                val response = repository.deleteAccount(userId)

                Log.d(
                    "DeleteAccountViewModel",
                    "Delete response: success=${response.success}, message=${response.message}"
                )

                state = DeleteAccountState(
                    loading = false,
                    success = response.success,
                    message = response.message,
                    deletedUser = response.deletedUser
                )

                onResult(
                    response.success,
                    response.message
                )

            } catch (e: Exception) {

                val errorMessage = if (e is HttpException) {

                    val errorBody = e.response()?.errorBody()?.string()

                    Log.e(
                        "DeleteAccountViewModel",
                        "HTTP ${e.code()} deleting account. Body: $errorBody"
                    )

                    errorBody ?: e.message()

                } else {

                    Log.e(
                        "DeleteAccountViewModel",
                        "Error deleting account: ${e.message}",
                        e
                    )

                    e.message ?: "Unknown Error"
                }

                state = DeleteAccountState(
                    loading = false,
                    success = false,
                    message = errorMessage ?: "Unknown Error",
                    deletedUser = null
                )

                onResult(
                    false,
                    errorMessage ?: "Unknown Error"
                )
            }
        }
    }
}