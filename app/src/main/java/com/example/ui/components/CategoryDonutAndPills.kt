package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.domain.CategorySpendSummary
import com.example.domain.ThresholdStatus
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCrimson
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoContainer
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseHexColor
import com.example.util.CurrencyFormatter
import kotlin.math.roundToInt

@Composable
fun CategoryBreakdownSection(
    categorySummaries: List<CategorySpendSummary>,
    selectedCategoryId: Long?,
    currencyCode: String,
    onSelectCategory: (Long?) -> Unit,
    onAddCustomCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (selectedCategoryId != null) {
                Text(
                    text = "Clear Filter",
                    style = MaterialTheme.typography.labelLarge,
                    color = LuminaIndigoLight,
                    modifier = Modifier
                        .clickable { onSelectCategory(null) }
                        .padding(4.dp)
                        .testTag("clear_category_filter")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Category Filter Pills + "+ Custom" button
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categorySummaries, key = { it.category.id }) { item ->
                val isSelected = selectedCategoryId == item.category.id
                val catColor = parseHexColor(item.category.colorHex)

                Surface(
                    onClick = { onSelectCategory(item.category.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) catColor.copy(alpha = 0.24f) else ObsidianCard,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) catColor else ObsidianBorder
                    ),
                    modifier = Modifier.testTag("category_pill_${item.category.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = item.category.emoji, fontSize = 15.sp)
                        Text(
                            text = item.category.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                        Text(
                            text = "${item.percentOfBudget}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = when (item.status) {
                                ThresholdStatus.BREACH_100 -> LuminaCrimson
                                ThresholdStatus.WARNING_80 -> LuminaAmber
                                else -> catColor
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Surface(
                    onClick = onAddCustomCategory,
                    shape = RoundedCornerShape(14.dp),
                    color = LuminaIndigoContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, LuminaIndigo.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("add_custom_category_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Category",
                            tint = LuminaIndigoLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelLarge,
                            color = LuminaIndigoLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Donut Chart + Legend Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("category_donut_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            border = BorderStroke(1.dp, ObsidianBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Multi-segment Donut Chart
                val activeSummaries = categorySummaries.filter { it.spent > 0.0 }
                val highlightedSummary = categorySummaries.firstOrNull { it.category.id == selectedCategoryId }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(132.dp)
                ) {
                    Canvas(modifier = Modifier.size(124.dp)) {
                        val stroke = 16.dp.toPx()
                        val inset = stroke / 2f + 4.dp.toPx()
                        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

                        if (activeSummaries.isEmpty()) {
                            drawArc(
                                color = ObsidianBorder,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = stroke)
                            )
                        } else {
                            var startAngle = -90f
                            val gapDegrees = if (activeSummaries.size > 1) 4f else 0f
                            val availableDegrees = 360f - (gapDegrees * activeSummaries.size)

                            activeSummaries.forEach { summary ->
                                val sweep = (summary.shareOfTotalSpend * availableDegrees).coerceAtLeast(6f)
                                val segmentColor = parseHexColor(summary.category.colorHex)
                                val isDimmed = selectedCategoryId != null && selectedCategoryId != summary.category.id

                                drawArc(
                                    color = if (isDimmed) segmentColor.copy(alpha = 0.25f) else segmentColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = Offset(inset, inset),
                                    size = arcSize,
                                    style = Stroke(
                                        width = if (selectedCategoryId == summary.category.id) stroke + 4.dp.toPx() else stroke,
                                        cap = StrokeCap.Butt
                                    )
                                )
                                startAngle += sweep + gapDegrees
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (highlightedSummary != null) {
                            Text(
                                text = highlightedSummary.category.emoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${(highlightedSummary.shareOfTotalSpend * 100).roundToInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = highlightedSummary.category.name.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        } else {
                            val topCat = activeSummaries.firstOrNull()
                            Text(
                                text = if (topCat != null) "${(topCat.shareOfTotalSpend * 100).roundToInt()}%" else "0%",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = topCat?.category?.name?.split(" ")?.firstOrNull() ?: "No Spend",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Right Column: Top Categories with Mini Ceiling Progress
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categorySummaries.take(4).forEach { summary ->
                        val catColor = parseHexColor(summary.category.colorHex)
                        val barColor = when (summary.status) {
                            ThresholdStatus.BREACH_100 -> LuminaCrimson
                            ThresholdStatus.WARNING_80 -> LuminaAmber
                            else -> catColor
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectCategory(summary.category.id) }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Text(
                                        text = "${summary.category.emoji} ${summary.category.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.format(summary.spent, currencyCode),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (summary.spent / summary.budget.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = barColor,
                                trackColor = ObsidianBorder
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    currencyCode: String,
    onEditClick: (TransactionEntity) -> Unit,
    onDeleteClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val catColor = category?.let { parseHexColor(it.colorHex) } ?: LuminaIndigo
    val emoji = category?.emoji ?: "💳"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick(transaction) }
            .testTag("transaction_card_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Emoji Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(catColor.copy(alpha = 0.16f))
                        .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(13.dp))
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Merchant, Category Tag & Note
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.merchant,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (transaction.isSubscription) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = LuminaIndigoContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = "Recurring Subscription",
                                        tint = LuminaIndigoLight,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "SUB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuminaIndigoLight
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val subtitle = transaction.note.ifBlank { transaction.categoryName }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Column: Amount & Source / Edit Actions
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.format(transaction.amount, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Bottom Row: Category Pill, Auto-Tags, Tax Split, and Edit/Delete Icons
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = catColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = transaction.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (transaction.sourceType != "MANUAL") {
                        val badgeLabel = when (transaction.sourceType) {
                            "VOICE_AI" -> "🎙️ Voice AI"
                            "RECEIPT_OCR" -> "📸 Receipt OCR"
                            else -> "✨ Smart AI"
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianCardElevated
                        ) {
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = LuminaIndigoLight,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    transaction.tagsList.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianCardElevated
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (transaction.taxAmount > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianCardElevated
                        ) {
                            Text(
                                text = "Tax: ${CurrencyFormatter.format(transaction.taxAmount, currencyCode)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuminaEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onEditClick(transaction) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_tx_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaction",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeleteClick(transaction) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_tx_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Transaction",
                            tint = LuminaCrimson.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (transaction.sentimentNote.isNotBlank() || transaction.lineItemsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val detailLine = if (transaction.lineItemsSummary.isNotBlank()) {
                    "Items: ${transaction.lineItemsSummary}"
                } else {
                    "💡 ${transaction.sentimentNote}"
                }
                Text(
                    text = detailLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
