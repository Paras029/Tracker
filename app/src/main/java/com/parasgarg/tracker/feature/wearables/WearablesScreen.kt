package com.parasgarg.tracker.feature.wearables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearablesScreen(
    onBack: () -> Unit,
    viewModel: WearablesViewModel = hiltViewModel(),
) {
    val connectedIds by viewModel.connectedIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connected Wearables") },
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
            item {
                Text(
                    "Simulated connectors — real device integration via Health Connect coming later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(viewModel.registry.all, key = { it.sourceId }) { connector ->
                val connected = connector.sourceId in connectedIds
                ListItem(
                    headlineContent = { Text(connector.displayName) },
                    supportingContent = {
                        Text(if (connected) "Connected (simulated)" else "Not connected")
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
        }
    }
}
