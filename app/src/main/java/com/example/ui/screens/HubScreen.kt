package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.ui.LuminaUiState
import com.example.ui.components.AiAlertCarousel
import com.example.ui.components.CategoryBreakdownSection
import com.example.ui.components.HeroBudgetCard
import com.example.ui.components.QuickAddActionStrip
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun HubScreen(
    uiState: LuminaUiState,
    onVoiceQuickAdd: () -> Unit,
    onReceiptOcr: () -> Unit,
    onManualQuickAdd: () -> Unit,
    onSmartNoteSubmit: (String) -> Unit,
    onDismissAlert: (String) -> Unit,
    onOpenAiCoach: () -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onOpenAddCategory: () -> Unit,
    onEditBudgetClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onViewAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inlineSmartNote by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredTransactions = if (uiState.selectedCategoryFilterId != null) {
        uiState.currentMonthTransactions.filter { it.categoryId == uiState.selectedCategoryFilterId }
    } else {
        uiState.currentMonthTransactions
    }
    val recentGrouped = filteredTransactions.take(10).groupBy { it.dateString }
    val categoryMap = uiState.categories.associateBy { it.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hub_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hero Card: Glassmorphic Monthly Budget Progress
        item {
            HeroBudgetCard(
                metrics = uiState.metrics,
                currencyCode = uiState.currencyCode,
                onEditBudgetClick = onEditBudgetClick
            )
        }

        // 2. Quick Input Strip: Voice Quick-Add | Receipt OCR | Manual Quick Log
        item {
            QuickAddActionStrip(
                onVoiceQuickAdd = onVoiceQuickAdd,
                onReceiptOcr = onReceiptOcr,
                onManualQuickAdd = onManualQuickAdd
            )
        }

        // 3. Inline Gemini Smart Note Bar ("Swiggy order 420" or "fuel at Indian Oil 1200")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LuminaIndigoLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gemini Instant Note Parser",
                            style = MaterialTheme.typography.labelLarge,
                            color = LuminaIndigoLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inlineSmartNote,
                            onValueChange = { inlineSmartNote = it },
                            placeholder = {
                                Text(
                                    text = "e.g. \"Swiggy order 420\" or \"Fuel at Indian Oil 1200\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminaIndigo,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedContainerColor = ObsidianCardElevated,
                                unfocusedContainerColor = ObsidianCardElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inlineSmartNote.isNotBlank()) {
                                        focusManager.clearFocus()
                                        onSmartNoteSubmit(inlineSmartNote)
                                        inlineSmartNote = ""
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inline_smart_note_input")
                        )

                        Button(
                            onClick = {
                                if (inlineSmartNote.isNotBlank()) {
                                    focusManager.clearFocus()
                                    onSmartNoteSubmit(inlineSmartNote)
                                    inlineSmartNote = ""
                                }
                            },
                            enabled = inlineSmartNote.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaIndigo,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("inline_smart_note_parse_button")
                        ) {
                            Text(
                                text = "Parse",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sample quick chips for 1-tap tryout
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val samples = listOf(
                            "Swiggy order 420",
                            "Fuel at Indian Oil 1200",
                            "Starbucks coffee 380"
                        )
                        samples.forEach { sample ->
                            Surface(
                                onClick = { inlineSmartNote = sample },
                                shape = RoundedCornerShape(8.dp),
                                color = ObsidianCardElevated,
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Text(
                                    text = sample,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. AI Insights & Anomaly Warning Banner Carousel
        item {
            AiAlertCarousel(
                alerts = uiState.activeAlerts,
                onDismissAlert = onDismissAlert,
                onAskGeminiCoaching = onOpenAiCoach
            )
        }

        // 5. Category Breakdown (Pills + Donut Chart)
        item {
            CategoryBreakdownSection(
                categorySummaries = uiState.categoryBreakdown,
                selectedCategoryId = uiState.selectedCategoryFilterId,
                currencyCode = uiState.currencyCode,
                onSelectCategory = onSelectCategoryFilter,
                onAddCustomCategory = onOpenAddCategory
            )
        }

        // 6. Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianCardElevated
                    ) {
                        Text(
                            text = "${filteredTransactions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "View Ledger →",
                    style = MaterialTheme.typography.labelLarge,
                    color = LuminaIndigoLight,
                    modifier = Modifier
                        .clickable { onViewAllHistory() }
                        .padding(4.dp)
                        .testTag("view_all_history_button")
                )
            }
        }

        // 7. Grouped Transaction Feed
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No expenses logged for this filter",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use Voice Add, Receipt OCR, or the Smart Note bar above to log an expense.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            recentGrouped.forEach { (dateStr, txsForDate) ->
                item(key = "header_$dateStr") {
                    Text(
                        text = DateUtils.formatFriendlyDateHeader(dateStr),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                    )
                }

                items(txsForDate, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        category = categoryMap[tx.categoryId],
                        currencyCode = uiState.currencyCode,
                        onEditClick = onEditTransaction,
                        onDeleteClick = onDeleteTransaction,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
