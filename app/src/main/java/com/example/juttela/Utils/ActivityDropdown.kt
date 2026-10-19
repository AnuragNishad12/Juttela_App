package com.example.juttela.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juttela.DataSource.Models.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDropdown(
    activities: List<Activity>,               // Pass list from ViewModel here
    selectedActivity: String,                 // selected key or label
    onActivitySelected: (String) -> Unit      // returns the selected key
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(selectedActivity) }

    // Sync when selection changes from outside
    LaunchedEffect(selectedActivity) {
        query = activities.find { it.key == selectedActivity }?.label
            ?: activities.find { it.label == selectedActivity }?.label
                    ?: selectedActivity
    }

    val filteredActivities = remember(query, activities) {
        if (query.isBlank()) activities
        else activities.filter {
            it.label.contains(query.trim(), ignoreCase = true) ||
                    it.key.contains(query.trim(), ignoreCase = true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (!expanded) expanded = true
            },
            placeholder = {
                Text("Select an activity", fontSize = 14.sp)
            },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD9D9D9),
                focusedBorderColor = Color(0xFFFF7B00)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            if (filteredActivities.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No matches found",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    onClick = {}
                )
            } else {
                filteredActivities.forEachIndexed { index, activity ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = activity.label,          // Display only activity name
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1A1A1A)
                            )
                        },
                        onClick = {
                            query = activity.label
                            onActivitySelected(activity.key)   // Send key back
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    )
                    if (index != filteredActivities.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}