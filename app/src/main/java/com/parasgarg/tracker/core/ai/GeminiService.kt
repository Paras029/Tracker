package com.parasgarg.tracker.core.ai

import java.time.Instant
import java.time.ZoneId
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
        carbsToday: Float,
        carbsTarget: Float,
        fatToday: Float,
        fatTarget: Float,
        fitnessGoal: String?,
        mealCount: Int,
    ): String {
        val calPct = if (calTarget > 0f) ((calsToday / calTarget) * 100).toInt() else null
        val prompt = buildString {
            appendLine("You are a knowledgeable, direct nutrition coach reviewing your client's daily food log.")
            appendLine()
            appendLine("Client context:")
            append("- Calories: ${calsToday.toInt()} kcal consumed")
            if (calTarget > 0f) append(" of ${calTarget.toInt()} kcal target (${calPct}% of goal)")
            appendLine()
            append("- Protein: ${proteinToday.toInt()}g")
            if (proteinTarget > 0f) append(" of ${proteinTarget.toInt()}g target")
            appendLine()
            append("- Carbohydrates: ${carbsToday.toInt()}g")
            if (carbsTarget > 0f) append(" of ${carbsTarget.toInt()}g target")
            appendLine()
            append("- Fat: ${fatToday.toInt()}g")
            if (fatTarget > 0f) append(" of ${fatTarget.toInt()}g target")
            appendLine()
            appendLine("- Food items logged today: $mealCount")
            appendLine("- Fitness goal: ${fitnessGoal ?: "general health"}")
            appendLine()
            appendLine("Write a coaching message of exactly 2–3 sentences. Requirements:")
            appendLine("- Lead with the most important observation from the numbers above (e.g. a macro significantly off target, or a calorie surplus/deficit that matters for the stated goal).")
            appendLine("- Give one concrete action the person can take in the next meal or hour.")
            append("- Be direct and specific — reference the actual numbers. Do not open with \"Great job,\" \"Well done,\" or similar generic praise.")
        }
        return callGemini(apiKey, prompt, maxTokens = 150) ?: nutritionFallback(calsToday, calTarget, proteinToday, proteinTarget)
    }

    suspend fun getWorkoutTip(
        apiKey: String,
        workoutType: String,
        durationMinutes: Int,
        distanceKm: Double?,
        perceivedEffort: Int?,
        fitnessGoal: String?,
        startTime: Instant,
    ): String {
        val timeOfDay = when (startTime.atZone(ZoneId.systemDefault()).hour) {
            in 5..11 -> "morning"
            in 12..16 -> "afternoon"
            else -> "evening"
        }
        val prompt = buildString {
            appendLine("You are an experienced fitness coach reviewing a training session just logged by your client.")
            appendLine()
            appendLine("Session details:")
            appendLine("- Workout type: $workoutType")
            appendLine("- Duration: $durationMinutes minutes")
            distanceKm?.let { appendLine("- Distance: ${"%.2f".format(it)} km") }
            appendLine("- Perceived effort: ${perceivedEffort?.let { "$it/10" } ?: "not rated"}")
            appendLine("- Time of day: $timeOfDay")
            appendLine("- Fitness goal: ${fitnessGoal ?: "general fitness"}")
            appendLine()
            appendLine("Write a coaching message of exactly 2–3 sentences. Requirements:")
            appendLine("- Give one specific recovery recommendation tailored to this workout type and effort level (e.g. for high-effort runs: protein timing and sleep; for strength: progressive overload or rest).")
            appendLine("- Include one actionable improvement tip for the next session of this type.")
            append("- Be direct and specific. Do not open with \"Great job,\" \"Well done,\" or similar generic praise.")
        }
        return callGemini(apiKey, prompt, maxTokens = 150) ?: workoutFallback(workoutType, perceivedEffort)
    }

    private suspend fun callGemini(apiKey: String, prompt: String, maxTokens: Int): String? {
        if (apiKey.isBlank()) return null
        return runCatching {
            withContext(Dispatchers.IO) {
                val body =
                    """{"contents":[{"parts":[{"text":${JSONObject.quote(prompt)}}]}],"generationConfig":{"maxOutputTokens":$maxTokens,"temperature":0.7}}"""
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

    // ── Offline fallbacks ──────────────────────────────────────────────────────

    private fun nutritionFallback(
        cals: Float,
        calTarget: Float,
        protein: Float,
        proteinTarget: Float,
    ): String {
        if (calTarget <= 0f) {
            return "Track your meals consistently — even rough logging is more useful than none. Start with protein at each meal and build from there."
        }
        val calRatio = cals / calTarget
        val proteinDeficit = (proteinTarget - protein).coerceAtLeast(0f)
        return when {
            calRatio < 0.4f ->
                "You're at ${(calRatio * 100).toInt()}% of your calorie target — eating too little can slow your metabolism and cause muscle loss. Prioritise a protein-rich meal now; aim for at least ${proteinTarget.toInt()}g protein today."
            calRatio < 0.85f && proteinDeficit > 30f ->
                "Calories are on track but protein is ${proteinDeficit.toInt()}g short of your ${proteinTarget.toInt()}g target. Add a high-protein source (eggs, chicken, legumes, Greek yogurt) to your next meal."
            calRatio in 0.85f..1.15f ->
                "You're well within your calorie target — good discipline. Make sure protein is close to ${proteinTarget.toInt()}g before end of day to support recovery and satiety."
            else ->
                "You've exceeded your calorie target by ${((calRatio - 1f) * calTarget).toInt()} kcal. Stay hydrated and keep tomorrow's first meal light and protein-forward to recalibrate."
        }
    }

    private fun workoutFallback(type: String, effort: Int?): String {
        val effortNote = when {
            effort != null && effort >= 8 -> "You pushed hard today"
            effort != null && effort <= 4 -> "That was a lighter session"
            else -> null
        }
        return when {
            type.contains("Run", ignoreCase = true) -> buildString {
                if (effortNote != null) append("$effortNote — ")
                append("consume 20–30g protein within 30 minutes to support muscle repair, and prioritise 7–9 hours of sleep tonight for full recovery. ")
                append("For your next run, try adding a 10-minute easy cool-down jog to clear lactate and reduce next-day soreness.")
            }
            type.contains("Swim", ignoreCase = true) -> buildString {
                if (effortNote != null) append("$effortNote — ")
                append("swimmers often underestimate fluid loss; drink 500ml of water now and eat a carb-and-protein snack within the hour. ")
                append("Next session, focus on one technical element (e.g. catch or turn) rather than just yardage — it compounds faster.")
            }
            type.contains("Strength", ignoreCase = true) -> buildString {
                if (effortNote != null) append("$effortNote — ")
                append("aim for 1.6–2.2g of protein per kg of bodyweight today to maximise muscle protein synthesis. ")
                append("Next session, add 2.5–5% weight to the bar on your primary lifts — progressive overload is what drives adaptation.")
            }
            type.contains("Badminton", ignoreCase = true) || type.contains("Table Tennis", ignoreCase = true) -> buildString {
                if (effortNote != null) append("$effortNote — ")
                append("racquet sports burn more calories than they feel like; refuel with carbs and electrolytes within 45 minutes. ")
                append("Dedicated footwork drills for 10 minutes before your next match will have a bigger impact on your game than longer rallies alone.")
            }
            else -> buildString {
                if (effortNote != null) append("$effortNote — ")
                append("sleep is when adaptation happens; make tonight's 7–9 hours count as much as the session itself. ")
                append("Log your key metrics now while they're fresh — consistency in tracking is what reveals patterns over weeks.")
            }
        }
    }
}
