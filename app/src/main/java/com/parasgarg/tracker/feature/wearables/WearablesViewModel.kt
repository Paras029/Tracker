package com.parasgarg.tracker.feature.wearables

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.health.HealthConnectManager
import com.parasgarg.tracker.core.strava.StravaAuthManager
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.source.WearableConnector
import com.parasgarg.tracker.data.source.WearableRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConnectorState(
    val connector: WearableConnector,
    val connected: Boolean,
)

data class StravaConnectionState(
    val athleteId: Long? = null,
    val isConnected: Boolean = false,
    val lastSyncAt: Long = 0L,
)

data class WearablesUiState(
    val healthConnectAvailable: Boolean = false,
    val healthConnectHasPermissions: Boolean = false,
    val stravaState: StravaConnectionState = StravaConnectionState(),
)

@HiltViewModel
class WearablesViewModel @Inject constructor(
    val registry: WearableRegistry,
    private val hcManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val stravaAuthManager: StravaAuthManager,
) : ViewModel() {

    val connectedIds: StateFlow<Set<String>> = registry.connectedIds

    private val _uiState = MutableStateFlow(WearablesUiState())
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()

    init {
        checkHealthConnect()
        observeStravaState()
    }

    private fun checkHealthConnect() {
        _uiState.update { it.copy(healthConnectAvailable = hcManager.isAvailable()) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(healthConnectHasPermissions = hcManager.hasPermissions())
            }
        }
    }

    fun onHealthConnectPermissionsResult(granted: Map<String, Boolean>) {
        val grantedSet = granted.filterValues { it }.keys
        _uiState.update {
            it.copy(healthConnectHasPermissions = grantedSet.containsAll(hcManager.requiredPermissions))
        }
    }

    private fun observeStravaState() {
        viewModelScope.launch {
            preferencesRepository.observeStravaAthleteId().collect { athleteId ->
                _uiState.update { state ->
                    state.copy(
                        stravaState = state.stravaState.copy(
                            athleteId = athleteId,
                            isConnected = athleteId != null,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.observeStravaLastSyncAt().collect { lastSync ->
                _uiState.update { state ->
                    state.copy(stravaState = state.stravaState.copy(lastSyncAt = lastSync))
                }
            }
        }
    }

    fun toggle(sourceId: String, currentlyConnected: Boolean) {
        if (currentlyConnected) registry.disconnect(sourceId)
        else registry.connect(sourceId)
    }

    fun requiredHcPermissions(): Array<String> = hcManager.requiredPermissions.toTypedArray()

    fun connectStrava(context: Context) {
        viewModelScope.launch { stravaAuthManager.launchOAuth(context) }
    }

    fun disconnectStrava() {
        viewModelScope.launch { stravaAuthManager.disconnect() }
    }
}
