package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.ui.LuminaTab
import com.example.ui.LuminaViewModel
import com.example.ui.components.LuminaTopBar
import com.example.ui.modals.AlertsCenterModal
import com.example.ui.modals.CategoryEditorModal
import com.example.ui.modals.ReceiptLensOcrModal
import com.example.ui.modals.SettingsAndBudgetModal
import com.example.ui.modals.TransactionEditorModal
import com.example.ui.modals.VoiceQuickLogModal
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HubScreen
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoContainer
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.LuminaSpendTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            LuminaSpendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBackground
                ) {
                    LuminaSpendApp()
                }
            }
        }
    }
}

@Composable
fun LuminaSpendApp(
    viewModel: LuminaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val extractionState by viewModel.aiExtractionState.collectAsStateWithLifecycle()
    val coachingState by viewModel.aiCoachingState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    var activeTab by rememberSaveable { mutableStateOf(LuminaTab.HUB) }

    // Modal Visibility States
    var showVoiceModal by rememberSaveable { mutableStateOf(false) }
    var showReceiptOcrModal by rememberSaveable { mutableStateOf(false) }
    var showTransactionEditor by rememberSaveable { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    var showCategoryEditor by rememberSaveable { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var showAlertsCenter by rememberSaveable { mutableStateOf(false) }
    var showSettingsModal by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = activeTab != LuminaTab.HUB) {
        activeTab = LuminaTab.HUB
    }

    Scaffold(
        containerColor = ObsidianBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LuminaTopBar(
                monthDisplay = uiState.selectedMonthDisplay,
                currencyCode = uiState.currencyCode,
                unreadAlertCount = uiState.activeAlerts.size,
                onPrevMonth = { viewModel.shiftMonth(-1) },
                onNextMonth = { viewModel.shiftMonth(1) },
                onOpenAlerts = { showAlertsCenter = true },
                onOpenSettings = { showSettingsModal = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianCard,
                tonalElevation = 0.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navItems = listOf(
                    Triple(
                        LuminaTab.HUB,
                        stringResource(R.string.tab_hub),
                        Icons.Filled.Dashboard to Icons.Outlined.Dashboard
                    ),
                    Triple(
                        LuminaTab.ANALYTICS,
                        stringResource(R.string.tab_analytics),
                        Icons.Filled.Analytics to Icons.Outlined.Analytics
                    ),
                    Triple(
                        LuminaTab.CATEGORIES,
                        stringResource(R.string.tab_categories),
                        Icons.Filled.Category to Icons.Outlined.Category
                    ),
                    Triple(
                        LuminaTab.HISTORY,
                        stringResource(R.string.tab_history),
                        Icons.Filled.History to Icons.Outlined.History
                    )
                )

                navItems.forEach { (tab, label, icons) ->
                    val selected = activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) icons.first else icons.second,
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuminaIndigoLight,
                            selectedTextColor = TextPrimary,
                            indicatorColor = LuminaIndigoContainer,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = activeTab, label = "lumina_tab_crossfade") { tab ->
                when (tab) {
                    LuminaTab.HUB -> {
                        HubScreen(
                            uiState = uiState,
                            onVoiceQuickAdd = {
                                viewModel.resetAiExtractionState()
                                showVoiceModal = true
                            },
                            onReceiptOcr = {
                                viewModel.resetAiExtractionState()
                                showReceiptOcrModal = true
                            },
                            onManualQuickAdd = {
                                editingTransaction = null
                                showTransactionEditor = true
                            },
                            onSmartNoteSubmit = { noteText ->
                                viewModel.resetAiExtractionState()
                                showVoiceModal = true
                                viewModel.extractExpenseFromText(noteText, "SMART_NOTE")
                            },
                            onDismissAlert = { viewModel.dismissAlert(it) },
                            onOpenAiCoach = {
                                activeTab = LuminaTab.ANALYTICS
                                viewModel.requestAiSpendingCoaching()
                            },
                            onSelectCategoryFilter = { viewModel.selectCategoryFilter(it) },
                            onOpenAddCategory = {
                                editingCategory = null
                                showCategoryEditor = true
                            },
                            onEditBudgetClick = { showSettingsModal = true },
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                showTransactionEditor = true
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                            },
                            onViewAllHistory = {
                                activeTab = LuminaTab.HISTORY
                            }
                        )
                    }

                    LuminaTab.ANALYTICS -> {
                        AnalyticsScreen(
                            uiState = uiState,
                            aiCoachingState = coachingState,
                            onRequestAiCoaching = { viewModel.requestAiSpendingCoaching() },
                            onBuildCsv = { onlyCurrent -> viewModel.buildCsvContent(onlyCurrent) },
                            onPostMessage = { viewModel.postMessage(it) }
                        )
                    }

                    LuminaTab.CATEGORIES -> {
                        CategoriesScreen(
                            uiState = uiState,
                            onAddCategory = {
                                editingCategory = null
                                showCategoryEditor = true
                            },
                            onEditCategory = { cat ->
                                editingCategory = cat
                                showCategoryEditor = true
                            },
                            onDeleteCategory = { cat ->
                                viewModel.deleteCategory(cat)
                            },
                            onDeleteMerchantRule = { keyword ->
                                viewModel.deleteMerchantRule(keyword)
                            }
                        )
                    }

                    LuminaTab.HISTORY -> {
                        HistoryScreen(
                            uiState = uiState,
                            onSearchChange = { viewModel.updateHistorySearch(it) },
                            onSelectCategoryFilter = { viewModel.selectCategoryFilter(it) },
                            onToggleSubscriptionsOnly = { viewModel.toggleHistorySubscriptionsOnly() },
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                showTransactionEditor = true
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                            }
                        )
                    }
                }
            }
        }
    }

    // 1. Voice & Smart Note Quick-Log Modal
    if (showVoiceModal) {
        VoiceQuickLogModal(
            extractionState = extractionState,
            categories = uiState.categories,
            currencyCode = uiState.currencyCode,
            onParseText = { text, source -> viewModel.extractExpenseFromText(text, source) },
            onConfirmSave = { merchant, amount, dateStr, catName, note, tags, tax, items, isSub, sentiment, source ->
                viewModel.saveTransaction(
                    merchant = merchant,
                    amount = amount,
                    dateString = dateStr,
                    categoryName = catName,
                    note = note,
                    tagsCsv = tags,
                    taxAmount = tax,
                    lineItemsSummary = items,
                    isSubscription = isSub,
                    sentimentNote = sentiment,
                    sourceType = source
                )
                showVoiceModal = false
            },
            onDismiss = {
                showVoiceModal = false
                viewModel.resetAiExtractionState()
            }
        )
    }

    // 2. Receipt Lens OCR Modal
    if (showReceiptOcrModal) {
        ReceiptLensOcrModal(
            extractionState = extractionState,
            categories = uiState.categories,
            currencyCode = uiState.currencyCode,
            onAnalyzeBitmap = { bmp -> viewModel.extractExpenseFromReceipt(bmp) },
            onConfirmSave = { merchant, amount, dateStr, catName, note, tags, tax, items, isSub, sentiment, source ->
                viewModel.saveTransaction(
                    merchant = merchant,
                    amount = amount,
                    dateString = dateStr,
                    categoryName = catName,
                    note = note,
                    tagsCsv = tags,
                    taxAmount = tax,
                    lineItemsSummary = items,
                    isSubscription = isSub,
                    sentimentNote = sentiment,
                    sourceType = source
                )
                showReceiptOcrModal = false
            },
            onDismiss = {
                showReceiptOcrModal = false
                viewModel.resetAiExtractionState()
            }
        )
    }

    // 3. Manual / Edit Transaction Modal
    if (showTransactionEditor) {
        TransactionEditorModal(
            existingTransaction = editingTransaction,
            categories = uiState.categories,
            currencyCode = uiState.currencyCode,
            defaultAutoLearn = uiState.autoLearnMerchants,
            onSave = { id, merchant, amount, dateStr, catName, note, tags, tax, items, isSub, learnRule ->
                viewModel.saveTransaction(
                    id = id,
                    merchant = merchant,
                    amount = amount,
                    dateString = dateStr,
                    categoryName = catName,
                    note = note,
                    tagsCsv = tags,
                    taxAmount = tax,
                    lineItemsSummary = items,
                    isSubscription = isSub,
                    sourceType = editingTransaction?.sourceType ?: "MANUAL",
                    learnMerchantRule = learnRule
                )
                showTransactionEditor = false
                editingTransaction = null
            },
            onDismiss = {
                showTransactionEditor = false
                editingTransaction = null
            }
        )
    }

    // 4. Category Editor Modal
    if (showCategoryEditor) {
        CategoryEditorModal(
            existingCategory = editingCategory,
            currencyCode = uiState.currencyCode,
            onSave = { id, name, emoji, colorHex, budget ->
                viewModel.saveCategory(
                    id = id,
                    name = name,
                    emoji = emoji,
                    colorHex = colorHex,
                    monthlyBudget = budget
                )
                showCategoryEditor = false
                editingCategory = null
            },
            onDismiss = {
                showCategoryEditor = false
                editingCategory = null
            }
        )
    }

    // 5. Alerts Center Modal
    if (showAlertsCenter) {
        AlertsCenterModal(
            alerts = uiState.activeAlerts,
            onDismissAlert = { viewModel.dismissAlert(it) },
            onRestoreAllAlerts = { viewModel.restoreMonthAlerts() },
            onClose = { showAlertsCenter = false }
        )
    }

    // 6. Currency & Budget Settings Modal
    if (showSettingsModal) {
        SettingsAndBudgetModal(
            currentCurrency = uiState.currencyCode,
            currentBudgetOverride = uiState.monthlyBudgetOverride,
            categoryBudgetSum = uiState.categories.sumOf { it.monthlyBudget },
            autoLearnMerchants = uiState.autoLearnMerchants,
            onSaveSettings = { curr, budgetOverride, autoLearn ->
                viewModel.updateSettings(curr, budgetOverride, autoLearn)
            },
            onClose = { showSettingsModal = false }
        )
    }
}
