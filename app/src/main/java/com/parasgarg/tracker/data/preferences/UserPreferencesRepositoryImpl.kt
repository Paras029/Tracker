package com.parasgarg.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val KEY_USE_METRIC = booleanPreferencesKey("use_metric")
private val KEY_CONNECTED_WEARABLE_IDS = stringPreferencesKey("connected_wearable_ids")
private val KEY_STRAVA_ACCESS_TOKEN = stringPreferencesKey("strava_access_token")
private val KEY_STRAVA_REFRESH_TOKEN = stringPreferencesKey("strava_refresh_token")
private val KEY_STRAVA_EXPIRES_AT = longPreferencesKey("strava_expires_at")
private val KEY_STRAVA_ATHLETE_ID = longPreferencesKey("strava_athlete_id")
private val KEY_STRAVA_LAST_SYNC_AT = longPreferencesKey("strava_last_sync_at")

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    override fun observeUseMetric(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_USE_METRIC] ?: true }

    override suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_USE_METRIC] = useMetric }
    }

    override fun observeConnectedWearableIds(): Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_CONNECTED_WEARABLE_IDS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: setOf("samsung_health")
        }

    override suspend fun setConnectedWearableIds(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONNECTED_WEARABLE_IDS] = ids.joinToString(",")
        }
    }

    override fun observeStravaAccessToken(): Flow<String?> =
        context.dataStore.data.map { it[KEY_STRAVA_ACCESS_TOKEN] }

    override fun observeStravaRefreshToken(): Flow<String?> =
        context.dataStore.data.map { it[KEY_STRAVA_REFRESH_TOKEN] }

    override fun observeStravaExpiresAt(): Flow<Long> =
        context.dataStore.data.map { it[KEY_STRAVA_EXPIRES_AT] ?: 0L }

    override fun observeStravaAthleteId(): Flow<Long?> =
        context.dataStore.data.map { it[KEY_STRAVA_ATHLETE_ID] }

    override fun observeStravaLastSyncAt(): Flow<Long> =
        context.dataStore.data.map { it[KEY_STRAVA_LAST_SYNC_AT] ?: 0L }

    override suspend fun saveStravaTokens(
        accessToken: String,
        refreshToken: String,
        expiresAt: Long,
        athleteId: Long,
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STRAVA_ACCESS_TOKEN] = accessToken
            prefs[KEY_STRAVA_REFRESH_TOKEN] = refreshToken
            prefs[KEY_STRAVA_EXPIRES_AT] = expiresAt
            prefs[KEY_STRAVA_ATHLETE_ID] = athleteId
        }
    }

    override suspend fun clearStravaTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_STRAVA_ACCESS_TOKEN)
            prefs.remove(KEY_STRAVA_REFRESH_TOKEN)
            prefs.remove(KEY_STRAVA_EXPIRES_AT)
            prefs.remove(KEY_STRAVA_ATHLETE_ID)
        }
    }

    override suspend fun setStravaLastSyncAt(epochSeconds: Long) {
        context.dataStore.edit { prefs -> prefs[KEY_STRAVA_LAST_SYNC_AT] = epochSeconds }
    }

    override suspend fun getStravaLastSyncAt(): Long =
        context.dataStore.data.first()[KEY_STRAVA_LAST_SYNC_AT] ?: 0L
}
