package com.parasgarg.tracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.AuthRepository
import com.parasgarg.tracker.data.repository.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val useMetric: StateFlow<Boolean> = preferencesRepository.observeUseMetric()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val currentUser: StateFlow<AuthUser?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isFirebaseAvailable: Boolean = authRepository.isFirebaseAvailable()

    fun setUseMetric(useMetric: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseMetric(useMetric) }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
