package com.parasgarg.tracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onWearablesClick: () -> Unit = {},
    onClinicalClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Text(
                text = "PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ListItem(
                headlineContent = { Text("Profile & Goals") },
                supportingContent = { Text("Name, height, fitness goal, nutrition targets") },
                leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProfileClick),
            )

            HorizontalDivider()

            Text(
                text = "INTEGRATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ListItem(
                headlineContent = { Text("Connected Wearables") },
                supportingContent = { Text("Samsung Health, Oura, Garmin, Fitbit, Whoop") },
                leadingContent = { Icon(Icons.Filled.Watch, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onWearablesClick),
            )

            ListItem(
                headlineContent = { Text("Clinical & BCA") },
                supportingContent = { Text("Blood tests, body composition scans") },
                leadingContent = { Icon(Icons.Filled.Biotech, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClinicalClick),
            )

            HorizontalDivider()

            Text(
                text = "NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ListItem(
                headlineContent = { Text("Reminders") },
                supportingContent = { Text("Daily workout reminder — coming soon") },
                leadingContent = { Icon(Icons.Filled.NotificationsNone, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
            )

            HorizontalDivider()
        }
    }
}
