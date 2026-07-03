package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.WellnessMetric
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface WellnessRepository {
    fun observeLatest(): Flow<WellnessMetric?>
    fun observeForDate(date: LocalDate): Flow<WellnessMetric?>
    suspend fun save(metric: WellnessMetric)
}
