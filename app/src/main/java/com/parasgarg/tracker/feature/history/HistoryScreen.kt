package com.parasgarg.tracker.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.core.util.formatSessionTime
import com.parasgarg.tracker.core.util.workoutSummary
import com.parasgarg.tracker.core.util.workoutTypeLabel
import com.parasgarg.tracker.data.model.WorkoutType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    onLogWorkout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("History") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onLogWorkout) {
                Icon(Icons.Filled.Add, contentDescription = "Log workout")
            }
        },
    ) { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("All") },
                    )
                }
                items(WorkoutType.entries.toList()) { type ->
                    FilterChip(
                        selected = uiState.selectedFilter == type,
                        onClick = { viewModel.setFilter(type) },
                        label = { Text(workoutTypeLabel(type)) },
                    )
                }
            }

            if (uiState.sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workouts logged yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.sessions, key = { it.id }) { session ->
                        Card(onClick = { onSessionClick(session.id) }) {
                            ListItem(
                                headlineContent = { Text(workoutTypeLabel(session.type)) },
                                supportingContent = {
                                    Text("${formatSessionTime(session.startTime)} · ${workoutSummary(session)}")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
