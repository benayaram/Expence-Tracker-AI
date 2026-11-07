package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.domain.ThresholdStatus
import com.example.ui.LuminaUiState
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCrimson
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseHexColor
import com.example.util.CurrencyFormatter

@Composable
fun CategoriesScreen(
    uiState: LuminaUiState,
    onAddCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDeleteMerchantRule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("categories_lazy_column"),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Add Custom Category Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Categories & Ceilings",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Threshold alerts fire at 50%, 80%, and 100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onAddCategory,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuminaIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("new_custom_category_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Category",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Custom", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Category Budget Cards
        items(uiState.categoryBreakdown, key = { it.category.id }) { item ->
            val cat = item.category
            val catColor = parseHexColor(cat.colorHex)
            val statusColor = when (item.status) {
                ThresholdStatus.BREACH_100 -> LuminaCrimson
                ThresholdStatus.WARNING_80 -> LuminaAmber
                ThresholdStatus.WATCH_50 -> LuminaIndigoLight
                ThresholdStatus.SAFE -> LuminaEmerald
            }
            val statusLabel = when (item.status) {
                ThresholdStatus.BREACH_100 -> "100%+ OVERRUN"
                ThresholdStatus.WARNING_80 -> "80%+ WARNING"
                ThresholdStatus.WATCH_50 -> "50%+ MIDPOINT"
                ThresholdStatus.SAFE -> "SAFE PACE"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditCategory(cat) }
                    .testTag("category_card_${cat.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(catColor.copy(alpha = 0.18f))
                                .border(1.dp, catColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        ) {
                            Text(text = cat.emoji, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.transactionCount} transactions • Median: ${CurrencyFormatter.format(item.medianTransaction, uiState.currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onEditCategory(cat) },
                            modifier = Modifier.testTag("edit_category_${cat.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Category",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (cat.isCustom) {
                            IconButton(
                                onClick = { onDeleteCategory(cat) },
                                modifier = Modifier.testTag("delete_category_${cat.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Custom Category",
                                    tint = LuminaCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${CurrencyFormatter.format(item.spent, uiState.currencyCode)} spent",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cap: ${CurrencyFormatter.format(item.budget, uiState.currencyCode)} (${item.percentOfBudget}%)",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (item.spent / item.budget.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = ObsidianBorder
                    )
                }
            }
        }

        // Self-Learning Merchant Rules Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = LuminaIndigoLight
                        )
                        Column {
                            Text(
                                text = "Self-Learning Merchant Mapping",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Automatically maps recognized vendors to your preferred category",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.merchantRules.isEmpty()) {
                        Text(
                            text = "No learned rules yet. Save or edit any transaction to train Lumina Spend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.merchantRules.take(10).forEach { rule ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ObsidianCardElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "\"${rule.merchantKeyword}\"",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "→",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = rule.targetCategoryName,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = LuminaIndigoLight
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteMerchantRule(rule.merchantKeyword) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete merchant rule",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
