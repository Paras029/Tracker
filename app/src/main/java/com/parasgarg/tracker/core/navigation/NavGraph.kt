package com.parasgarg.tracker.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.parasgarg.tracker.feature.dashboard.DashboardScreen
import com.parasgarg.tracker.feature.history.HistoryScreen
import com.parasgarg.tracker.feature.reports.ReportsScreen
import com.parasgarg.tracker.feature.settings.SettingsScreen

private data class BottomNavItem(
    val destination: Destination,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Destination.Dashboard, "Dashboard", Icons.Filled.Home),
    BottomNavItem(Destination.History, "History", Icons.Filled.History),
    BottomNavItem(Destination.Reports, "Reports", Icons.Filled.BarChart),
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
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(Destination.Dashboard.route) { DashboardScreen() }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Reports.route) { ReportsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
