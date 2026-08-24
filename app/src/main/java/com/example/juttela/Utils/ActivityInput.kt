package com.example.juttela.Utils
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityInput(
    text: String,
    onTextChange: (String) -> Unit
) {
    val suggestions = listOf("Walk","Conversation", "Sitting")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.LightGray,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("e.g. play football, do yoga, go cycling...")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { item ->
                SuggestionChip(
                    onClick = {
                        onTextChange(item)
                    },
                    label = {
                        Text(item)
                    }
                )
            }
        }
    }
}