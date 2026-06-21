package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSession>>
    fun observeSession(id: String): Flow<WorkoutSession?>
    suspend fun saveSession(session: WorkoutSession)
    suspend fun deleteSession(id: String)
}
