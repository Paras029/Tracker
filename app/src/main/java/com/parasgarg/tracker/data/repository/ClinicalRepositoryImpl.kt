package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.ClinicalDao
import com.parasgarg.tracker.data.local.entity.BcaReportEntity
import com.parasgarg.tracker.data.local.entity.BloodTestReportEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.domain.BcaReport
import com.parasgarg.tracker.data.model.domain.BloodTestReport
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClinicalRepositoryImpl @Inject constructor(
    private val dao: ClinicalDao,
) : ClinicalRepository {

    override fun observeBloodTests(): Flow<List<BloodTestReport>> =
        dao.observeBloodTests().map { it.map { e -> e.toDomain() } }

    override fun observeLatestBloodTest(): Flow<BloodTestReport?> =
        dao.observeLatestBloodTest().map { it?.toDomain() }

    override suspend fun saveBloodTest(report: BloodTestReport) {
        dao.upsertBloodTest(report.toEntity())
    }

    override suspend fun deleteBloodTest(id: String) {
        dao.softDeleteBloodTest(id, Instant.now().toEpochMilli())
    }

    override fun observeBcaReports(): Flow<List<BcaReport>> =
        dao.observeBcaReports().map { it.map { e -> e.toDomain() } }

    override fun observeLatestBcaReport(): Flow<BcaReport?> =
        dao.observeLatestBcaReport().map { it?.toDomain() }

    override suspend fun saveBcaReport(report: BcaReport) {
        dao.upsertBcaReport(report.toEntity())
    }

    override suspend fun deleteBcaReport(id: String) {
        dao.softDeleteBcaReport(id, Instant.now().toEpochMilli())
    }

    private fun BloodTestReportEntity.toDomain() = BloodTestReport(
        id = id, testedAt = testedAt, labName = labName,
        hemoglobinGdl = hemoglobinGdl, vitaminD3NgMl = vitaminD3NgMl,
        vitaminB12PgMl = vitaminB12PgMl, ldlMgDl = ldlMgDl, hdlMgDl = hdlMgDl,
        fastingBloodSugarMgDl = fastingBloodSugarMgDl, hba1cPercent = hba1cPercent,
        notes = notes,
    )

    private fun BloodTestReport.toEntity(): BloodTestReportEntity {
        val now = Instant.now()
        return BloodTestReportEntity(
            id = id, testedAt = testedAt, labName = labName,
            hemoglobinGdl = hemoglobinGdl, vitaminD3NgMl = vitaminD3NgMl,
            vitaminB12PgMl = vitaminB12PgMl, ldlMgDl = ldlMgDl, hdlMgDl = hdlMgDl,
            fastingBloodSugarMgDl = fastingBloodSugarMgDl, hba1cPercent = hba1cPercent,
            notes = notes, syncStatus = SyncStatus.PENDING, isDeleted = false,
            createdAt = now, updatedAt = now,
        )
    }

    private fun BcaReportEntity.toDomain() = BcaReport(
        id = id, scannedAt = scannedAt, bodyFatPercent = bodyFatPercent,
        skeletalMuscleMassKg = skeletalMuscleMassKg, visceralFatLevel = visceralFatLevel,
        waterPercent = waterPercent, boneMassKg = boneMassKg, bmi = bmi,
        deviceName = deviceName,
    )

    private fun BcaReport.toEntity(): BcaReportEntity {
        val now = Instant.now()
        return BcaReportEntity(
            id = id, scannedAt = scannedAt, bodyFatPercent = bodyFatPercent,
            skeletalMuscleMassKg = skeletalMuscleMassKg, visceralFatLevel = visceralFatLevel,
            waterPercent = waterPercent, boneMassKg = boneMassKg, bmi = bmi,
            deviceName = deviceName, syncStatus = SyncStatus.PENDING, isDeleted = false,
            createdAt = now, updatedAt = now,
        )
    }
}
