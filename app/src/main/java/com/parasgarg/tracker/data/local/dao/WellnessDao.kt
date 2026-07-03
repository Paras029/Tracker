package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.parasgarg.tracker.data.local.entity.WellnessMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Query("SELECT * FROM wellness_metrics WHERE isDeleted = 0 ORDER BY recordedDate DESC LIMIT 1")
    fun observeLatest(): Flow<WellnessMetricEntity?>

    @Query("SELECT * FROM wellness_metrics WHERE recordedDate = :epochDay AND isDeleted = 0 LIMIT 1")
    fun observeForDate(epochDay: Long): Flow<WellnessMetricEntity?>

    @Upsert
    suspend fun upsert(entity: WellnessMetricEntity)
}
