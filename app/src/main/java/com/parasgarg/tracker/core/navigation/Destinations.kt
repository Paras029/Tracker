package com.parasgarg.tracker.core.navigation

sealed class Destination(val route: String) {
    data object Dashboard : Destination("dashboard")
    data object History : Destination("history")
    data object Reports : Destination("reports")
    data object Settings : Destination("settings")
}
