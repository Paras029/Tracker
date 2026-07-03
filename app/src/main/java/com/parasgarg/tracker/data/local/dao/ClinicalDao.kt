package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.parasgarg.tracker.data.local.entity.BcaReportEntity
import com.parasgarg.tracker.data.local.entity.BloodTestReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalDao {

    @Query("SELECT * FROM blood_test_reports WHERE isDeleted = 0 ORDER BY testedAt DESC")
    fun observeBloodTests(): Flow<List<BloodTestReportEntity>>

    @Query("SELECT * FROM blood_test_reports WHERE isDeleted = 0 ORDER BY testedAt DESC LIMIT 1")
    fun observeLatestBloodTest(): Flow<BloodTestReportEntity?>

    @Upsert
    suspend fun upsertBloodTest(entity: BloodTestReportEntity)

    @Query("UPDATE blood_test_reports SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteBloodTest(id: String, updatedAt: Long)

    @Query("SELECT * FROM bca_reports WHERE isDeleted = 0 ORDER BY scannedAt DESC")
    fun observeBcaReports(): Flow<List<BcaReportEntity>>

    @Query("SELECT * FROM bca_reports WHERE isDeleted = 0 ORDER BY scannedAt DESC LIMIT 1")
    fun observeLatestBcaReport(): Flow<BcaReportEntity?>

    @Upsert
    suspend fun upsertBcaReport(entity: BcaReportEntity)

    @Query("UPDATE bca_reports SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteBcaReport(id: String, updatedAt: Long)
}
