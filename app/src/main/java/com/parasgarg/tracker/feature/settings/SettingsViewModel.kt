package com.parasgarg.tracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val useMetric: StateFlow<Boolean> = preferencesRepository.observeUseMetric()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setUseMetric(useMetric: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseMetric(useMetric) }
    }
}
