package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiExpenseAiService
import com.example.data.ai.ParsedExpenseDraft
import com.example.data.local.CategoryEntity
import com.example.data.local.DismissedAlertEntity
import com.example.data.local.LuminaDatabase
import com.example.data.local.LuminaRepository
import com.example.data.local.MerchantRuleEntity
import com.example.data.local.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.BudgetAlert
import com.example.domain.BudgetAlertEngine
import com.example.domain.CategorySpendSummary
import com.example.domain.MonthlyBudgetMetrics
import com.example.domain.WeeklyBarPoint
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class LuminaTab {
    HUB,
    ANALYTICS,
    CATEGORIES,
    HISTORY
}

sealed interface AiExtractionState {
    data object Idle : AiExtractionState
    data class Loading(val label: String) : AiExtractionState
    data class Ready(val draft: ParsedExpenseDraft, val sourceType: String) : AiExtractionState
    data class Error(val message: String) : AiExtractionState
}

sealed interface AiCoachingState {
    data object Idle : AiCoachingState
    data object Loading : AiCoachingState
    data class Success(val insightText: String) : AiCoachingState
    data class Error(val message: String) : AiCoachingState
}

data class LuminaUiState(
    val isLoading: Boolean = true,
    val selectedYearMonth: String = DateUtils.currentYearMonth(),
    val selectedMonthDisplay: String = DateUtils.formatYearMonthDisplay(DateUtils.currentYearMonth()),
    val currencyCode: String = "INR",
    val monthlyBudgetOverride: Double = 0.0,
    val autoLearnMerchants: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    val currentMonthTransactions: List<TransactionEntity> = emptyList(),
    val prevMonthTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val categoryBreakdown: List<CategorySpendSummary> = emptyList(),
    val metrics: MonthlyBudgetMetrics = MonthlyBudgetMetrics(
        yearMonth = DateUtils.currentYearMonth(),
        totalSpent = 0.0,
        totalBudget = 60000.0,
        remainingBudget = 60000.0,
        percentSpent = 0,
        progressFraction = 0f,
        dailyBurnRate = 0.0,
        allowedDailyPace = 2000.0,
        projectedMonthEndSpend = 0.0,
        isPacedUnderBudget = true,
        velocityHeadline = "Paced to finish under budget",
        velocitySubtext = "",
        subscriptionTotal = 0.0,
        subscriptionCount = 0
    ),
    val activeAlerts: List<BudgetAlert> = emptyList(),
    val weeklyComparison: List<WeeklyBarPoint> = emptyList(),
    val merchantRules: List<MerchantRuleEntity> = emptyList(),
    val selectedCategoryFilterId: Long? = null,
    val historySearchQuery: String = "",
    val historySubscriptionsOnly: Boolean = false
)

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LuminaDatabase.getInstance(application)
    private val repository = LuminaRepository(database.luminaDao())
    private val prefsRepository = UserPreferencesRepository(application)
    private val aiService = GeminiExpenseAiService()

    private val _selectedYearMonth = MutableStateFlow(DateUtils.currentYearMonth())
    private val _selectedCategoryFilterId = MutableStateFlow<Long?>(null)
    private val _historySearchQuery = MutableStateFlow("")
    private val _historySubscriptionsOnly = MutableStateFlow(false)

    private val _aiExtractionState = MutableStateFlow<AiExtractionState>(AiExtractionState.Idle)
    val aiExtractionState: StateFlow<AiExtractionState> = _aiExtractionState.asStateFlow()

    private val _aiCoachingState = MutableStateFlow<AiCoachingState>(AiCoachingState.Idle)
    val aiCoachingState: StateFlow<AiCoachingState> = _aiCoachingState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    private data class CoreDataBundle(
        val categories: List<CategoryEntity>,
        val transactions: List<TransactionEntity>,
        val rules: List<MerchantRuleEntity>,
        val dismissed: List<DismissedAlertEntity>,
        val prefs: UserPreferences
    )

    private val coreDataFlow = combine(
        repository.categoriesFlow,
        repository.allTransactionsFlow,
        repository.merchantRulesFlow,
        repository.dismissedAlertsFlow,
        prefsRepository.userPreferencesFlow
    ) { categories, transactions, rules, dismissed, prefs ->
        CoreDataBundle(categories, transactions, rules, dismissed, prefs)
    }

    val uiState: StateFlow<LuminaUiState> = combine(
        coreDataFlow,
        _selectedYearMonth,
        _selectedCategoryFilterId,
        _historySearchQuery,
        _historySubscriptionsOnly
    ) { core, ym, catFilterId, searchQuery, subsOnly ->
        val prevYm = DateUtils.shiftYearMonth(ym, -1)
        val currentMonthTxs = core.transactions.filter { it.yearMonth == ym }
        val prevMonthTxs = core.transactions.filter { it.yearMonth == prevYm }

        val metrics = BudgetAlertEngine.computeMetrics(
            yearMonth = ym,
            transactions = currentMonthTxs,
            categories = core.categories,
            monthlyBudgetOverride = core.prefs.monthlyBudgetOverride,
            currencyCode = core.prefs.currencyCode
        )

        val breakdown = BudgetAlertEngine.computeCategoryBreakdown(
            transactions = currentMonthTxs,
            categories = core.categories
        )

        val dismissedKeys = core.dismissed.map { it.alertKey }.toSet()
        val alerts = BudgetAlertEngine.generateAlerts(
            yearMonth = ym,
            transactions = currentMonthTxs,
            prevMonthTransactions = prevMonthTxs,
            categorySummaries = breakdown,
            metrics = metrics,
            dismissedKeys = dismissedKeys,
            currencyCode = core.prefs.currencyCode
        )

        val weeklyBars = BudgetAlertEngine.computeWeeklyComparison(
            currentMonthTxs = currentMonthTxs,
            prevMonthTxs = prevMonthTxs
        )

        LuminaUiState(
            isLoading = false,
            selectedYearMonth = ym,
            selectedMonthDisplay = DateUtils.formatYearMonthDisplay(ym),
            currencyCode = core.prefs.currencyCode,
            monthlyBudgetOverride = core.prefs.monthlyBudgetOverride,
            autoLearnMerchants = core.prefs.autoLearnMerchants,
            categories = core.categories,
            currentMonthTransactions = currentMonthTxs,
            prevMonthTransactions = prevMonthTxs,
            allTransactions = core.transactions,
            categoryBreakdown = breakdown,
            metrics = metrics,
            activeAlerts = alerts,
            weeklyComparison = weeklyBars,
            merchantRules = core.rules,
            selectedCategoryFilterId = catFilterId,
            historySearchQuery = searchQuery,
            historySubscriptionsOnly = subsOnly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LuminaUiState()
    )

    fun shiftMonth(delta: Int) {
        _selectedYearMonth.value = DateUtils.shiftYearMonth(_selectedYearMonth.value, delta)
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilterId.value = if (_selectedCategoryFilterId.value == categoryId) {
            null
        } else {
            categoryId
        }
    }

    fun updateHistorySearch(query: String) {
        _historySearchQuery.value = query
    }

    fun toggleHistorySubscriptionsOnly() {
        _historySubscriptionsOnly.value = !_historySubscriptionsOnly.value
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun postMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // Settings
    fun updateSettings(
        currencyCode: String,
        monthlyBudgetOverride: Double,
        autoLearnMerchants: Boolean
    ) {
        viewModelScope.launch {
            prefsRepository.setCurrencyCode(currencyCode)
            prefsRepository.setMonthlyBudgetOverride(monthlyBudgetOverride)
            prefsRepository.setAutoLearnMerchants(autoLearnMerchants)
            _snackbarMessage.value = "Updated currency to $currencyCode and budget preferences"
        }
    }

    // Alerts
    fun dismissAlert(alertKey: String) {
        viewModelScope.launch {
            repository.dismissAlert(alertKey, _selectedYearMonth.value)
        }
    }

    fun restoreMonthAlerts() {
        viewModelScope.launch {
            repository.restoreAlertsForMonth(_selectedYearMonth.value)
            _snackbarMessage.value = "Restored all alerts for ${DateUtils.formatYearMonthDisplay(_selectedYearMonth.value)}"
        }
    }

    // AI Extraction: Voice / Conversational Note
    fun extractExpenseFromText(rawText: String, sourceType: String = "VOICE_AI") {
        if (rawText.isBlank()) {
            _aiExtractionState.value = AiExtractionState.Error("Please enter or speak an expense note first.")
            return
        }
        viewModelScope.launch {
            val state = uiState.value
            _aiExtractionState.value = AiExtractionState.Loading("Gemini is parsing your expense...")
            val existingCategoryNames = state.categories.map { it.name }
            val activeDate = if (state.selectedYearMonth == DateUtils.currentYearMonth()) {
                DateUtils.currentDateString()
            } else {
                DateUtils.dateForCurrentMonthDay(state.selectedYearMonth, 15)
            }

            val result = aiService.parseExpenseFromText(
                rawInput = rawText,
                currentDate = activeDate,
                existingCategories = existingCategoryNames,
                defaultCurrency = state.currencyCode
            )

            result.fold(
                onSuccess = { draft ->
                    // Check self-learning merchant rules first if merchant matches a learned rule
                    val matchedRule = state.merchantRules.firstOrNull { rule ->
                        draft.merchant.lowercase(Locale.US).contains(rule.merchantKeyword)
                    }
                    val refinedDraft = if (matchedRule != null) {
                        draft.copy(
                            category = matchedRule.targetCategoryName,
                            statusNotice = "${draft.statusNotice} • Auto-mapped via Merchant Rule"
                        )
                    } else {
                        draft
                    }
                    _aiExtractionState.value = AiExtractionState.Ready(refinedDraft, sourceType)
                },
                onFailure = { err ->
                    _aiExtractionState.value = AiExtractionState.Error(
                        err.localizedMessage ?: "Failed to parse expense note."
                    )
                }
            )
        }
    }

    // AI Extraction: Multimodal Receipt OCR
    fun extractExpenseFromReceipt(bitmap: Bitmap, samplePresetHint: Int? = null) {
        viewModelScope.launch {
            val state = uiState.value
            _aiExtractionState.value = AiExtractionState.Loading("Gemini Vision is scanning receipt line items & tax...")
            val existingCategoryNames = state.categories.map { it.name }
            val activeDate = if (state.selectedYearMonth == DateUtils.currentYearMonth()) {
                DateUtils.currentDateString()
            } else {
                DateUtils.dateForCurrentMonthDay(state.selectedYearMonth, 15)
            }

            val result = aiService.analyzeReceiptBitmap(
                bitmap = bitmap,
                currentDate = activeDate,
                existingCategories = existingCategoryNames,
                defaultCurrency = state.currencyCode,
                samplePresetHint = samplePresetHint
            )

            result.fold(
                onSuccess = { draft ->
                    val matchedRule = state.merchantRules.firstOrNull { rule ->
                        draft.merchant.lowercase(Locale.US).contains(rule.merchantKeyword)
                    }
                    val refinedDraft = if (matchedRule != null) {
                        draft.copy(category = matchedRule.targetCategoryName)
                    } else {
                        draft
                    }
                    _aiExtractionState.value = AiExtractionState.Ready(refinedDraft, "RECEIPT_OCR")
                },
                onFailure = { err ->
                    _aiExtractionState.value = AiExtractionState.Error(
                        err.localizedMessage ?: "Receipt OCR failed. Check connection or try another image."
                    )
                }
            )
        }
    }

    fun resetAiExtractionState() {
        _aiExtractionState.value = AiExtractionState.Idle
    }

    // AI Coaching (Streaming with resilient fallback)
    fun requestAiSpendingCoaching() {
        viewModelScope.launch {
            val state = uiState.value
            _aiCoachingState.value = AiCoachingState.Loading

            val totalSpentStr = CurrencyFormatter.format(state.metrics.totalSpent, state.currencyCode)
            val totalBudgetStr = CurrencyFormatter.format(state.metrics.totalBudget, state.currencyCode)
            val catSummaryStr = state.categoryBreakdown.joinToString("; ") {
                "${it.category.name}: ${CurrencyFormatter.format(it.spent, state.currencyCode)}/${CurrencyFormatter.format(it.budget, state.currencyCode)} (${it.percentOfBudget}%)"
            }
            val topMerchantsStr = state.currentMonthTransactions
                .sortedByDescending { it.amount }
                .take(4)
                .joinToString(", ") { "${it.merchant} (${CurrencyFormatter.format(it.amount, state.currencyCode)})" }

            var latestText = ""
            try {
                aiService.streamSpendingInsights(
                    monthDisplay = state.selectedMonthDisplay,
                    totalSpentFormatted = totalSpentStr,
                    totalBudgetFormatted = totalBudgetStr,
                    percentSpent = state.metrics.percentSpent,
                    velocityHeadline = state.metrics.velocityHeadline,
                    categoryBreakdownText = catSummaryStr,
                    topMerchantsText = topMerchantsStr
                ).collect { chunkSnapshot ->
                    latestText = chunkSnapshot
                    _aiCoachingState.value = AiCoachingState.Success(latestText)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiCoachingState.value = AiCoachingState.Error(
                    e.localizedMessage ?: "Unable to generate AI coaching right now."
                )
            }
        }
    }

    // Commit Transaction (creates new custom category automatically if AI suggested a brand-new category)
    fun saveTransaction(
        id: Long = 0L,
        merchant: String,
        amount: Double,
        dateString: String,
        categoryName: String,
        note: String,
        tagsCsv: String,
        taxAmount: Double = 0.0,
        lineItemsSummary: String = "",
        isSubscription: Boolean = false,
        sentimentNote: String = "",
        sourceType: String = "MANUAL",
        learnMerchantRule: Boolean = uiState.value.autoLearnMerchants
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val cleanMerchant = merchant.trim().ifBlank { "General Merchant" }
            val cleanCategoryName = categoryName.trim().ifBlank { "Food & Dining" }

            // Find matching category or auto-create a custom category if AI suggested a new one
            val existingCat = state.categories.firstOrNull {
                it.name.equals(cleanCategoryName, ignoreCase = true)
            }
            val resolvedCategoryId = if (existingCat != null) {
                existingCat.id
            } else {
                repository.saveCategory(
                    CategoryEntity(
                        name = cleanCategoryName,
                        emoji = "✨",
                        colorHex = "#06B6D4",
                        monthlyBudget = 5000.0,
                        isCustom = true
                    )
                )
            }
            val resolvedCatName = existingCat?.name ?: cleanCategoryName
            val yearMonth = if (dateString.length >= 7) dateString.take(7) else state.selectedYearMonth

            val tx = TransactionEntity(
                id = id,
                merchant = cleanMerchant,
                amount = amount.coerceAtLeast(0.0),
                currency = state.currencyCode,
                dateString = dateString,
                yearMonth = yearMonth,
                categoryId = resolvedCategoryId,
                categoryName = resolvedCatName,
                note = note.trim(),
                tagsCsv = tagsCsv.trim(),
                taxAmount = taxAmount.coerceAtLeast(0.0),
                lineItemsSummary = lineItemsSummary.trim(),
                isSubscription = isSubscription,
                sentimentNote = sentimentNote.trim(),
                sourceType = sourceType
            )

            repository.addOrUpdateTransaction(tx, learnMerchantRule)
            _selectedYearMonth.value = yearMonth
            _aiExtractionState.value = AiExtractionState.Idle
            val formattedAmt = CurrencyFormatter.format(amount, state.currencyCode)
            _snackbarMessage.value = if (id == 0L) {
                "Logged $formattedAmt at $cleanMerchant ($resolvedCatName)"
            } else {
                "Updated $cleanMerchant ($formattedAmt)"
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx.id)
            _snackbarMessage.value = "Deleted ${tx.merchant} transaction"
        }
    }

    // Categories & Self-Learning Rules
    fun saveCategory(
        id: Long = 0L,
        name: String,
        emoji: String,
        colorHex: String,
        monthlyBudget: Double
    ) {
        viewModelScope.launch {
            val cleanName = name.trim().ifBlank { "Custom Category" }
            val existing = uiState.value.categories.firstOrNull { it.id == id }
            val entity = CategoryEntity(
                id = id,
                name = cleanName,
                emoji = emoji.trim().ifBlank { "✨" },
                colorHex = colorHex,
                monthlyBudget = monthlyBudget.coerceAtLeast(500.0),
                isCustom = existing?.isCustom ?: true,
                sortOrder = existing?.sortOrder ?: (uiState.value.categories.size + 1)
            )
            repository.saveCategory(entity)
            _snackbarMessage.value = if (id == 0L) {
                "Created category ${entity.emoji} $cleanName"
            } else {
                "Updated ${entity.emoji} $cleanName budget to ${CurrencyFormatter.format(entity.monthlyBudget, uiState.value.currencyCode)}"
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category.id)
            if (_selectedCategoryFilterId.value == category.id) {
                _selectedCategoryFilterId.value = null
            }
            _snackbarMessage.value = "Removed category ${category.name}"
        }
    }

    fun deleteMerchantRule(keyword: String) {
        viewModelScope.launch {
            repository.deleteMerchantRule(keyword)
            _snackbarMessage.value = "Removed auto-rule for '$keyword'"
        }
    }

    // CSV Export Generator
    fun buildCsvContent(onlyCurrentMonth: Boolean = false): String {
        val state = uiState.value
        val sourceList = if (onlyCurrentMonth) {
            state.currentMonthTransactions
        } else {
            state.allTransactions
        }
        val sb = StringBuilder()
        sb.appendLine("Date,Merchant,Category,Amount,Currency,TaxAmount,IsSubscription,Source,Tags,Note,LineItems")
        sourceList.forEach { tx ->
            fun esc(v: String) = "\"${v.replace("\"", "\"\"")}\""
            sb.appendLine(
                listOf(
                    esc(tx.dateString),
                    esc(tx.merchant),
                    esc(tx.categoryName),
                    String.format(Locale.US, "%.2f", tx.amount),
                    esc(tx.currency),
                    String.format(Locale.US, "%.2f", tx.taxAmount),
                    tx.isSubscription.toString(),
                    esc(tx.sourceType),
                    esc(tx.tagsCsv),
                    esc(tx.note),
                    esc(tx.lineItemsSummary)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }
}
