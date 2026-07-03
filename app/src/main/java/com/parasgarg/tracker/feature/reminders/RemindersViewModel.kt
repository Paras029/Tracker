package com.parasgarg.tracker.feature.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.notifications.ReminderScheduler
import com.parasgarg.tracker.data.local.dao.ReminderConfigDao
import com.parasgarg.tracker.data.local.entity.ReminderConfigEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val WORKOUT_REMINDER_ID = "workout_reminder"
private const val WORKOUT_REMINDER_REQUEST_CODE = 1001

data class ReminderUiState(
    val isEnabled: Boolean = false,
    val hour: Int = 8,
    val minute: Int = 0,
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val canScheduleExact: Boolean = true,
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val dao: ReminderConfigDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val scheduler = ReminderScheduler(context)
    private val _extra = MutableStateFlow(scheduler.canScheduleExact())

    val uiState: StateFlow<ReminderUiState> = combine(
        dao.observeAll(),
        _extra,
    ) { configs, canScheduleExact ->
        val config = configs.firstOrNull { it.id == WORKOUT_REMINDER_ID }
        ReminderUiState(
            isEnabled = config?.isEnabled ?: false,
            hour = config?.hour ?: 8,
            minute = config?.minute ?: 0,
            daysOfWeek = config?.daysOfWeek ?: DayOfWeek.entries.toSet(),
            canScheduleExact = canScheduleExact,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderUiState())

    fun toggleEnabled(enabled: Boolean) {
        val state = uiState.value
        viewModelScope.launch {
            saveConfig(enabled, state.hour, state.minute, state.daysOfWeek)
        }
    }

    fun setTime(hour: Int, minute: Int) {
        val state = uiState.value
        viewModelScope.launch {
            saveConfig(state.isEnabled, hour, minute, state.daysOfWeek)
        }
    }

    fun toggleDay(day: DayOfWeek) {
        val state = uiState.value
        val newDays = if (day in state.daysOfWeek) state.daysOfWeek - day else state.daysOfWeek + day
        viewModelScope.launch {
            saveConfig(state.isEnabled, state.hour, state.minute, newDays)
        }
    }

    fun refreshExactAlarmPermission() {
        _extra.value = scheduler.canScheduleExact()
    }

    private suspend fun saveConfig(
        enabled: Boolean,
        hour: Int,
        minute: Int,
        days: Set<DayOfWeek>,
    ) {
        dao.upsert(
            ReminderConfigEntity(
                id = WORKOUT_REMINDER_ID,
                reminderType = "WORKOUT",
                hour = hour,
                minute = minute,
                isEnabled = enabled,
                daysOfWeek = days,
            ),
        )
        if (enabled && scheduler.canScheduleExact()) {
            scheduler.schedule(hour, minute, WORKOUT_REMINDER_REQUEST_CODE)
        } else {
            scheduler.cancel(WORKOUT_REMINDER_REQUEST_CODE)
        }
    }
}
