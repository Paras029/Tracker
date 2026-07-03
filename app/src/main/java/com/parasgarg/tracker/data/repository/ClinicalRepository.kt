package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.BcaReport
import com.parasgarg.tracker.data.model.domain.BloodTestReport
import kotlinx.coroutines.flow.Flow

interface ClinicalRepository {
    fun observeBloodTests(): Flow<List<BloodTestReport>>
    fun observeLatestBloodTest(): Flow<BloodTestReport?>
    suspend fun saveBloodTest(report: BloodTestReport)
    suspend fun deleteBloodTest(id: String)

    fun observeBcaReports(): Flow<List<BcaReport>>
    fun observeLatestBcaReport(): Flow<BcaReport?>
    suspend fun saveBcaReport(report: BcaReport)
    suspend fun deleteBcaReport(id: String)
}
