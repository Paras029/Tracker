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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.parasgarg.tracker.R
import kotlinx.coroutines.launch

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
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

            if (!viewModel.isFirebaseAvailable) {
                ListItem(
                    headlineContent = { Text("Firebase not configured") },
                    supportingContent = {
                        Text("Place google-services.json in app/ directory and rebuild to enable cloud backup & sync.")
                    },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                )
            } else if (currentUser != null) {
                ListItem(
                    headlineContent = { Text(currentUser!!.displayName ?: currentUser!!.email ?: "Signed in") },
                    supportingContent = { Text("Cloud sync active · ${currentUser!!.email ?: ""}") },
                    leadingContent = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    trailingContent = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sync, contentDescription = "Synced", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text("Sign out") },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable(onClick = viewModel::signOut),
                )
            } else {
                ListItem(
                    headlineContent = { Text("Sign in with Google") },
                    supportingContent = { Text("Back up your data and sync across devices") },
                    leadingContent = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            runCatching {
                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(context.getString(R.string.default_web_client_id))
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                val result = credentialManager.getCredential(context, request)
                                val tokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                viewModel.signInWithGoogle(tokenCredential.idToken)
                            }
                        }
                    },
                )
            }

            HorizontalDivider()

            SectionLabel("ABOUT")

            ListItem(
                headlineContent = { Text("Vitals") },
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
