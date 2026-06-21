package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parasgarg.tracker.data.local.entity.ReminderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: ReminderConfigEntity)

    @Query("SELECT * FROM reminder_configs")
    fun observeAll(): Flow<List<ReminderConfigEntity>>

    @Query("DELETE FROM reminder_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
