package com.parasgarg.tracker.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.form

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            viewModel.consumeSaved()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Goals") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionHeader("Personal Info")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.heightCm,
                            onValueChange = viewModel::updateHeight,
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = form.weightKg,
                            onValueChange = viewModel::updateWeight,
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = form.activityLevel,
                        onValueChange = viewModel::updateActivityLevel,
                        label = { Text("Activity level (e.g. Moderate, High)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.fitnessGoal,
                        onValueChange = viewModel::updateFitnessGoal,
                        label = { Text("Fitness goal (e.g. Lean Muscle Growth)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                HorizontalDivider()
                SectionHeader("Daily Nutrition Targets")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.targetCaloriesKcal,
                        onValueChange = viewModel::updateTargetCalories,
                        label = { Text("Target calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.targetProteinG,
                            onValueChange = viewModel::updateTargetProtein,
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = form.targetCarbsG,
                            onValueChange = viewModel::updateTargetCarbs,
                            label = { Text("Carbs (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = form.targetFatG,
                            onValueChange = viewModel::updateTargetFat,
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
                SectionHeader("AI Coaching (Gemini)")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.geminiApiKey,
                        onValueChange = viewModel::updateGeminiApiKey,
                        label = { Text("Gemini API key (optional)") },
                        placeholder = { Text("Leave blank to use offline coaching only") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Force offline coaching", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Use built-in tips instead of Gemini",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = form.isGeminiOfflineMode,
                            onCheckedChange = viewModel::toggleGeminiOfflineMode,
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
                SectionHeader("Strava Integration")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Get your Client ID and Client Secret from strava.com/settings/api",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = form.stravaClientId,
                        onValueChange = viewModel::updateStravaClientId,
                        label = { Text("Strava Client ID") },
                        placeholder = { Text("Numeric ID from developer portal") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.stravaClientSecret,
                        onValueChange = viewModel::updateStravaClientSecret,
                        label = { Text("Strava Client Secret") },
                        placeholder = { Text("Secret string from developer portal") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Text("Save Profile")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}
