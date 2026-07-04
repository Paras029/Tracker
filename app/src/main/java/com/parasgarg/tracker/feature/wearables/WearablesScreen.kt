package com.parasgarg.tracker.feature.wearables

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearablesScreen(
    onBack: () -> Unit,
    viewModel: WearablesViewModel = hiltViewModel(),
) {
    val connectedIds by viewModel.connectedIds.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val hcPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = viewModel::onHealthConnectPermissionsResult,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connected Devices") },
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
                .padding(contentPadding),
        ) {
            // Health Connect section
            item {
                SectionLabel("HEALTH CONNECT")
            }

            item {
                if (!uiState.healthConnectAvailable) {
                    ListItem(
                        headlineContent = { Text("Health Connect not available") },
                        supportingContent = {
                            Text(
                                "Install Health Connect from Play Store, or update your Android version. " +
                                    "Samsung Health and Google Fit sync through Health Connect.",
                            )
                        },
                        trailingContent = {
                            FilledTonalButton(
                                onClick = { openPlayStore(context, "com.google.android.apps.healthdata") },
                            ) { Text("Install") }
                        },
                    )
                } else if (!uiState.healthConnectHasPermissions) {
                    ListItem(
                        headlineContent = { Text("Grant Health Connect permissions") },
                        supportingContent = {
                            Text("Allows reading steps, sleep, heart rate, and exercise from Samsung Health, Google Fit, and other apps.")
                        },
                        trailingContent = {
                            Button(
                                onClick = { hcPermLauncher.launch(viewModel.requiredHcPermissions()) },
                            ) { Text("Grant") }
                        },
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("Health Connect") },
                        supportingContent = { Text("Permissions granted — reading from Samsung Health, Google Fit, and other connected apps") },
                        leadingContent = {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }

            // Samsung Health sync fix guidance (when HC is available)
            if (uiState.healthConnectAvailable) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "Samsung Watch data not syncing?",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Open Samsung Health → Profile → Connected Services → Health Connect → enable all permissions. " +
                                "Also check Samsung Health Settings → Connected Apps → Health Connect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { openApp(context, "com.sec.android.app.shealth") },
                            modifier = Modifier.padding(top = 4.dp),
                        ) { Text("Open Samsung Health") }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // Wearable connectors
            item { SectionLabel("WEARABLES") }

            items(viewModel.registry.all, key = { it.sourceId }) { connector ->
                val connected = connector.sourceId in connectedIds
                ListItem(
                    headlineContent = { Text(connector.displayName) },
                    supportingContent = {
                        Text(
                            if (connected) {
                                when (connector.sourceId) {
                                    "samsung_health", "google_fit" ->
                                        if (uiState.healthConnectAvailable && uiState.healthConnectHasPermissions)
                                            "Connected via Health Connect"
                                        else "Connected (enable Health Connect above for real data)"
                                    else -> "Connected (API integration coming soon)"
                                }
                            } else {
                                "Not connected"
                            },
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Filled.Watch, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = connected,
                            onCheckedChange = { viewModel.toggle(connector.sourceId, connected) },
                        )
                    },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // Strava section
            item { SectionLabel("CONNECTED APPS") }

            item {
                val stravaState = uiState.stravaState
                ListItem(
                    headlineContent = { Text("Strava") },
                    supportingContent = {
                        if (stravaState.isConnected) {
                            val lastSync = if (stravaState.lastSyncAt > 0L) {
                                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                                    .format(Date(stravaState.lastSyncAt * 1000))
                            } else "Never"
                            Text("Connected (Athlete #${stravaState.athleteId}) · Last sync: $lastSync")
                        } else {
                            Text("Tap Connect to link your Strava account and import activities")
                        }
                    },
                    leadingContent = {
                        Icon(Icons.Filled.DirectionsBike, contentDescription = null)
                    },
                    trailingContent = {
                        if (stravaState.isConnected) {
                            OutlinedButton(
                                onClick = viewModel::disconnectStrava,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) { Text("Disconnect") }
                        } else {
                            Button(
                                onClick = {
                                    // Profile must have Client ID set first
                                    viewModel.connectStrava(context)
                                },
                            ) { Text("Connect") }
                        }
                    },
                )
            }

            item {
                Text(
                    "Add Strava Client ID and Client Secret in Profile → Strava Integration before connecting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
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

private fun openPlayStore(context: Context, packageName: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }.onFailure {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun openApp(context: Context, packageName: String) {
    context.packageManager.getLaunchIntentForPackage(packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ?.let { context.startActivity(it) }
}
