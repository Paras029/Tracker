package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parasgarg.tracker.data.local.entity.BodyMetricEntity
import com.parasgarg.tracker.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metric: BodyMetricEntity)

    @Update
    suspend fun update(metric: BodyMetricEntity)

    @Query("SELECT * FROM body_metrics WHERE id = :id")
    suspend fun getById(id: String): BodyMetricEntity?

    @Query("SELECT * FROM body_metrics WHERE isDeleted = 0 ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics WHERE isDeleted = 0 ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMetricEntity?>

    @Query("SELECT * FROM body_metrics WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: SyncStatus): List<BodyMetricEntity>

    @Query("DELETE FROM body_metrics WHERE id = :id")
    suspend fun deleteById(id: String)
}
