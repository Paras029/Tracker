package com.parasgarg.tracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.parasgarg.tracker.core.database.TrackerDatabase
import com.parasgarg.tracker.data.local.entity.BodyMetricEntity
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BodyMetricDaoTest {

    private lateinit var database: TrackerDatabase
    private lateinit var dao: BodyMetricDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackerDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.bodyMetricDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun metric(
        id: String = UUID.randomUUID().toString(),
        recordedAt: Instant = Instant.parse("2026-06-01T08:00:00Z"),
        weightKg: Double? = 75.0,
        isDeleted: Boolean = false,
    ) = BodyMetricEntity(
        id = id,
        recordedAt = recordedAt,
        weightKg = weightKg,
        bodyFatPercent = null,
        restingHeartRate = null,
        notes = null,
        syncStatus = SyncStatus.PENDING,
        isDeleted = isDeleted,
        createdAt = recordedAt,
        updatedAt = recordedAt,
    )

    @Test
    fun observeLatest_returnsMostRecentNonDeleted() = runTest {
        val older = metric(recordedAt = Instant.parse("2026-06-01T08:00:00Z"), weightKg = 76.0)
        val newer = metric(recordedAt = Instant.parse("2026-06-05T08:00:00Z"), weightKg = 75.0)
        val deletedNewest = metric(recordedAt = Instant.parse("2026-06-06T08:00:00Z"), isDeleted = true)

        dao.upsert(older)
        dao.upsert(newer)
        dao.upsert(deletedNewest)

        dao.observeLatest().test {
            val latest = awaitItem()
            assert(latest?.id == newer.id)
        }
    }

    @Test
    fun observeAll_emitsInsertedMetricsInDescendingOrder() = runTest {
        val older = metric(recordedAt = Instant.parse("2026-06-01T08:00:00Z"))
        val newer = metric(recordedAt = Instant.parse("2026-06-02T08:00:00Z"))

        dao.upsert(older)
        dao.upsert(newer)

        dao.observeAll().test {
            val all = awaitItem()
            assert(all.map { it.id } == listOf(newer.id, older.id))
        }
    }
}
