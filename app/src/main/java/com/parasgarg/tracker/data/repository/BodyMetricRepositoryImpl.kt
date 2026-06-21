package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.BodyMetricDao
import com.parasgarg.tracker.data.local.entity.BodyMetricEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.domain.BodyMetric
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BodyMetricRepositoryImpl @Inject constructor(
    private val bodyMetricDao: BodyMetricDao,
) : BodyMetricRepository {

    override fun observeAll(): Flow<List<BodyMetric>> =
        bodyMetricDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeLatest(): Flow<BodyMetric?> =
        bodyMetricDao.observeLatest().map { it?.toDomain() }

    override suspend fun save(metric: BodyMetric) {
        val now = Instant.now()
        bodyMetricDao.upsert(
            BodyMetricEntity(
                id = metric.id,
                recordedAt = metric.recordedAt,
                weightKg = metric.weightKg,
                bodyFatPercent = metric.bodyFatPercent,
                restingHeartRate = metric.restingHeartRate,
                notes = metric.notes,
                syncStatus = SyncStatus.PENDING,
                isDeleted = false,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun delete(id: String) {
        val existing = bodyMetricDao.getById(id) ?: return
        bodyMetricDao.update(
            existing.copy(
                isDeleted = true,
                syncStatus = SyncStatus.PENDING,
                updatedAt = Instant.now(),
            ),
        )
    }
}

private fun BodyMetricEntity.toDomain() = BodyMetric(
    id = id,
    recordedAt = recordedAt,
    weightKg = weightKg,
    bodyFatPercent = bodyFatPercent,
    restingHeartRate = restingHeartRate,
    notes = notes,
)
