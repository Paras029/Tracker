package com.parasgarg.tracker.core.ai

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val GEMINI_ENDPOINT =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

@Singleton
class GeminiService @Inject constructor(private val httpClient: OkHttpClient) {

    suspend fun getNutritionTip(
        apiKey: String,
        calsToday: Float,
        calTarget: Float,
        proteinToday: Float,
        proteinTarget: Float,
    ): String {
        val prompt = buildString {
            append("I am tracking my nutrition. Today I've consumed ${calsToday.toInt()} kcal")
            if (calTarget > 0f) append(" (target: ${calTarget.toInt()} kcal)")
            append(" and ${proteinToday.toInt()}g protein")
            if (proteinTarget > 0f) append(" (target: ${proteinTarget.toInt()}g)")
            append(". Give me a short, practical, encouraging tip (2-3 sentences) to stay on track with my nutrition goals today.")
        }
        return callGemini(apiKey, prompt) ?: nutritionFallback(calsToday, calTarget)
    }

    suspend fun getWorkoutTip(
        apiKey: String,
        workoutType: String,
        durationMinutes: Int,
        distanceKm: Double?,
    ): String {
        val prompt = buildString {
            append("I just completed a $workoutType workout for $durationMinutes minutes")
            distanceKm?.let { append(", covering ${"%.1f".format(it)} km") }
            append(". Give me a short, encouraging recovery or improvement tip (2-3 sentences) for this type of workout.")
        }
        return callGemini(apiKey, prompt) ?: workoutFallback(workoutType)
    }

    private suspend fun callGemini(apiKey: String, prompt: String): String? {
        if (apiKey.isBlank()) return null
        return runCatching {
            withContext(Dispatchers.IO) {
                val body =
                    """{"contents":[{"parts":[{"text":${JSONObject.quote(prompt)}}]}],"generationConfig":{"maxOutputTokens":150,"temperature":0.7}}"""
                val request = Request.Builder()
                    .url("$GEMINI_ENDPOINT?key=$apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val json = JSONObject(checkNotNull(response.body).string())
                    json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()
                }
            }
        }.getOrNull()
    }

    private fun nutritionFallback(cals: Float, target: Float): String {
        if (target <= 0f) {
            return "Track your meals consistently to understand your eating patterns. Small, sustainable improvements add up over time!"
        }
        val ratio = cals / target
        return when {
            ratio < 0.5f -> "You're well under your calorie target — aim for a balanced, protein-rich meal to fuel your body and prevent muscle loss."
            ratio < 0.9f -> "Good progress on nutrition today! Add a protein source and some vegetables to your next meal to round out your macros."
            ratio in 0.9f..1.1f -> "Right on track with your calorie goal! Focus on hitting your protein target to support muscle recovery."
            else -> "You've exceeded your calorie target today. Balance it tomorrow with lighter meals and stay well-hydrated."
        }
    }

    private fun workoutFallback(type: String): String = when {
        type.contains("Run", ignoreCase = true) ->
            "Great run! Prioritise protein within 30 minutes to support muscle repair, and don't skip your cool-down stretches."
        type.contains("Swim", ignoreCase = true) ->
            "Excellent swim! Hydrate well — swimmers often underestimate fluid loss. A carb-and-protein snack works great for recovery."
        type.contains("Strength", ignoreCase = true) ->
            "Strong session! Aim for 7-9 hours of sleep tonight — muscle growth happens during rest. Log your weights to track progressive overload."
        type.contains("Badminton", ignoreCase = true) || type.contains("Table Tennis", ignoreCase = true) ->
            "Fantastic match! Racquet sports are excellent for agility. Stay consistent and focus on footwork drills to keep levelling up."
        else ->
            "Great workout! Rest and recovery are just as important as the session itself. Prioritise sleep and proper nutrition to make the most of your effort."
    }
}
