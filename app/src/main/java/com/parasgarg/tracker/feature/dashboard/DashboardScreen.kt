package com.parasgarg.tracker.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.core.util.formatSessionTime
import com.parasgarg.tracker.core.util.workoutSummary
import com.parasgarg.tracker.core.util.workoutTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSessionClick: (String) -> Unit = {},
    onBodyMetricsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("This week", style = MaterialTheme.typography.titleMedium)
                    Text("${uiState.weeklySessionCount} workouts · ${uiState.weeklyDurationMinutes} min")
                }
            }

            Card(onClick = onBodyMetricsClick, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Latest weight", style = MaterialTheme.typography.titleMedium)
                    val latest = uiState.latestBodyMetric
                    Text(
                        latest?.weightKg?.let { "%.1f kg".format(it) }
                            ?: "No body metrics logged yet. Tap to add one.",
                    )
                }
            }

            Text("Recent workouts", style = MaterialTheme.typography.titleMedium)

            if (uiState.recentSessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text("No workouts logged yet. Tap + to log one.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.recentSessions, key = { it.id }) { session ->
                        Card(onClick = { onSessionClick(session.id) }, modifier = Modifier.fillMaxWidth()) {
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
