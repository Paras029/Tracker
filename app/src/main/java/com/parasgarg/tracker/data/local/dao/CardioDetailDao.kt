package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parasgarg.tracker.data.local.entity.CardioDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(detail: CardioDetailEntity)

    @Query("SELECT * FROM cardio_details WHERE sessionId = :sessionId")
    fun observeForSession(sessionId: String): Flow<CardioDetailEntity?>

    @Query("DELETE FROM cardio_details WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}
