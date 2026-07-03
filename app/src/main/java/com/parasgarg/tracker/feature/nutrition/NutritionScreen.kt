package com.parasgarg.tracker.feature.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.data.model.domain.FoodOption
import com.parasgarg.tracker.data.model.domain.MacroTotals
import com.parasgarg.tracker.data.model.domain.MealType
import com.parasgarg.tracker.data.model.domain.NutritionEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrition") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddSheet() }) {
                Icon(Icons.Filled.Add, contentDescription = "Log food")
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            item {
                DateNavigationRow(
                    date = uiState.date,
                    onPrevious = viewModel::previousDay,
                    onNext = viewModel::nextDay,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item {
                MacroSummaryCard(
                    totals = uiState.totals,
                    targets = uiState.targets,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            MealType.entries.forEach { mealType ->
                val mealEntries = uiState.entries.filter { it.mealType == mealType }
                item(key = mealType.name + "_header") {
                    MealSectionHeader(
                        mealType = mealType,
                        entries = mealEntries,
                        onAddClick = { viewModel.openAddSheet(mealType) },
                    )
                }
                if (mealEntries.isNotEmpty()) {
                    items(mealEntries, key = { it.id }) { entry ->
                        FoodEntryRow(
                            entry = entry,
                            onDelete = { viewModel.deleteEntry(entry.id) },
                        )
                    }
                    item(key = mealType.name + "_divider") { HorizontalDivider() }
                }
            }
        }

        if (uiState.showAddSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::closeAddSheet,
                sheetState = sheetState,
            ) {
                AddFoodSheetContent(
                    state = uiState.addFood,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSelectFood = viewModel::selectFood,
                    onServingGramsChange = viewModel::updateServingGrams,
                    onMealTypeChange = viewModel::updateMealType,
                    onConfirm = viewModel::logFood,
                    onDismiss = viewModel::closeAddSheet,
                )
            }
        }
    }
}

@Composable
private fun DateNavigationRow(
    date: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
        }
        Text(label, style = MaterialTheme.typography.titleMedium)
        IconButton(
            onClick = onNext,
            enabled = date < today,
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
        }
    }
}

@Composable
private fun MacroSummaryCard(
    totals: MacroTotals,
    targets: MacroTotals,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroProgressRow("Calories", totals.caloriesKcal, targets.caloriesKcal, "kcal")
            MacroProgressRow("Protein", totals.proteinG, targets.proteinG, "g")
            MacroProgressRow("Carbs", totals.carbsG, targets.carbsG, "g")
            MacroProgressRow("Fat", totals.fatG, targets.fatG, "g")
        }
    }
}

@Composable
private fun MacroProgressRow(label: String, current: Double, target: Double, unit: String) {
    val progress = if (target > 0) (current / target).coerceIn(0.0, 1.0).toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(
                "${current.roundToInt()} / ${if (target > 0) target.roundToInt() else "—"} $unit",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MealSectionHeader(
    mealType: MealType,
    entries: List<NutritionEntry>,
    onAddClick: () -> Unit,
) {
    val totalKcal = entries.sumOf { it.caloriesKcal }.roundToInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(mealType.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (totalKcal > 0) {
                Text("$totalKcal kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onAddClick) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("Add")
        }
    }
}

@Composable
private fun FoodEntryRow(entry: NutritionEntry, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(entry.foodName) },
        supportingContent = {
            Text("${entry.servingGrams.roundToInt()}g · ${entry.proteinG.roundToInt()}p · ${entry.carbsG.roundToInt()}c · ${entry.fatG.roundToInt()}f")
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${entry.caloriesKcal.roundToInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
    )
}

@Composable
private fun AddFoodSheetContent(
    state: AddFoodState,
    onQueryChange: (String) -> Unit,
    onSelectFood: (FoodOption) -> Unit,
    onServingGramsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Add Food", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text("Search food") },
            placeholder = { Text("e.g. Chicken Breast, Oats…") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (state.results.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    state.results.take(6).forEach { food ->
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFood(food) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (state.selected != null) {
            val grams = state.servingGrams.toDoubleOrNull() ?: 0.0
            val factor = grams / 100.0
            val kcal = (state.selected.caloriesPer100g * factor).roundToInt()
            Text(
                "≈ $kcal kcal · ${(state.selected.proteinPer100g * factor).roundToInt()}g P · ${(state.selected.carbsPer100g * factor).roundToInt()}g C · ${(state.selected.fatPer100g * factor).roundToInt()}g F",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        OutlinedTextField(
            value = state.servingGrams,
            onValueChange = onServingGramsChange,
            label = { Text("Serving size (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Text("Meal", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { mealType ->
                FilterChip(
                    selected = state.mealType == mealType,
                    onClick = { onMealTypeChange(mealType) },
                    label = { Text(mealType.displayName) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(
                onClick = onConfirm,
                enabled = state.selected != null && state.servingGrams.toDoubleOrNull()?.let { it > 0 } == true,
            ) {
                Text("Log Food")
            }
        }
    }
}
