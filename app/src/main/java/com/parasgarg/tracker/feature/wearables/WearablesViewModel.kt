package com.parasgarg.tracker.feature.wearables

import androidx.lifecycle.ViewModel
import com.parasgarg.tracker.data.source.WearableConnector
import com.parasgarg.tracker.data.source.WearableRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

data class ConnectorState(
    val connector: WearableConnector,
    val connected: Boolean,
)

@HiltViewModel
class WearablesViewModel @Inject constructor(
    val registry: WearableRegistry,
) : ViewModel() {

    val connectedIds: StateFlow<Set<String>> = registry.connectedIds

    fun toggle(sourceId: String, currentlyConnected: Boolean) {
        if (currentlyConnected) registry.disconnect(sourceId)
        else registry.connect(sourceId)
    }
}
