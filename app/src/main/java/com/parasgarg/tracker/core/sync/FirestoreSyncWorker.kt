package com.parasgarg.tracker.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parasgarg.tracker.data.local.dao.WorkoutSessionDao
import com.parasgarg.tracker.data.model.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val workoutSessionDao: WorkoutSessionDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!isFirebaseAvailable()) return Result.success()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        val firestore = FirebaseFirestore.getInstance()
        val pendingSessions = workoutSessionDao.getBySyncStatus(SyncStatus.PENDING)
        for (session in pendingSessions) {
            runCatching {
                if (session.isDeleted) {
                    firestore.collection("users").document(uid)
                        .collection("workouts").document(session.id)
                        .delete().await()
                } else {
                    val data = mapOf(
                        "id" to session.id,
                        "type" to session.type.name,
                        "startTime" to session.startTime.toEpochMilli(),
                        "durationMinutes" to session.durationMinutes,
                        "notes" to session.notes,
                        "perceivedEffort" to session.perceivedEffort,
                        "sourceId" to session.sourceId,
                        "externalId" to session.externalId,
                        "updatedAt" to session.updatedAt.toEpochMilli(),
                    )
                    firestore.collection("users").document(uid)
                        .collection("workouts").document(session.id)
                        .set(data).await()
                }
                workoutSessionDao.update(session.copy(syncStatus = SyncStatus.SYNCED))
            }.onFailure {
                workoutSessionDao.update(session.copy(syncStatus = SyncStatus.FAILED))
            }
        }
        return Result.success()
    }

    private fun isFirebaseAvailable(): Boolean =
        runCatching { FirebaseApp.getInstance(); true }.getOrDefault(false)

    companion object {
        const val WORK_NAME = "firestore_sync"
    }
}
