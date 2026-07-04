package com.parasgarg.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private val KEY_USE_METRIC = booleanPreferencesKey("use_metric")

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    override fun observeUseMetric(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_USE_METRIC] ?: true }

    override suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_USE_METRIC] = useMetric }
    }
}
