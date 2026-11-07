package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.ui.LuminaUiState
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoContainer
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun HistoryScreen(
    uiState: LuminaUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onToggleSubscriptionsOnly: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryMap = uiState.categories.associateBy { it.id }

    val filtered = uiState.currentMonthTransactions.filter { tx ->
        val matchesCategory = uiState.selectedCategoryFilterId == null || tx.categoryId == uiState.selectedCategoryFilterId
        val matchesSub = !uiState.historySubscriptionsOnly || tx.isSubscription
        val q = uiState.historySearchQuery.trim()
        val matchesQuery = q.isEmpty() ||
            tx.merchant.contains(q, ignoreCase = true) ||
            tx.categoryName.contains(q, ignoreCase = true) ||
            tx.note.contains(q, ignoreCase = true) ||
            tx.tagsCsv.contains(q, ignoreCase = true)
        matchesCategory && matchesSub && matchesQuery
    }

    val groupedByDate = filtered.groupBy { it.dateString }
    val filteredSum = filtered.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen_container")
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.historySearchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Search merchant, note, category, or #tag...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search transactions",
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (uiState.historySearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LuminaIndigo,
                unfocusedBorderColor = ObsidianBorder,
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.historySubscriptionsOnly,
                    onClick = onToggleSubscriptionsOnly,
                    label = { Text("Subscriptions") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuminaIndigoContainer,
                        selectedLabelColor = LuminaIndigoLight,
                        containerColor = ObsidianCard,
                        labelColor = TextSecondary
                    )
                )
            }

            items(uiState.categories, key = { it.id }) { cat ->
                val selected = uiState.selectedCategoryFilterId == cat.id
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCategoryFilter(cat.id) },
                    label = { Text("${cat.emoji} ${cat.name}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuminaIndigoContainer,
                        selectedLabelColor = TextPrimary,
                        containerColor = ObsidianCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary Count Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filtered.size} transactions",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "Total: ${CurrencyFormatter.format(filteredSum, uiState.currencyCode)}",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching transactions found",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing your search query or category filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                groupedByDate.forEach { (dateStr, txs) ->
                    item(key = "hist_date_$dateStr") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = DateUtils.formatFriendlyDateHeader(dateStr),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyFormatter.format(txs.sumOf { it.amount }, uiState.currencyCode),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    items(txs, key = { it.id }) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            category = categoryMap[tx.categoryId],
                            currencyCode = uiState.currencyCode,
                            onEditClick = onEditTransaction,
                            onDeleteClick = onDeleteTransaction
                        )
                    }
                }
            }
        }
    }
}
