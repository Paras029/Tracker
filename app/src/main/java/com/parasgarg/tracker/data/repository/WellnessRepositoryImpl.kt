package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.WellnessDao
import com.parasgarg.tracker.data.local.entity.WellnessMetricEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.domain.WellnessMetric
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WellnessRepositoryImpl @Inject constructor(
    private val dao: WellnessDao,
) : WellnessRepository {

    override fun observeLatest(): Flow<WellnessMetric?> =
        dao.observeLatest().map { it?.toDomain() }

    override fun observeForDate(date: LocalDate): Flow<WellnessMetric?> =
        dao.observeForDate(date.toEpochDay()).map { it?.toDomain() }

    override suspend fun save(metric: WellnessMetric) {
        dao.upsert(metric.toEntity())
    }

    private fun WellnessMetricEntity.toDomain() = WellnessMetric(
        id = id,
        recordedDate = recordedDate,
        stepsCount = stepsCount,
        activeCaloriesKcal = activeCaloriesKcal,
        sleepHours = sleepHours,
        hrvMs = hrvMs,
        recoveryScore = recoveryScore,
        restingHeartRate = restingHeartRate,
        sourceName = sourceName,
    )

    private fun WellnessMetric.toEntity(): WellnessMetricEntity {
        val now = Instant.now()
        return WellnessMetricEntity(
            id = id,
            recordedDate = recordedDate,
            stepsCount = stepsCount,
            activeCaloriesKcal = activeCaloriesKcal,
            sleepHours = sleepHours,
            hrvMs = hrvMs,
            recoveryScore = recoveryScore,
            restingHeartRate = restingHeartRate,
            sourceName = sourceName,
            syncStatus = SyncStatus.PENDING,
            isDeleted = false,
            createdAt = now,
            updatedAt = now,
        )
    }
}
