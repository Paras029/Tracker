package com.parasgarg.tracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onWearablesClick: () -> Unit = {},
    onClinicalClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val useMetric by viewModel.useMetric.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("PROFILE")

            ListItem(
                headlineContent = { Text("Profile & Goals") },
                supportingContent = { Text("Name, height, fitness goal, nutrition targets") },
                leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onProfileClick),
            )

            HorizontalDivider()

            SectionLabel("UNITS")

            ListItem(
                headlineContent = { Text("Use metric units") },
                supportingContent = { Text(if (useMetric) "kg · km" else "lbs · mi") },
                leadingContent = { Icon(Icons.Filled.Scale, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = useMetric,
                        onCheckedChange = viewModel::setUseMetric,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            SectionLabel("INTEGRATIONS")

            ListItem(
                headlineContent = { Text("Connected Wearables") },
                supportingContent = { Text("Samsung Health, Oura, Garmin, Fitbit, Whoop") },
                leadingContent = { Icon(Icons.Filled.Watch, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onWearablesClick),
            )

            ListItem(
                headlineContent = { Text("Clinical & BCA") },
                supportingContent = { Text("Blood tests, body composition scans") },
                leadingContent = { Icon(Icons.Filled.Biotech, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClinicalClick),
            )

            HorizontalDivider()

            SectionLabel("NOTIFICATIONS")

            ListItem(
                headlineContent = { Text("Reminders") },
                supportingContent = { Text("Daily workout reminder time & days") },
                leadingContent = { Icon(Icons.Filled.NotificationsNone, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onRemindersClick),
            )

            HorizontalDivider()

            SectionLabel("SYNC & ACCOUNT")

            ListItem(
                headlineContent = { Text("Cloud sync") },
                supportingContent = { Text("Data stored locally. Firebase sync available after adding google-services.json.") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            )

            HorizontalDivider()

            SectionLabel("ABOUT")

            ListItem(
                headlineContent = { Text("Tracker") },
                supportingContent = { Text("Version 1.0 · Personal health & fitness tracker") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
