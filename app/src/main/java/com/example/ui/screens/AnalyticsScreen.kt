package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AiCoachingState
import com.example.ui.LuminaUiState
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
import com.example.util.CurrencyFormatter
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    uiState: LuminaUiState,
    aiCoachingState: AiCoachingState,
    onRequestAiCoaching: () -> Unit,
    onBuildCsv: (Boolean) -> String,
    onPostMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // SAF CSV File Exporter
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvData = onBuildCsv(false)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csvData.toByteArray(Charsets.UTF_8))
                }
                onPostMessage("Saved Lumina Spend CSV export successfully!")
            } catch (e: Exception) {
                onPostMessage("Failed to save CSV: ${e.localizedMessage}")
            }
        }
    }

    val prevMonthTotal = uiState.prevMonthTransactions.sumOf { it.amount }
    val currMonthTotal = uiState.metrics.totalSpent
    val monthDeltaPct = if (prevMonthTotal > 0.0) {
        (((currMonthTotal - prevMonthTotal) / prevMonthTotal) * 100.0).roundToInt()
    } else {
        0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_lazy_column"),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Month-over-Month & Velocity Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "VS LAST MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val sign = if (monthDeltaPct > 0) "+" else ""
                        Text(
                            text = "$sign$monthDeltaPct%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (monthDeltaPct <= 0) LuminaEmerald else LuminaAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last mo: ${CurrencyFormatter.format(prevMonthTotal, uiState.currencyCode)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DAILY BURN PACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = CurrencyFormatter.format(uiState.metrics.dailyBurnRate, uiState.currencyCode),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (uiState.metrics.isPacedUnderBudget) LuminaEmerald else LuminaCrimson,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: ${CurrencyFormatter.format(uiState.metrics.allowedDailyPace, uiState.currencyCode)}/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 2. Weekly & Monthly Comparison Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_comparison_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Weekly & Monthly Comparison",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.selectedMonthDisplay} vs Previous Month",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuminaIndigo)
                                )
                                Text("This Mo", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuminaEmerald.copy(alpha = 0.55f))
                                )
                                Text("Prev Mo", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val maxVal = uiState.weeklyComparison.maxOfOrNull {
                        maxOf(it.currentMonthAmount, it.prevMonthAmount)
                    }?.coerceAtLeast(1000.0) ?: 10000.0

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        val groupCount = uiState.weeklyComparison.size.coerceAtLeast(1)
                        val groupWidth = size.width / groupCount
                        val barWidth = (groupWidth * 0.28f).coerceAtMost(30.dp.toPx())
                        val chartHeight = size.height - 8.dp.toPx()

                        // Subtle horizontal grid lines
                        for (i in 0..3) {
                            val y = chartHeight * (i / 3f)
                            drawLine(
                                color = ObsidianBorder,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        uiState.weeklyComparison.forEachIndexed { idx, point ->
                            val centerX = groupWidth * idx + groupWidth / 2f
                            val currH = ((point.currentMonthAmount / maxVal).toFloat().coerceIn(0.03f, 1f)) * chartHeight
                            val prevH = ((point.prevMonthAmount / maxVal).toFloat().coerceIn(0.03f, 1f)) * chartHeight

                            // Current Month Bar
                            drawRoundRect(
                                color = LuminaIndigo,
                                topLeft = Offset(centerX - barWidth - 3.dp.toPx(), chartHeight - currH),
                                size = Size(barWidth, currH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // Previous Month Bar
                            drawRoundRect(
                                color = LuminaEmerald.copy(alpha = 0.5f),
                                topLeft = Offset(centerX + 3.dp.toPx(), chartHeight - prevH),
                                size = Size(barWidth, prevH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        uiState.weeklyComparison.forEach { point ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = point.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = CurrencyFormatter.formatCompact(point.currentMonthAmount, uiState.currencyCode),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuminaIndigoLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Runway & Recurring Subscriptions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = LuminaEmerald
                        )
                        Text(
                            text = "Runway & Fixed Commitments",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Projected Month-End", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = CurrencyFormatter.format(uiState.metrics.projectedMonthEndSpend, uiState.currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (uiState.metrics.isPacedUnderBudget) LuminaEmerald else LuminaCrimson
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = LuminaIndigoLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Subscriptions (${uiState.metrics.subscriptionCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = CurrencyFormatter.format(uiState.metrics.subscriptionTotal, uiState.currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. Gemini AI Deep Financial Coach Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_coaching_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, LuminaIndigo.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LuminaIndigoLight
                            )
                            Column {
                                Text(
                                    text = "Gemini Spend Velocity Audit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Personalized budget & runway coaching",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onRequestAiCoaching,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaIndigo,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("generate_ai_audit_button")
                        ) {
                            Text("Analyze", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (aiCoachingState) {
                        is AiCoachingState.Idle -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianCardElevated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tap Analyze to run a live Gemini 3.8 Flash audit on your ${uiState.selectedMonthDisplay} category velocities and top merchants.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        is AiCoachingState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = LuminaIndigoLight,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Gemini is auditing your category burn rates...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuminaIndigoLight
                                )
                            }
                        }
                        is AiCoachingState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = LuminaIndigoContainer.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiCoachingState.insightText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                        is AiCoachingState.Error -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianCardElevated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiCoachingState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuminaAmber,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Data Export to CSV Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("csv_export_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Export Ledger to CSV",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Download or share all ${uiState.allTransactions.size} transactions including AI tags, tax splits, and receipt line items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                createCsvLauncher.launch("lumina_spend_${uiState.selectedYearMonth}.csv")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaEmerald,
                                contentColor = Color(0xFF022C22)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save CSV file",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save CSV",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = onBuildCsv(false)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Lumina Spend Ledger (${uiState.selectedMonthDisplay})")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Lumina Spend CSV"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share CSV",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share CSV",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
