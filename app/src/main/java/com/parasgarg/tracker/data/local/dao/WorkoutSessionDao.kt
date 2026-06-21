package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parasgarg.tracker.data.local.entity.WorkoutSessionEntity
import com.parasgarg.tracker.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: WorkoutSessionEntity)

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: SyncStatus): List<WorkoutSessionEntity>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
