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

val activityList = listOf(
    "running", "walking", "cycling", "roller_skating", "skateboarding",
    "yoga", "gym", "hiking", "rock_climbing", "swimming",
    "football", "badminton", "basketball", "volleyball", "tennis",
    "table_tennis", "cricket", "frisbee", "bowling", "billiards",
    "coffee", "lunch", "dinner", "breakfast", "ice_cream",
    "drinks", "sushi", "street_food", "dessert", "shopping",
    "movies", "watch_series", "gaming", "board_games", "chess",
    "karaoke", "theatre", "painting", "photography", "fishing",
    "study", "coding", "book_club", "puzzle_solving", "coworking",
    "conversation", "casual_chat", "make_friends", "meet_new_people", "dating",
    "dog_walking", "sunrise_sunset", "local_events"
)

private fun String.toDisplayLabel(): String =
    if (this.isBlank()) "" else this.replace("_", " ").replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDropdown(
    selectedActivity: String,
    onActivitySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // The anchor field IS the search field now — this is what fixes typing,
    // since ExposedDropdownMenu's popup is non-focusable and can't host a
    // separate editable field inside it.
    var query by remember { mutableStateOf(selectedActivity.toDisplayLabel()) }

    // Keep the field in sync if the selection changes from outside
    // (e.g. tapping a "Popular near you" chip).
    LaunchedEffect(selectedActivity) {
        query = selectedActivity.toDisplayLabel()
    }

    val filteredActivities = remember(query) {
        if (query.isBlank()) activityList
        else activityList.filter { it.contains(query.trim(), ignoreCase = true) }
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
                                text = activity.toDisplayLabel(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1A1A1A)
                            )
                        },
                        onClick = {
                            query = activity.toDisplayLabel()
                            onActivitySelected(activity)
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