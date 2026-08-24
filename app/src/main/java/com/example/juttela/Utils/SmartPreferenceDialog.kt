package com.example.juttela.Utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juttela.DataSource.Models.AgePreference

@Composable
fun SmartPreferenceDialog(
    onDismiss: () -> Unit,
    onSubmit: (AgePreference, String) -> Unit
) {
    var minAge by remember { mutableStateOf("21") }
    var maxAge by remember { mutableStateOf("30") }
    var genderPreference by remember { mutableStateOf("any") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    text = "Juttela Pro matching",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Set who you want to meet",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Age preference",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAge,
                        onValueChange = {
                            minAge = it.filter { ch -> ch.isDigit() }
                            errorMessage = ""
                        },
                        label = { Text("Min", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7B00)
                        )
                    )

                    OutlinedTextField(
                        value = maxAge,
                        onValueChange = {
                            maxAge = it.filter { ch -> ch.isDigit() }
                            errorMessage = ""
                        },
                        label = { Text("Max", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7B00)
                        )
                    )
                }

                Text(
                    text = "Gender preference",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("any", "male", "female").forEach { option ->
                        FilterChip(
                            selected = genderPreference == option,
                            onClick = { genderPreference = option },
                            label = {
                                Text(
                                    text = option.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFE6CC),
                                selectedLabelColor = Color(0xFFFF7B00)
                            )
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        val min = minAge.toIntOrNull()
                        val max = maxAge.toIntOrNull()

                        when {
                            min == null || max == null -> {
                                errorMessage = "Please enter min and max age"
                            }
                            min < 18 -> {
                                errorMessage = "Min age should be 18 or above"
                            }
                            min > max -> {
                                errorMessage = "Min age cannot be greater than max age"
                            }
                            else -> {
                                onSubmit(
                                    AgePreference(min = min, max = max),
                                    genderPreference
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7B00),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = Color(0xFFFF7B00),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    )
}