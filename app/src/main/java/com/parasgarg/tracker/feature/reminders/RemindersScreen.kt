package com.parasgarg.tracker.feature.reminders

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user decided; they can re-enable in Settings */ }

    LaunchedEffect(Unit) {
        viewModel.refreshExactAlarmPermission()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Daily workout reminder", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (uiState.isEnabled) "Enabled" else "Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = uiState.isEnabled,
                                onCheckedChange = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    viewModel.toggleEnabled(it)
                                },
                            )
                        }

                        HorizontalDivider()

                        TimeRow(
                            hour = uiState.hour,
                            minute = uiState.minute,
                            onTimeSet = viewModel::setTime,
                        )

                        HorizontalDivider()

                        Text("Days", style = MaterialTheme.typography.labelMedium)
                        DayChips(
                            selected = uiState.daysOfWeek,
                            onToggle = viewModel::toggleDay,
                        )
                    }
                }
            }

            if (!uiState.canScheduleExact && uiState.isEnabled) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Exact alarm permission required",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                "On Android 12+, grant exact alarm permission so the reminder fires at the exact time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                }
                            }) {
                                Text("Open settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRow(hour: Int, minute: Int, onTimeSet: (Int, Int) -> Unit) {
    var hourInput by remember(hour) { mutableStateOf(hour.toString().padStart(2, '0')) }
    var minuteInput by remember(minute) { mutableStateOf(minute.toString().padStart(2, '0')) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Time", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = hourInput,
            onValueChange = { v ->
                hourInput = v
                val h = v.toIntOrNull()?.coerceIn(0, 23) ?: return@OutlinedTextField
                onTimeSet(h, minuteInput.toIntOrNull()?.coerceIn(0, 59) ?: minute)
            },
            label = { Text("HH") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        Text(":", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = minuteInput,
            onValueChange = { v ->
                minuteInput = v
                val m = v.toIntOrNull()?.coerceIn(0, 59) ?: return@OutlinedTextField
                onTimeSet(hourInput.toIntOrNull()?.coerceIn(0, 23) ?: hour, m)
            },
            label = { Text("MM") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
    }
}

@Composable
private fun DayChips(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val days = listOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
        DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.forEach { (day, label) ->
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = { Text(label) },
            )
        }
    }
}
