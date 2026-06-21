package com.parasgarg.tracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.parasgarg.tracker.core.database.TrackerDatabase
import com.parasgarg.tracker.data.local.entity.WorkoutSessionEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.WorkoutType
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutSessionDaoTest {

    private lateinit var database: TrackerDatabase
    private lateinit var dao: WorkoutSessionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackerDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.workoutSessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun session(
        id: String = UUID.randomUUID().toString(),
        type: WorkoutType = WorkoutType.RUNNING,
        startTime: Instant = Instant.parse("2026-06-01T08:00:00Z"),
        isDeleted: Boolean = false,
        syncStatus: SyncStatus = SyncStatus.PENDING,
    ) = WorkoutSessionEntity(
        id = id,
        type = type,
        startTime = startTime,
        durationMinutes = 30,
        notes = null,
        perceivedEffort = 5,
        sourceId = "manual",
        externalId = null,
        syncStatus = syncStatus,
        isDeleted = isDeleted,
        createdAt = startTime,
        updatedAt = startTime,
    )

    @Test
    fun upsertAndGetById_returnsSession() = runTest {
        val entity = session()
        dao.upsert(entity)

        val loaded = dao.getById(entity.id)

        assert(loaded == entity)
    }

    @Test
    fun observeAll_excludesDeletedAndOrdersByStartTimeDescending() = runTest {
        val older = session(startTime = Instant.parse("2026-06-01T08:00:00Z"))
        val newer = session(startTime = Instant.parse("2026-06-02T08:00:00Z"))
        val deleted = session(startTime = Instant.parse("2026-06-03T08:00:00Z"), isDeleted = true)

        dao.upsert(older)
        dao.upsert(newer)
        dao.upsert(deleted)

        dao.observeAll().test {
            val sessions = awaitItem()
            assert(sessions.map { it.id } == listOf(newer.id, older.id))
        }
    }

    @Test
    fun getBySyncStatus_returnsOnlyMatching() = runTest {
        val pending = session(syncStatus = SyncStatus.PENDING)
        val synced = session(syncStatus = SyncStatus.SYNCED)

        dao.upsert(pending)
        dao.upsert(synced)

        val result = dao.getBySyncStatus(SyncStatus.PENDING)

        assert(result.map { it.id } == listOf(pending.id))
    }

    @Test
    fun deleteById_removesSession() = runTest {
        val entity = session()
        dao.upsert(entity)

        dao.deleteById(entity.id)

        assert(dao.getById(entity.id) == null)
    }
}
