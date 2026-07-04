package com.parasgarg.tracker.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeUseMetric(): Flow<Boolean>
    suspend fun setUseMetric(useMetric: Boolean)

    fun observeConnectedWearableIds(): Flow<Set<String>>
    suspend fun setConnectedWearableIds(ids: Set<String>)

    fun observeStravaAccessToken(): Flow<String?>
    fun observeStravaRefreshToken(): Flow<String?>
    fun observeStravaExpiresAt(): Flow<Long>
    fun observeStravaAthleteId(): Flow<Long?>
    fun observeStravaLastSyncAt(): Flow<Long>
    suspend fun saveStravaTokens(accessToken: String, refreshToken: String, expiresAt: Long, athleteId: Long)
    suspend fun clearStravaTokens()
    suspend fun setStravaLastSyncAt(epochSeconds: Long)
    suspend fun getStravaLastSyncAt(): Long
}
