package com.parasgarg.tracker.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.core.util.formatPace
import com.parasgarg.tracker.core.util.formatSessionTime
import com.parasgarg.tracker.core.util.workoutTypeLabel
import com.parasgarg.tracker.data.model.domain.WorkoutDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onDeleted: () -> Unit,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    val session = uiState.session

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.let { workoutTypeLabel(it.type) } ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete workout")
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        if (session != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(formatSessionTime(session.startTime))
                Text("Duration: ${session.durationMinutes} min")
                session.perceivedEffort?.let { Text("Perceived effort: $it/10") }

                HorizontalDivider()

                when (val detail = session.detail) {
                    is WorkoutDetail.Strength -> {
                        Text("Sets", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        detail.sets.forEach { set ->
                            Text("${set.exerciseName}: ${set.reps} reps @ ${set.weightKg} kg")
                        }
                    }
                    is WorkoutDetail.Cardio -> {
                        Text("Distance: %.2f km".format(detail.distanceMeters / 1000.0))
                        formatPace(detail.avgPaceSecondsPerKm)?.let { Text("Avg pace: $it") }
                        detail.poolLengthMeters?.let { Text("Pool length: $it m") }
                        detail.laps?.let { Text("Laps: $it") }
                    }
                    is WorkoutDetail.Racquet -> {
                        detail.opponentName?.let { Text("Opponent: $it") }
                        Text("Sets: ${detail.setsWon}-${detail.setsLost}")
                        detail.scoreSummary?.let { Text("Score: $it") }
                    }
                    WorkoutDetail.None -> Unit
                }

                session.notes?.let {
                    HorizontalDivider()
                    Text("Notes: $it")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding).padding(16.dp),
            ) {
                Text("Workout not found.")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete workout?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}
