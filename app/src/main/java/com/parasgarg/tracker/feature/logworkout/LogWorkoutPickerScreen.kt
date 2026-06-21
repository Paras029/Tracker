package com.parasgarg.tracker.feature.logworkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parasgarg.tracker.data.model.WorkoutType

private data class WorkoutTypeOption(
    val type: WorkoutType,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val workoutTypeOptions = listOf(
    WorkoutTypeOption(WorkoutType.STRENGTH, "Strength Training", Icons.Filled.FitnessCenter),
    WorkoutTypeOption(WorkoutType.RUNNING, "Running", Icons.AutoMirrored.Filled.DirectionsRun),
    WorkoutTypeOption(WorkoutType.SWIMMING, "Swimming", Icons.Filled.Pool),
    WorkoutTypeOption(WorkoutType.BADMINTON, "Badminton", Icons.Filled.SportsTennis),
    WorkoutTypeOption(WorkoutType.TABLE_TENNIS, "Table Tennis", Icons.Filled.SportsTennis),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutPickerScreen(
    onTypeSelected: (WorkoutType) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            workoutTypeOptions.forEach { option ->
                Card(onClick = { onTypeSelected(option.type) }) {
                    ListItem(
                        headlineContent = { Text(option.label) },
                        leadingContent = { Icon(option.icon, contentDescription = null) },
                    )
                }
            }
        }
    }
}
