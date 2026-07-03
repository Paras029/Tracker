package com.parasgarg.tracker.core.navigation

sealed class Destination(val route: String) {
    data object Dashboard : Destination("dashboard")
    data object History : Destination("history")
    data object Reports : Destination("reports")
    data object Settings : Destination("settings")

    data object LogWorkoutPicker : Destination("log_workout")
    data object LogStrength : Destination("log_workout/strength")

    data object LogCardio : Destination("log_workout/cardio/{type}") {
        const val ARG_TYPE = "type"
        fun createRoute(type: String) = "log_workout/cardio/$type"
    }

    data object LogRacquet : Destination("log_workout/racquet/{type}") {
        const val ARG_TYPE = "type"
        fun createRoute(type: String) = "log_workout/racquet/$type"
    }

    data object SessionDetail : Destination("history/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"
        fun createRoute(sessionId: String) = "history/$sessionId"
    }

    data object BodyMetrics : Destination("body_metrics")
    data object Nutrition : Destination("nutrition")
    data object Profile : Destination("profile")
    data object Wearables : Destination("wearables")
    data object Clinical : Destination("clinical")
}
