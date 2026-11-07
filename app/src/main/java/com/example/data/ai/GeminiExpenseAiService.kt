package com.example.data.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.Locale

private const val TAG = "GeminiExpenseAiService"

data class ParsedExpenseDraft(
    val merchant: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val category: String,
    val note: String,
    val tags: List<String>,
    val taxAmount: Double = 0.0,
    val lineItemsSummary: String = "",
    val isSubscription: Boolean = false,
    val sentimentNote: String = "",
    val aiPowered: Boolean = true,
    val statusNotice: String = "Extracted via Gemini 3.8 Flash"
)

class GeminiExpenseAiService {

    private val expenseSchema = Schema.obj(
        properties = mapOf(
            "merchant" to Schema.string(description = "Name of the business, restaurant, or vendor"),
            "amount" to Schema.double(description = "Total monetary value of the expense"),
            "currency" to Schema.string(description = "ISO currency code, e.g., INR, USD, EUR"),
            "date" to Schema.string(description = "Transaction date in YYYY-MM-DD format"),
            "category" to Schema.string(description = "Mapped category name from user's taxonomy or suggested new custom category"),
            "note" to Schema.string(description = "Concise human-readable description or purpose of the spend"),
            "tags" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 lowercase descriptive tags"
            ),
            "taxAmount" to Schema.double(description = "Extracted GST/VAT/Sales tax amount if shown, else 0"),
            "lineItemsSummary" to Schema.string(description = "Comma-separated summary of items on the receipt or order"),
            "isSubscription" to Schema.boolean(description = "True if this represents a recurring bill or subscription"),
            "sentimentNote" to Schema.string(description = "Short financial observation, e.g. 'Weekend dining treat' or 'Recurring utility bill'")
        ),
        optionalProperties = listOf(
            "merchant",
            "currency",
            "date",
            "note",
            "tags",
            "taxAmount",
            "lineItemsSummary",
            "isSubscription",
            "sentimentNote"
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = expenseSchema
                temperature = 0.2f
            }
        )
    }

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.4f
            }
        )
    }

    suspend fun parseExpenseFromText(
        rawInput: String,
        currentDate: String,
        existingCategories: List<String>,
        defaultCurrency: String
    ): Result<ParsedExpenseDraft> {
        val categoriesStr = existingCategories.joinToString(", ")
        val prompt = """
            You are Lumina Spend's financial extraction engine.
            Parse the user's natural language expense note or voice transcript into structured JSON.
            
            User Input: "$rawInput"
            Today's Date: $currentDate
            Default Currency: $defaultCurrency
            User's Existing Categories: [$categoriesStr]
            
            Rules:
            1. Match "category" to the closest existing category from [$categoriesStr], or suggest a concise new category name only if none fits.
            2. Extract the numerical "amount" accurately.
            3. Infer "merchant" (e.g. "Swiggy", "Indian Oil", "Paradise Biryani", "Uber").
            4. Set "isSubscription" to true for monthly plans, broadband, OTT streaming, rent, or recurring bills.
            5. Provide a helpful 1-sentence "sentimentNote" about this expense.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Gemini returned an empty response.")
            val draft = parseJsonToDraft(
                jsonText = jsonText,
                fallbackDate = currentDate,
                defaultCurrency = defaultCurrency,
                rawInput = rawInput
            )
            Result.success(draft)
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Gemini text extraction timed out", e)
                val fallback = localSmartParse(rawInput, currentDate, existingCategories, defaultCurrency)
                Result.success(
                    fallback.copy(
                        aiPowered = false,
                        statusNotice = "AI timed out after 60s — parsed using local smart fallback"
                    )
                )
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini text extraction failed, using local smart fallback", e)
            val fallback = localSmartParse(rawInput, currentDate, existingCategories, defaultCurrency)
            Result.success(
                fallback.copy(
                    aiPowered = false,
                    statusNotice = "Local smart parser used (${e.localizedMessage?.take(60) ?: "Offline"})"
                )
            )
        }
    }

    suspend fun analyzeReceiptBitmap(
        bitmap: Bitmap,
        currentDate: String,
        existingCategories: List<String>,
        defaultCurrency: String
    ): Result<ParsedExpenseDraft> {
        val categoriesStr = existingCategories.joinToString(", ")
        val scaledBitmap = scaleBitmapDown(bitmap, maxDimension = 1280)
        val prompt = """
            Analyze this receipt image and extract the expense details into structured JSON.
            Today's Date (use if receipt has no date): $currentDate
            Default Currency: $defaultCurrency
            User's Existing Categories: [$categoriesStr]
            
            Extract:
            - merchant: Store, restaurant, or vendor name
            - amount: Grand total paid
            - taxAmount: Total GST/CGST+SGST/VAT/Tax amount if visible, else 0
            - lineItemsSummary: Concise comma-separated list of purchased items
            - date: YYYY-MM-DD format
            - category: Best matching category from [$categoriesStr] or sensible custom category
            - tags: Relevant lowercase tags
            - sentimentNote: Brief insight on tax split or purchase type
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty OCR response from Gemini.")
            val draft = parseJsonToDraft(
                jsonText = jsonText,
                fallbackDate = currentDate,
                defaultCurrency = defaultCurrency,
                rawInput = "Receipt Scan"
            )
            Result.success(draft.copy(statusNotice = "Extracted via Gemini Vision OCR"))
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Receipt OCR timed out", e)
                Result.failure(IllegalStateException("Receipt OCR timed out after 60 seconds. Please try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Receipt OCR failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateSpendingInsights(
        monthDisplay: String,
        totalSpentFormatted: String,
        totalBudgetFormatted: String,
        categoryBreakdownText: String,
        topMerchantsText: String
    ): Result<String> {
        val prompt = """
            You are Lumina Spend's AI Financial Coach.
            Analyze the user's spending for $monthDisplay:
            - Total Spent: $totalSpentFormatted / Budget: $totalBudgetFormatted
            - Category Breakdown: $categoryBreakdownText
            - Top Merchants: $topMerchantsText
            
            Write 3 concise, high-impact bullet points (max 65 words total):
            1. Spend Velocity & Runway observation
            2. Category optimization opportunity (with specific numbers)
            3. One actionable savings challenge for the next 7 days
            Use plain text bullets (•) without markdown headers.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                coachModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Model returned empty coaching response."))
            }
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "AI Coaching timed out", e)
                Result.failure(IllegalStateException("AI Coaching timed out after 60s."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI Coaching generation failed", e)
            Result.failure(e)
        }
    }

    private fun parseJsonToDraft(
        jsonText: String,
        fallbackDate: String,
        defaultCurrency: String,
        rawInput: String
    ): ParsedExpenseDraft {
        val json = JSONObject(jsonText)
        val merchant = json.optString("merchant").ifBlank { "Quick Expense" }
        val amount = json.optDouble("amount", 0.0).let { if (it.isNaN() || it < 0) 0.0 else it }
        val currency = json.optString("currency").ifBlank { defaultCurrency }.uppercase(Locale.US)
        val rawDate = json.optString("date").trim()
        val validDate = if (rawDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) rawDate else fallbackDate
        val category = json.optString("category").ifBlank { "Food & Dining" }
        val note = json.optString("note").ifBlank { rawInput }
        val taxAmount = json.optDouble("taxAmount", 0.0).let { if (it.isNaN() || it < 0) 0.0 else it }
        val lineItems = json.optString("lineItemsSummary", "")
        val isSub = json.optBoolean("isSubscription", false)
        val sentiment = json.optString("sentimentNote", "Auto-categorized by Gemini AI")

        val tags = mutableListOf<String>()
        val tagsArr = json.optJSONArray("tags")
        if (tagsArr != null) {
            for (i in 0 until tagsArr.length()) {
                val tag = tagsArr.optString(i).trim().lowercase(Locale.US)
                if (tag.isNotBlank()) tags.add(tag)
            }
        }

        return ParsedExpenseDraft(
            merchant = merchant,
            amount = amount,
            currency = currency,
            date = validDate,
            category = category,
            note = note,
            tags = tags.distinct(),
            taxAmount = taxAmount,
            lineItemsSummary = lineItems,
            isSubscription = isSub,
            sentimentNote = sentiment,
            aiPowered = true
        )
    }

    fun localSmartParse(
        rawInput: String,
        currentDate: String,
        existingCategories: List<String>,
        defaultCurrency: String
    ): ParsedExpenseDraft {
        val text = rawInput.trim()
        val lower = text.lowercase(Locale.US)

        // Extract numeric amount
        val numberRegex = Regex("(\\d+(?:,\\d{2,3})*(?:\\.\\d{1,2})?)")
        val amountMatch = numberRegex.findAll(text)
            .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
            .firstOrNull { it > 0.0 } ?: 0.0

        // Extract merchant heuristic ("at X", "from X", "to X", or leading words)
        val atMatch = Regex("(?:at|from|on|to)\\s+([A-Za-z0-9 &']+?)(?:\\s+for|\\s+\\d|$)", RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.getOrNull(1)?.trim()

        val merchant = when {
            !atMatch.isNullOrBlank() && atMatch.length >= 2 -> {
                atMatch.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                }
            }
            lower.contains("swiggy") -> "Swiggy"
            lower.contains("zomato") -> "Zomato"
            lower.contains("paradise") -> "Paradise Biryani"
            lower.contains("indian oil") || lower.contains("petrol") || lower.contains("fuel") -> "Indian Oil"
            lower.contains("uber") -> "Uber"
            lower.contains("ola") -> "Ola Cabs"
            lower.contains("bigbasket") -> "BigBasket"
            lower.contains("blinkit") -> "Blinkit"
            lower.contains("netflix") -> "Netflix"
            lower.contains("airtel") -> "Airtel"
            lower.contains("apollo") -> "Apollo Pharmacy"
            lower.contains("amazon") -> "Amazon"
            else -> {
                val cleanedWords = text.replace(numberRegex, "")
                    .replace(Regex("\\b(spent|rupees|rs|inr|usd|dollars|for|on|at|with|order|paid)\\b", RegexOption.IGNORE_CASE), "")
                    .trim()
                cleanedWords.split(Regex("\\s+")).take(3).joinToString(" ")
                    .ifBlank { "Quick Expense" }
            }
        }

        val inferredCategory = when {
            lower.containsAny("biryani", "dinner", "lunch", "breakfast", "coffee", "cafe", "swiggy", "zomato", "pizza", "food", "restaurant") -> "Food & Dining"
            lower.containsAny("fuel", "petrol", "diesel", "indian oil", "uber", "ola", "metro", "cab", "flight", "transit", "train") -> "Transit"
            lower.containsAny("grocery", "groceries", "bigbasket", "blinkit", "milk", "vegetables", "supermarket", "mart") -> "Groceries"
            lower.containsAny("netflix", "spotify", "airtel", "jio", "wifi", "electricity", "bill", "subscription", "recharge") -> "Subscriptions & Utilities"
            lower.containsAny("pharmacy", "apollo", "doctor", "medicine", "hospital", "health", "gym", "clinic") -> "Healthcare"
            lower.containsAny("amazon", "flipkart", "croma", "myntra", "shoes", "shirt", "electronics", "shopping") -> "Shopping"
            else -> existingCategories.firstOrNull() ?: "Food & Dining"
        }

        val isSubscription = lower.containsAny("subscription", "netflix", "spotify", "monthly", "broadband", "postpaid", "fiber")
        val tags = mutableListOf<String>()
        if (inferredCategory.contains("Food")) tags.add("dining")
        if (inferredCategory.contains("Transit")) tags.add("commute")
        if (isSubscription) tags.add("recurring")
        tags.add("quick-add")

        return ParsedExpenseDraft(
            merchant = merchant,
            amount = amountMatch,
            currency = defaultCurrency,
            date = currentDate,
            category = inferredCategory,
            note = text,
            tags = tags.distinct(),
            isSubscription = isSubscription,
            sentimentNote = if (isSubscription) "Recurring charge flagged" else "Mapped to $inferredCategory",
            aiPowered = true
        )
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = width.toFloat() / height.toFloat()
        val (targetW, targetH) = if (width > height) {
            maxDimension to (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            (maxDimension * ratio).toInt().coerceAtLeast(1) to maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    companion object {
        /**
         * Renders a crisp, realistic paper tax receipt Bitmap on-device so users can
         * test Multimodal Gemini Vision Receipt OCR immediately in the emulator or device.
         */
        fun createSampleReceiptBitmap(presetIndex: Int = 0): Bitmap {
            val width = 720
            val height = 960
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAF8F5"))

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#111827")
                textSize = 34f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#374151")
                textSize = 22f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#111827")
                textSize = 24f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#000000")
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }

            if (presetIndex % 2 == 0) {
                canvas.drawText("THIRD WAVE COFFEE ROASTERS", width / 2f, 90f, titlePaint)
                canvas.drawText("Indiranagar 100ft Rd, Bengaluru", width / 2f, 130f, subPaint)
                canvas.drawText("GSTIN: 29AABCT1234F1Z5", width / 2f, 165f, subPaint)
                canvas.drawText("Date: 2026-11-07   Bill #TW-8492", width / 2f, 205f, subPaint)
                canvas.drawText("------------------------------------", width / 2f, 245f, subPaint)

                val items = listOf(
                    "1x Sea Salt Caramel Latte" to "310.00",
                    "1x Cold Brew Tonic" to "280.00",
                    "1x Smoked Croissant Sandwich" to "390.00",
                    "1x Basque Burnt Cheesecake" to "320.00"
                )
                var y = 300f
                items.forEach { (name, price) ->
                    canvas.drawText(name, 55f, y, itemPaint)
                    canvas.drawText(price, 540f, y, itemPaint)
                    y += 50f
                }

                canvas.drawText("------------------------------------", width / 2f, y + 10f, subPaint)
                y += 60f
                canvas.drawText("Subtotal", 55f, y, itemPaint)
                canvas.drawText("1,300.00", 520f, y, itemPaint)
                y += 45f
                canvas.drawText("CGST (2.5%) + SGST (2.5%)", 55f, y, itemPaint)
                canvas.drawText("65.00", 545f, y, itemPaint)
                y += 65f
                canvas.drawText("GRAND TOTAL (INR)", 55f, y, boldPaint)
                canvas.drawText("Rs. 1,365.00", 460f, y, boldPaint)
                y += 70f
                canvas.drawText("Paid via UPI • Thank You!", width / 2f, y, subPaint)
            } else {
                canvas.drawText("NATURE'S BASKET GOURMET", width / 2f, 90f, titlePaint)
                canvas.drawText("Banjara Hills Rd No. 12, Hyderabad", width / 2f, 130f, subPaint)
                canvas.drawText("Date: 2026-11-07   Inv #NB-3091", width / 2f, 175f, subPaint)
                canvas.drawText("------------------------------------", width / 2f, 220f, subPaint)

                val items = listOf(
                    "1x Hass Avocados Pack (4pc)" to "540.00",
                    "1x Sourdough Artisan Loaf" to "260.00",
                    "2x Greek Yogurt Berry Tub" to "380.00",
                    "1x Cold Pressed Olive Oil 1L" to "1,120.00"
                )
                var y = 275f
                items.forEach { (name, price) ->
                    canvas.drawText(name, 55f, y, itemPaint)
                    canvas.drawText(price, 520f, y, itemPaint)
                    y += 50f
                }

                canvas.drawText("------------------------------------", width / 2f, y + 10f, subPaint)
                y += 60f
                canvas.drawText("Taxable Subtotal", 55f, y, itemPaint)
                canvas.drawText("2,300.00", 520f, y, itemPaint)
                y += 45f
                canvas.drawText("GST Total (5%)", 55f, y, itemPaint)
                canvas.drawText("115.00", 540f, y, itemPaint)
                y += 65f
                canvas.drawText("TOTAL AMOUNT PAID", 55f, y, boldPaint)
                canvas.drawText("Rs. 2,415.00", 460f, y, boldPaint)
            }
            return bmp
        }
    }
}
