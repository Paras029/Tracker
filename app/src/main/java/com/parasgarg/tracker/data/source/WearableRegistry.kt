package com.parasgarg.tracker.data.source

import com.parasgarg.tracker.data.model.domain.WellnessMetric
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.WellnessRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
class WearableRegistry @Inject constructor(
    connectors: Set<@JvmSuppressWildcards WearableConnector>,
    private val preferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val all: List<WearableConnector> = connectors.sortedBy { it.displayName }

    val connectedIds: StateFlow<Set<String>> = preferencesRepository
        .observeConnectedWearableIds()
        .stateIn(scope, SharingStarted.Eagerly, setOf("samsung_health"))

    fun connect(sourceId: String) {
        scope.launch {
            preferencesRepository.setConnectedWearableIds(connectedIds.value + sourceId)
        }
    }

    fun disconnect(sourceId: String) {
        scope.launch {
            preferencesRepository.setConnectedWearableIds(connectedIds.value - sourceId)
        }
    }

    private fun connectedConnectors(): List<WearableConnector> =
        all.filter { it.sourceId in connectedIds.value }

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
