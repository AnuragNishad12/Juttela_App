package com.example.juttela.Utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

object AppSnackbar {

    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
    }
}