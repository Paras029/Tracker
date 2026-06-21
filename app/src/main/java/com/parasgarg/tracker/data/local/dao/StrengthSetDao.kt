package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parasgarg.tracker.data.local.entity.StrengthSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrengthSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sets: List<StrengthSetEntity>)

    @Query("SELECT * FROM strength_sets WHERE sessionId = :sessionId ORDER BY setOrder ASC")
    fun observeForSession(sessionId: String): Flow<List<StrengthSetEntity>>

    @Query("DELETE FROM strength_sets WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}
