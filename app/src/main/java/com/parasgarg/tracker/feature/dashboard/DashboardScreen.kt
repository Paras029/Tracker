package com.parasgarg.tracker.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.core.util.formatSessionTime
import com.parasgarg.tracker.core.util.formatWeight
import com.parasgarg.tracker.core.util.workoutSummary
import com.parasgarg.tracker.core.util.workoutTypeLabel
import com.parasgarg.tracker.data.model.domain.WellnessMetric
import kotlin.math.roundToInt

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("This week", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${uiState.weeklySessionCount} workouts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text("${uiState.weeklyDurationMinutes} min", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (uiState.healthScore != null) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Health score", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${uiState.healthScore}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text("/100", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (uiState.todayWellness != null) {
                item {
                    WearableTelemetryCard(uiState.todayWellness!!)
                }
            }

            item {
                Card(onClick = onBodyMetricsClick, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Latest weight", style = MaterialTheme.typography.labelMedium)
                        val latest = uiState.latestBodyMetric
                        Text(
                            latest?.weightKg?.let { formatWeight(it, uiState.useMetric) }
                                ?: "No body metrics logged yet. Tap to add.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Text("Recent workouts", style = MaterialTheme.typography.titleMedium)
            }

            if (uiState.recentSessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("No workouts logged yet. Tap + to log one.")
                    }
                }
            } else {
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

            item { Box(modifier = Modifier.padding(bottom = 8.dp)) }
        }
    }
}

@Composable
private fun WearableTelemetryCard(wellness: WellnessMetric) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Today's activity", style = MaterialTheme.typography.titleSmall)
                Text(
                    wellness.sourceName.replace('_', ' ').replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                wellness.stepsCount?.let { TelemetryStat("Steps", "$it") }
                wellness.activeCaloriesKcal?.let { TelemetryStat("Calories", "${it.roundToInt()} kcal") }
                wellness.sleepHours?.let { TelemetryStat("Sleep", "%.1fh".format(it)) }
                wellness.restingHeartRate?.let { TelemetryStat("Resting HR", "$it bpm") }
            }
        }
    }
}

@Composable
private fun TelemetryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
