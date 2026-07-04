package com.parasgarg.tracker.core.strava

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class StravaActivity(
    val id: Long,
    val name: String,
    val type: String,
    val startDateLocal: String,
    val elapsedSeconds: Int,
    val distanceMeters: Double,
    val averageSpeedMps: Double,
    val totalElevationGain: Double,
    val summaryPolyline: String?,
)

@Singleton
class StravaApiService @Inject constructor(
    private val httpClient: OkHttpClient,
) {
    companion object {
        private const val BASE_URL = "https://www.strava.com/api/v3"
    }

    suspend fun getActivities(accessToken: String, after: Long, page: Int = 1): List<StravaActivity> =
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/athlete/activities?after=$after&per_page=50&page=$page")
                .header("Authorization", "Bearer $accessToken")
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@runCatching emptyList()
            parseActivities(JSONArray(body))
        }.getOrDefault(emptyList())

    private fun parseActivities(array: JSONArray): List<StravaActivity> {
        val result = mutableListOf<StravaActivity>()
        for (i in 0 until array.length()) {
            runCatching {
                val obj: JSONObject = array.getJSONObject(i)
                val map = obj.optJSONObject("map")
                result.add(
                    StravaActivity(
                        id = obj.getLong("id"),
                        name = obj.optString("name", "Strava Activity"),
                        type = obj.optString("type", "Workout"),
                        startDateLocal = obj.optString("start_date_local", ""),
                        elapsedSeconds = obj.optInt("elapsed_time", 0),
                        distanceMeters = obj.optDouble("distance", 0.0),
                        averageSpeedMps = obj.optDouble("average_speed", 0.0),
                        totalElevationGain = obj.optDouble("total_elevation_gain", 0.0),
                        summaryPolyline = map?.optString("summary_polyline"),
                    ),
                )
            }
        }
        return result
    }
}
