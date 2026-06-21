package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.BodyMetric
import kotlinx.coroutines.flow.Flow

interface BodyMetricRepository {
    fun observeAll(): Flow<List<BodyMetric>>
    fun observeLatest(): Flow<BodyMetric?>
    suspend fun save(metric: BodyMetric)
    suspend fun delete(id: String)
}
