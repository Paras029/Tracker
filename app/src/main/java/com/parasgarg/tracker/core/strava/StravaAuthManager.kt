package com.parasgarg.tracker.core.strava

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.UserProfileRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

enum class StravaAuthState { NOT_CONNECTED, CONNECTED, TOKEN_EXPIRED }

@Singleton
class StravaAuthManager @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val profileRepository: UserProfileRepository,
    private val httpClient: OkHttpClient,
) {

    companion object {
        private const val AUTH_BASE = "https://www.strava.com/oauth/authorize"
        private const val TOKEN_URL = "https://www.strava.com/oauth/token"
        const val REDIRECT_URI = "tracker://strava/callback"
        private const val SCOPE = "activity:read_all"
    }

    val authState: Flow<StravaAuthState> = combine(
        prefsRepository.observeStravaAthleteId(),
        prefsRepository.observeStravaExpiresAt(),
    ) { athleteId, expiresAt ->
        when {
            athleteId == null -> StravaAuthState.NOT_CONNECTED
            expiresAt < Instant.now().epochSecond -> StravaAuthState.TOKEN_EXPIRED
            else -> StravaAuthState.CONNECTED
        }
    }

    suspend fun launchOAuth(context: Context) {
        val profile = profileRepository.observe().first() ?: return
        val clientId = profile.stravaClientId ?: return
        val url = "$AUTH_BASE?client_id=$clientId&response_type=code" +
            "&redirect_uri=$REDIRECT_URI&scope=$SCOPE&approval_prompt=auto"
        withContext(Dispatchers.Main) {
            CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
        }
    }

    suspend fun exchangeCode(code: String): Boolean {
        val profile = profileRepository.observe().first() ?: return false
        val clientId = profile.stravaClientId ?: return false
        val clientSecret = profile.stravaClientSecret ?: return false
        return runCatching {
            val body = FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build()
            val request = Request.Builder().url(TOKEN_URL).post(body).build()
            val response = httpClient.newCall(request).execute()
            val json = JSONObject(response.body!!.string())
            val accessToken = json.getString("access_token")
            val refreshToken = json.getString("refresh_token")
            val expiresAt = json.getLong("expires_at")
            val athleteId = json.getJSONObject("athlete").getLong("id")
            prefsRepository.saveStravaTokens(accessToken, refreshToken, expiresAt, athleteId)
            true
        }.getOrDefault(false)
    }

    suspend fun getValidAccessToken(): String? {
        val expiresAt = prefsRepository.observeStravaExpiresAt().first()
        return if (expiresAt > Instant.now().epochSecond + 300) {
            prefsRepository.observeStravaAccessToken().first()
        } else {
            refreshToken()
        }
    }

    private suspend fun refreshToken(): String? {
        val profile = profileRepository.observe().first() ?: return null
        val clientId = profile.stravaClientId ?: return null
        val clientSecret = profile.stravaClientSecret ?: return null
        val refreshToken = prefsRepository.observeStravaRefreshToken().first() ?: return null
        return runCatching {
            val body = FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build()
            val request = Request.Builder().url(TOKEN_URL).post(body).build()
            val response = httpClient.newCall(request).execute()
            val json = JSONObject(response.body!!.string())
            val newAccessToken = json.getString("access_token")
            val newRefreshToken = json.getString("refresh_token")
            val newExpiresAt = json.getLong("expires_at")
            val athleteId = prefsRepository.observeStravaAthleteId().first() ?: return null
            prefsRepository.saveStravaTokens(newAccessToken, newRefreshToken, newExpiresAt, athleteId)
            newAccessToken
        }.getOrNull()
    }

    /**
     * Saves tokens pasted directly from strava.com/settings/api, skipping the OAuth flow.
     * Fetches athlete ID from the API to confirm the token works and mark the account connected.
     * Sets expiresAt = 0 so the first sync auto-refreshes to a fresh token.
     */
    suspend fun saveManualTokens(accessToken: String, refreshToken: String): Boolean {
        if (accessToken.isBlank() || refreshToken.isBlank()) return false
        val athleteId = fetchAthleteId(accessToken) ?: return false
        prefsRepository.saveStravaTokens(accessToken, refreshToken, 0L, athleteId)
        return true
    }

    private suspend fun fetchAthleteId(accessToken: String): Long? = runCatching {
        val request = Request.Builder()
            .url("https://www.strava.com/api/v3/athlete")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        val response = httpClient.newCall(request).execute()
        JSONObject(response.body!!.string()).getLong("id")
    }.getOrNull()

    suspend fun disconnect() = prefsRepository.clearStravaTokens()
}
