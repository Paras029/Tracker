package com.parasgarg.tracker.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeUseMetric(): Flow<Boolean>
    suspend fun setUseMetric(useMetric: Boolean)
}
