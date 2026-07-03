package com.parasgarg.tracker.feature.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

private val tabs = listOf("Activity", "Body", "Nutrition")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Reports") }) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) },
                    )
                }
            }

            when (uiState.selectedTab) {
                0 -> ActivityTab(uiState)
                1 -> BodyTab(uiState)
                2 -> NutritionTab(uiState)
            }
        }
    }
}

@Composable
private fun ActivityTab(uiState: ReportsUiState) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val today = LocalDate.now()
    val dayLabels = (6 downTo 0).map { i ->
        today.minusDays(i.toLong())
            .dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    label = "Workouts this month",
                    value = uiState.totalWorkoutsThisMonth.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Minutes this month",
                    value = uiState.totalMinutesThisMonth.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            ChartCard(title = "Workouts — last 7 days") {
                BarChart(
                    values = uiState.last7DaysCounts,
                    labels = dayLabels,
                    barColor = primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            }
        }

        if (uiState.sessionsByType.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Sessions by type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(12.dp))
                        val total = uiState.sessionsByType.sumOf { it.second }.toFloat()
                        uiState.sessionsByType.forEach { (label, count) ->
                            SessionTypeRow(
                                label = label,
                                count = count,
                                fraction = if (total > 0f) count / total else 0f,
                                color = secondary,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun BodyTab(uiState: ReportsUiState) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        if (uiState.weightHistory.isNotEmpty()) {
            item {
                ChartCard(title = "Weight trend (kg)") {
                    LineChart(
                        points = uiState.weightHistory.map { (_, kg) -> kg },
                        labels = uiState.weightHistory.map { (date, _) ->
                            "${date.dayOfMonth}/${date.monthValue}"
                        },
                        lineColor = primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    )
                }
            }
        } else {
            item { EmptyState("Log body metrics to see your weight trend.") }
        }

        if (uiState.bodyFatHistory.isNotEmpty()) {
            item {
                ChartCard(title = "Body fat % trend") {
                    LineChart(
                        points = uiState.bodyFatHistory.map { (_, pct) -> pct },
                        labels = uiState.bodyFatHistory.map { (date, _) ->
                            "${date.dayOfMonth}/${date.monthValue}"
                        },
                        lineColor = tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    )
                }
            }
        } else {
            item { EmptyState("Add BCA scans in Clinical to see body fat trend.") }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun NutritionTab(uiState: ReportsUiState) {
    val primary = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val dayLabels = (6 downTo 0).map { i ->
        today.minusDays(i.toLong())
            .dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    label = "Avg calories (7d)",
                    value = "%.0f kcal".format(uiState.avgCaloriesLast7),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Avg protein (7d)",
                    value = "%.0f g".format(uiState.avgProteinLast7),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            ChartCard(title = "Calories — last 7 days") {
                BarChart(
                    values = uiState.last7DaysCalories,
                    labels = dayLabels,
                    barColor = primary,
                    targetLine = if (uiState.caloriesTarget > 0f) uiState.caloriesTarget else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            }
        }

        if (uiState.caloriesTarget > 0f) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Daily calorie target", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "%.0f kcal".format(uiState.caloriesTarget),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Chart Composables ──────────────────────────────────────────────────────────

@Composable
private fun BarChart(
    values: List<Float>,
    labels: List<String>,
    barColor: Color,
    modifier: Modifier = Modifier,
    targetLine: Float? = null,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val targetColor = MaterialTheme.colorScheme.error

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (values.isEmpty()) return@Canvas
            val maxVal = max(values.max(), targetLine ?: 0f).takeIf { it > 0f } ?: 1f
            val barCount = values.size
            val totalPad = size.width * 0.1f
            val slotWidth = (size.width - totalPad) / barCount
            val barWidth = slotWidth * 0.6f
            val chartH = size.height

            drawLine(onSurface, Offset(0f, chartH), Offset(size.width, chartH), strokeWidth = 1.dp.toPx())

            values.forEachIndexed { i, v ->
                val barH = (v / maxVal) * chartH
                val x = totalPad / 2 + i * slotWidth + (slotWidth - barWidth) / 2
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, chartH - barH),
                    size = Size(barWidth, barH.coerceAtLeast(2f)),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }

            if (targetLine != null && targetLine > 0f) {
                val y = chartH - (targetLine / maxVal) * chartH
                drawLine(
                    color = targetColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 8f), 0f,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    points: List<Float>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    val dotColor = lineColor
    val onSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (points.size < 2) return@Canvas
            val minVal = points.min()
            val maxVal = points.max()
            val range = (maxVal - minVal).takeIf { it > 0f } ?: 1f
            val chartH = size.height
            val chartW = size.width
            val step = chartW / (points.size - 1)

            drawLine(onSurface, Offset(0f, chartH), Offset(chartW, chartH), strokeWidth = 1.dp.toPx())

            val path = Path()
            points.forEachIndexed { i, v ->
                val x = i * step
                val y = chartH - ((v - minVal) / range) * chartH * 0.9f - chartH * 0.05f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx()))

            points.forEachIndexed { i, v ->
                val x = i * step
                val y = chartH - ((v - minVal) / range) * chartH * 0.9f - chartH * 0.05f
                drawCircle(dotColor, radius = 4.dp.toPx(), center = Offset(x, y))
            }
        }

        val stride = max(1, labels.size / 5)
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                val visible = i % stride == 0 || i == labels.size - 1
                Text(
                    if (visible) label else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Small shared composables ───────────────────────────────────────────────────

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun SessionTypeRow(label: String, count: Int, fraction: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(count.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(3.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp)),
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}
