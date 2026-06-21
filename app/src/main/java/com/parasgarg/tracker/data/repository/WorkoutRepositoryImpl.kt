package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.CardioDetailDao
import com.parasgarg.tracker.data.local.dao.RacquetSportDetailDao
import com.parasgarg.tracker.data.local.dao.StrengthSetDao
import com.parasgarg.tracker.data.local.dao.WorkoutSessionDao
import com.parasgarg.tracker.data.local.entity.CardioDetailEntity
import com.parasgarg.tracker.data.local.entity.RacquetSportDetailEntity
import com.parasgarg.tracker.data.local.entity.StrengthSetEntity
import com.parasgarg.tracker.data.local.entity.WorkoutSessionEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.StrengthSetInput
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val strengthSetDao: StrengthSetDao,
    private val cardioDetailDao: CardioDetailDao,
    private val racquetSportDetailDao: RacquetSportDetailDao,
) : WorkoutRepository {

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        sessionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeSession(id: String): Flow<WorkoutSession?> =
        sessionDao.observeById(id).map { it?.toDomain() }

    override suspend fun saveSession(session: WorkoutSession) {
        val now = Instant.now()
        val existing = sessionDao.getById(session.id)
        val entity = WorkoutSessionEntity(
            id = session.id,
            type = session.type,
            startTime = session.startTime,
            durationMinutes = session.durationMinutes,
            notes = session.notes,
            perceivedEffort = session.perceivedEffort,
            sourceId = existing?.sourceId ?: "manual",
            externalId = existing?.externalId,
            syncStatus = SyncStatus.PENDING,
            isDeleted = false,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        sessionDao.upsert(entity)

        strengthSetDao.deleteForSession(session.id)
        cardioDetailDao.deleteForSession(session.id)
        racquetSportDetailDao.deleteForSession(session.id)

        when (val detail = session.detail) {
            is WorkoutDetail.Strength -> strengthSetDao.upsertAll(
                detail.sets.map { it.toEntity(session.id) },
            )
            is WorkoutDetail.Cardio -> cardioDetailDao.upsert(detail.toEntity(session.id))
            is WorkoutDetail.Racquet -> racquetSportDetailDao.upsert(detail.toEntity(session.id))
            WorkoutDetail.None -> Unit
        }
    }

    override suspend fun deleteSession(id: String) {
        val existing = sessionDao.getById(id) ?: return
        sessionDao.update(
            existing.copy(
                isDeleted = true,
                syncStatus = SyncStatus.PENDING,
                updatedAt = Instant.now(),
            ),
        )
    }

    private suspend fun WorkoutSessionEntity.toDomain(): WorkoutSession {
        val detail = when (type) {
            WorkoutType.STRENGTH -> WorkoutDetail.Strength(
                strengthSetDao.getForSession(id).map { it.toDomain() },
            )
            WorkoutType.RUNNING, WorkoutType.SWIMMING -> cardioDetailDao.getForSession(id)?.toDomain()
                ?: WorkoutDetail.None
            WorkoutType.BADMINTON, WorkoutType.TABLE_TENNIS -> racquetSportDetailDao.getForSession(id)?.toDomain()
                ?: WorkoutDetail.None
            WorkoutType.OTHER -> WorkoutDetail.None
        }
        return WorkoutSession(
            id = id,
            type = type,
            startTime = startTime,
            durationMinutes = durationMinutes,
            notes = notes,
            perceivedEffort = perceivedEffort,
            detail = detail,
        )
    }
}

private fun StrengthSetEntity.toDomain() = StrengthSetInput(
    exerciseName = exerciseName,
    setOrder = setOrder,
    reps = reps,
    weightKg = weightKg,
    restSeconds = restSeconds,
)

private fun StrengthSetInput.toEntity(sessionId: String) = StrengthSetEntity(
    id = UUID.randomUUID().toString(),
    sessionId = sessionId,
    exerciseName = exerciseName,
    setOrder = setOrder,
    reps = reps,
    weightKg = weightKg,
    restSeconds = restSeconds,
)

private fun CardioDetailEntity.toDomain() = WorkoutDetail.Cardio(
    distanceMeters = distanceMeters,
    avgPaceSecondsPerKm = avgPaceSecondsPerKm,
    laps = laps,
    poolLengthMeters = poolLengthMeters,
)

private fun WorkoutDetail.Cardio.toEntity(sessionId: String) = CardioDetailEntity(
    sessionId = sessionId,
    distanceMeters = distanceMeters,
    avgPaceSecondsPerKm = avgPaceSecondsPerKm,
    laps = laps,
    poolLengthMeters = poolLengthMeters,
    routeGeoJson = null,
)

private fun RacquetSportDetailEntity.toDomain() = WorkoutDetail.Racquet(
    opponentName = opponentName,
    setsWon = setsWon,
    setsLost = setsLost,
    scoreSummary = scoreSummary,
)

private fun WorkoutDetail.Racquet.toEntity(sessionId: String) = RacquetSportDetailEntity(
    sessionId = sessionId,
    opponentName = opponentName,
    setsWon = setsWon,
    setsLost = setsLost,
    scoreSummary = scoreSummary,
)
