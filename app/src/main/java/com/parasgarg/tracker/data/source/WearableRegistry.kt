package com.parasgarg.tracker.data.source

import com.parasgarg.tracker.data.model.domain.WellnessMetric
import com.parasgarg.tracker.data.repository.WellnessRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class WearableRegistry @Inject constructor() {

    val all: List<WearableConnector> = listOf(
        SamsungHealthConnector(),
        GoogleFitConnector(),
        OuraConnector(),
        FitbitConnector(),
        WhoopConnector(),
        GarminConnector(),
    )

    private val _connectedIds = MutableStateFlow(setOf("samsung_health"))
    val connectedIds: StateFlow<Set<String>> = _connectedIds.asStateFlow()

    fun connect(sourceId: String) = _connectedIds.update { it + sourceId }
    fun disconnect(sourceId: String) = _connectedIds.update { it - sourceId }

    private fun connectedConnectors(): List<WearableConnector> =
        all.filter { it.sourceId in _connectedIds.value }

    suspend fun syncToday(wellnessRepository: WellnessRepository) {
        connectedConnectors().forEach { connector ->
            connector.fetchTodayMetrics()?.let { metric ->
                wellnessRepository.save(metric)
            }
        }
    }

    suspend fun fetchTodayFromAll(): List<WellnessMetric> =
        connectedConnectors().mapNotNull { it.fetchTodayMetrics() }
}
