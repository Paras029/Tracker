package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parasgarg.tracker.data.local.entity.RacquetSportDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RacquetSportDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(detail: RacquetSportDetailEntity)

    @Query("SELECT * FROM racquet_sport_details WHERE sessionId = :sessionId")
    fun observeForSession(sessionId: String): Flow<RacquetSportDetailEntity?>

    @Query("SELECT * FROM racquet_sport_details WHERE sessionId = :sessionId")
    suspend fun getForSession(sessionId: String): RacquetSportDetailEntity?

    @Query("DELETE FROM racquet_sport_details WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}
