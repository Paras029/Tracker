package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSession>>
    fun observeSession(id: String): Flow<WorkoutSession?>
    suspend fun getSessionById(id: String): WorkoutSession?
    suspend fun saveSession(session: WorkoutSession)
    suspend fun importExternalSession(session: WorkoutSession, sourceId: String, externalId: String)
    suspend fun deleteSession(id: String)
}
