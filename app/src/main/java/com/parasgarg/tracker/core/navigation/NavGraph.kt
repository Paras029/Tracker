package com.parasgarg.tracker.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.feature.bodymetrics.BodyMetricsScreen
import com.parasgarg.tracker.feature.dashboard.DashboardScreen
import com.parasgarg.tracker.feature.history.HistoryScreen
import com.parasgarg.tracker.feature.history.SessionDetailScreen
import com.parasgarg.tracker.feature.logworkout.LogWorkoutPickerScreen
import com.parasgarg.tracker.feature.logworkout.cardio.LogCardioScreen
import com.parasgarg.tracker.feature.logworkout.racquet.LogRacquetScreen
import com.parasgarg.tracker.feature.logworkout.strength.LogStrengthScreen
import com.parasgarg.tracker.feature.clinical.ClinicalScreen
import com.parasgarg.tracker.feature.nutrition.NutritionScreen
import com.parasgarg.tracker.feature.reminders.RemindersScreen
import com.parasgarg.tracker.feature.profile.ProfileScreen
import com.parasgarg.tracker.feature.reports.ReportsScreen
import com.parasgarg.tracker.feature.settings.SettingsScreen
import com.parasgarg.tracker.feature.wearables.WearablesScreen

private data class BottomNavItem(
    val destination: Destination,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Destination.Dashboard, "Dashboard", Icons.Filled.Home),
    BottomNavItem(Destination.History, "History", Icons.Filled.History),
    BottomNavItem(Destination.Reports, "Reports", Icons.Filled.BarChart),
    BottomNavItem(Destination.Nutrition, "Nutrition", Icons.Filled.Restaurant),
    BottomNavItem(Destination.Settings, "Settings", Icons.Filled.Settings),
)

@Composable
fun TrackerNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.destination.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Destination.LogWorkoutPicker.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Log workout")
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(Destination.Dashboard.route) {
                DashboardScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Destination.SessionDetail.createRoute(sessionId))
                    },
                    onBodyMetricsClick = { navController.navigate(Destination.BodyMetrics.route) },
                )
            }
            composable(Destination.History.route) {
                HistoryScreen(onSessionClick = { sessionId ->
                    navController.navigate(Destination.SessionDetail.createRoute(sessionId))
                })
            }
            composable(Destination.Reports.route) { ReportsScreen() }
            composable(Destination.Nutrition.route) { NutritionScreen() }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onProfileClick = { navController.navigate(Destination.Profile.route) },
                    onWearablesClick = { navController.navigate(Destination.Wearables.route) },
                    onClinicalClick = { navController.navigate(Destination.Clinical.route) },
                    onRemindersClick = { navController.navigate(Destination.Reminders.route) },
                )
            }
            composable(Destination.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Wearables.route) {
                WearablesScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Clinical.route) {
                ClinicalScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Reminders.route) {
                RemindersScreen(onBack = { navController.popBackStack() })
            }

            composable(Destination.LogWorkoutPicker.route) {
                LogWorkoutPickerScreen(
                    onTypeSelected = { type ->
                        val route = when (type) {
                            WorkoutType.STRENGTH -> Destination.LogStrength.route
                            WorkoutType.RUNNING, WorkoutType.SWIMMING ->
                                Destination.LogCardio.createRoute(type.name)
                            WorkoutType.BADMINTON, WorkoutType.TABLE_TENNIS ->
                                Destination.LogRacquet.createRoute(type.name)
                            WorkoutType.OTHER -> Destination.LogStrength.route
                        }
                        navController.navigate(route)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.LogStrength.route) {
                LogStrengthScreen(
                    onSaved = { navController.popBackStack(Destination.LogWorkoutPicker.route, inclusive = true) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Destination.LogCardio.route,
                arguments = listOf(navArgument(Destination.LogCardio.ARG_TYPE) { type = NavType.StringType }),
            ) {
                LogCardioScreen(
                    onSaved = { navController.popBackStack(Destination.LogWorkoutPicker.route, inclusive = true) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Destination.LogRacquet.route,
                arguments = listOf(navArgument(Destination.LogRacquet.ARG_TYPE) { type = NavType.StringType }),
            ) {
                LogRacquetScreen(
                    onSaved = { navController.popBackStack(Destination.LogWorkoutPicker.route, inclusive = true) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Destination.SessionDetail.route,
                arguments = listOf(navArgument(Destination.SessionDetail.ARG_SESSION_ID) { type = NavType.StringType }),
            ) {
                SessionDetailScreen(
                    onDeleted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.BodyMetrics.route) {
                BodyMetricsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
