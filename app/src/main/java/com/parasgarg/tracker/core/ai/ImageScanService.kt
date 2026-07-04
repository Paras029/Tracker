package com.parasgarg.tracker.core.ai

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val GEMINI_VISION_ENDPOINT =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

private const val BCA_PROMPT = """You are a precision health data extraction assistant. \
The attached image shows a Body Composition Analysis (BCA) report printed by a medical \
device — typical brands include InBody, Tanita, Omron, Evolt, or Dexa.

Your task: extract the numerical measurements and return them as a single JSON object. \
Follow these rules exactly:
1. Return ONLY the JSON — no markdown, no explanation, no surrounding text.
2. Use null (not a string) for any measurement that is not legible or not present.
3. All numerical values must be plain numbers (not strings).
4. Body fat and water percentage should be a number like 18.5, not "18.5%".
5. If the report shows imperial units, convert to metric.

Required JSON schema (no extra keys):
{"bodyFatPercent":<number|null>,"skeletalMuscleMassKg":<number|null>,"visceralFatLevel":<integer 1-20|null>,"waterPercent":<number|null>,"boneMassKg":<number|null>,"bmi":<number|null>}"""

private const val BLOOD_TEST_PROMPT = """You are a medical data extraction assistant. \
The attached image shows a blood test or pathology lab report.

Extract the listed biomarkers and return them as a single JSON object. Rules:
1. Return ONLY the JSON — no markdown, no explanation, no surrounding text.
2. Use null for any biomarker that is absent or not legible.
3. All numerical values must be plain numbers.
4. Apply unit conversions where needed:
   - Hemoglobin in mmol/L → multiply by 1.6113 to get g/dL
   - Glucose/FBS in mmol/L → multiply by 18.016 to get mg/dL
   - LDL/HDL in mmol/L → multiply by 38.67 to get mg/dL
   - Vitamin D3 in nmol/L → divide by 2.496 to get ng/mL
   - Vitamin B12 in pmol/L → multiply by 1.355 to get pg/mL
5. For testedDateIso: use ISO format YYYY-MM-DD. Use null if the date is not visible.

Required JSON schema (no extra keys):
{"hemoglobinGdL":<number|null>,"vitaminD3NgmL":<number|null>,"vitaminB12PgmL":<number|null>,"ldlMgdL":<number|null>,"hdlMgdL":<number|null>,"fastingGlucoseMgdL":<number|null>,"hba1cPercent":<number|null>,"testedDateIso":<"YYYY-MM-DD"|null>}"""

private const val FOOD_LABEL_PROMPT = """You are a nutrition data extraction assistant. \
The attached image shows a food product's nutrition facts panel, nutrition label, \
or ingredient list.

Extract the nutrition information PER SERVING and return as a single JSON object. Rules:
1. Return ONLY the JSON — no markdown, no explanation, no surrounding text.
2. All numerical values must be plain numbers.
3. Prefer per-serving values over per-100g values when both are shown.
4. If only per-100g values are given (no stated serving size), set servingGrams to 100 \
and use those values.
5. Energy in kJ → divide by 4.184 to convert to kcal.
6. For foodName: use the product name from the label, or a short descriptive name \
(e.g. "Whole Wheat Bread", "Greek Yogurt") if the brand name is not informative.

Required JSON schema (no extra keys):
{"foodName":<string>,"servingGrams":<number>,"caloriesKcal":<number|null>,"proteinG":<number|null>,"carbsG":<number|null>,"fatG":<number|null>}"""

@Singleton
class ImageScanService @Inject constructor(private val httpClient: OkHttpClient) {

    suspend fun scanBcaReport(bitmap: Bitmap, apiKey: String): BcaScanResult? {
        val json = callGeminiVision(bitmap, apiKey, BCA_PROMPT) ?: return null
        return runCatching {
            val obj = JSONObject(json)
            BcaScanResult(
                bodyFatPercent = obj.optDoubleOrNull("bodyFatPercent"),
                skeletalMuscleMassKg = obj.optDoubleOrNull("skeletalMuscleMassKg"),
                visceralFatLevel = obj.optIntOrNull("visceralFatLevel"),
                waterPercent = obj.optDoubleOrNull("waterPercent"),
                boneMassKg = obj.optDoubleOrNull("boneMassKg"),
                bmi = obj.optDoubleOrNull("bmi"),
            )
        }.getOrNull()
    }

    suspend fun scanBloodTestReport(bitmap: Bitmap, apiKey: String): BloodTestScanResult? {
        val json = callGeminiVision(bitmap, apiKey, BLOOD_TEST_PROMPT) ?: return null
        return runCatching {
            val obj = JSONObject(json)
            val dateStr = obj.optString("testedDateIso").takeIf { it.isNotBlank() && it != "null" }
            BloodTestScanResult(
                hemoglobinGdL = obj.optDoubleOrNull("hemoglobinGdL"),
                vitaminD3NgmL = obj.optDoubleOrNull("vitaminD3NgmL"),
                vitaminB12PgmL = obj.optDoubleOrNull("vitaminB12PgmL"),
                ldlMgdL = obj.optDoubleOrNull("ldlMgdL"),
                hdlMgdL = obj.optDoubleOrNull("hdlMgdL"),
                fastingGlucoseMgdL = obj.optDoubleOrNull("fastingGlucoseMgdL"),
                hba1cPercent = obj.optDoubleOrNull("hba1cPercent"),
                testedDate = dateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            )
        }.getOrNull()
    }

    suspend fun scanFoodLabel(bitmap: Bitmap, apiKey: String): FoodLabelScanResult? {
        val json = callGeminiVision(bitmap, apiKey, FOOD_LABEL_PROMPT) ?: return null
        return runCatching {
            val obj = JSONObject(json)
            FoodLabelScanResult(
                foodName = obj.optString("foodName").takeIf { it.isNotBlank() && it != "null" },
                servingGrams = obj.optDoubleOrNull("servingGrams"),
                caloriesKcal = obj.optDoubleOrNull("caloriesKcal"),
                proteinG = obj.optDoubleOrNull("proteinG"),
                carbsG = obj.optDoubleOrNull("carbsG"),
                fatG = obj.optDoubleOrNull("fatG"),
            )
        }.getOrNull()
    }

    private suspend fun callGeminiVision(bitmap: Bitmap, apiKey: String, prompt: String): String? {
        if (apiKey.isBlank()) return null
        return runCatching {
            withContext(Dispatchers.IO) {
                val base64 = bitmap.toBase64Jpeg()
                val body = buildString {
                    append("""{"contents":[{"parts":[""")
                    append("""{"inline_data":{"mime_type":"image/jpeg","data":""")
                    append(JSONObject.quote(base64))
                    append("""}},""")
                    append("""{"text":""")
                    append(JSONObject.quote(prompt))
                    append("""]}],"generationConfig":{"responseMimeType":"application/json","maxOutputTokens":512,"temperature":0.1}}""")
                }
                val request = Request.Builder()
                    .url("$GEMINI_VISION_ENDPOINT?key=$apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val responseJson = JSONObject(checkNotNull(response.body).string())
                    responseJson.getJSONArray("candidates")
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

    private fun Bitmap.toBase64Jpeg(maxDimension: Int = 1280): String {
        val scaled = if (width > maxDimension || height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(width, height)
            Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        } else this
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun JSONObject.optDoubleOrNull(key: String): Double? {
        if (isNull(key)) return null
        val v = optDouble(key)
        return if (v.isNaN()) null else v
    }

    private fun JSONObject.optIntOrNull(key: String): Int? {
        if (isNull(key)) return null
        val v = optInt(key, Int.MIN_VALUE)
        return if (v == Int.MIN_VALUE) null else v
    }
}
