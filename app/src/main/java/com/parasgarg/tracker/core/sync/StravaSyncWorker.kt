package com.parasgarg.tracker.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.parasgarg.tracker.core.strava.StravaApiService
import com.parasgarg.tracker.core.strava.StravaAuthManager
import com.parasgarg.tracker.core.strava.StravaWorkoutMapper
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant

@HiltWorker
class StravaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stravaAuthManager: StravaAuthManager,
    private val stravaApiService: StravaApiService,
    private val workoutRepository: WorkoutRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val accessToken = stravaAuthManager.getValidAccessToken() ?: return Result.success()
        val lastSyncAt = preferencesRepository.getStravaLastSyncAt()
        val activities = stravaApiService.getActivities(accessToken, after = lastSyncAt)
        for (activity in activities) {
            val session = StravaWorkoutMapper.map(activity)
            val existing = workoutRepository.getSessionById(session.id)
            if (existing == null) {
                workoutRepository.importExternalSession(
                    session = session,
                    sourceId = "strava",
                    externalId = activity.id.toString(),
                )
            }
        }
        preferencesRepository.setStravaLastSyncAt(Instant.now().epochSecond)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "strava_sync"
    }
}
