package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AlertSeverity
import com.example.domain.BudgetAlert
import com.example.domain.MonthlyBudgetMetrics
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaAmberContainer
import com.example.ui.theme.LuminaCrimson
import com.example.ui.theme.LuminaCrimsonContainer
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaEmeraldContainer
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

@Composable
fun LuminaTopBar(
    monthDisplay: String,
    currencyCode: String,
    unreadAlertCount: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand & Month Switcher Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianCard)
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = TextPrimary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "LUMINA SPEND",
                    style = MaterialTheme.typography.labelSmall,
                    color = LuminaIndigoLight,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = monthDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = TextPrimary
                )
            }
        }

        // Right Actions: Currency / Settings & Alerts Bell
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("currency_settings_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Currency and budget settings",
                        tint = LuminaIndigoLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = currencyCode,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }

            Surface(
                onClick = onOpenAlerts,
                shape = RoundedCornerShape(12.dp),
                color = ObsidianCard,
                border = BorderStroke(
                    1.dp,
                    if (unreadAlertCount > 0) LuminaAmber.copy(alpha = 0.6f) else ObsidianBorder
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("alerts_bell_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BadgedBox(
                        badge = {
                            if (unreadAlertCount > 0) {
                                Badge(
                                    containerColor = LuminaCrimson,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = unreadAlertCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts and Notifications",
                            tint = if (unreadAlertCount > 0) LuminaAmber else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBudgetCard(
    metrics: MonthlyBudgetMetrics,
    currencyCode: String,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = metrics.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "budget_gauge_progress"
    )

    val gaugeColor = when {
        metrics.percentSpent >= 100 -> LuminaCrimson
        metrics.percentSpent >= 80 -> LuminaAmber
        else -> LuminaEmerald
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("hero_budget_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle ambient visual backdrop
            Image(
                painter = painterResource(id = R.drawable.img_lumina_glow_1794079179340),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.22f)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianCard.copy(alpha = 0.65f),
                                ObsidianCard.copy(alpha = 0.94f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Monetary Readout & Remaining Balance
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                onClick = onEditBudgetClick,
                                shape = RoundedCornerShape(8.dp),
                                color = LuminaIndigoContainer.copy(alpha = 0.8f),
                                modifier = Modifier.testTag("adjust_cap_chip")
                            ) {
                                Text(
                                    text = "Adjust Cap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuminaIndigoLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = CurrencyFormatter.format(metrics.totalSpent, currencyCode),
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "of ${CurrencyFormatter.format(metrics.totalBudget, currencyCode)} monthly cap",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remaining Balance Pill
                        val isPositiveRemaining = metrics.remainingBudget >= 0
                        val remText = if (isPositiveRemaining) {
                            "${CurrencyFormatter.format(metrics.remainingBudget, currencyCode)} left to spend"
                        } else {
                            "${CurrencyFormatter.format(-metrics.remainingBudget, currencyCode)} over budget"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPositiveRemaining) LuminaEmerald else LuminaCrimson)
                            )
                            Text(
                                text = remText,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isPositiveRemaining) LuminaEmerald else LuminaCrimson
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Column: Interactive Circular Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(116.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 11.dp.toPx()
                            val inset = strokeWidth / 2f + 4.dp.toPx()
                            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

                            // Track Arc
                            drawArc(
                                color = ObsidianBorder,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Progress Arc with gradient
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        LuminaIndigo,
                                        gaugeColor,
                                        LuminaIndigoLight
                                    )
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${metrics.percentSpent}%",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SPENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Velocity & Runway Strip
                val velocityBg = if (metrics.isPacedUnderBudget) {
                    LuminaEmeraldContainer.copy(alpha = 0.55f)
                } else {
                    LuminaCrimsonContainer.copy(alpha = 0.6f)
                }
                val velocityAccent = if (metrics.isPacedUnderBudget) LuminaEmerald else LuminaCrimson

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = velocityBg,
                    border = BorderStroke(1.dp, velocityAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (metrics.isPacedUnderBudget) {
                                Icons.Default.TrendingDown
                            } else {
                                Icons.Default.TrendingUp
                            },
                            contentDescription = "Burn velocity indicator",
                            tint = velocityAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = metrics.velocityHeadline,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = metrics.velocitySubtext,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddActionStrip(
    onVoiceQuickAdd: () -> Unit,
    onReceiptOcr: () -> Unit,
    onManualQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Voice Quick-Add
        Surface(
            onClick = onVoiceQuickAdd,
            shape = RoundedCornerShape(16.dp),
            color = LuminaIndigo,
            modifier = Modifier
                .weight(1.1f)
                .height(54.dp)
                .testTag("quick_voice_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Quick-Add",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Voice Add",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // 2. Receipt OCR
        Surface(
            onClick = onReceiptOcr,
            shape = RoundedCornerShape(16.dp),
            color = ObsidianCardElevated,
            border = BorderStroke(1.dp, LuminaIndigo.copy(alpha = 0.55f)),
            modifier = Modifier
                .weight(1.1f)
                .height(54.dp)
                .testTag("quick_receipt_ocr_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan Receipt OCR",
                    tint = LuminaIndigoLight,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Receipt OCR",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
        }

        // 3. Manual / Smart Entry
        Surface(
            onClick = onManualQuickAdd,
            shape = RoundedCornerShape(16.dp),
            color = ObsidianCard,
            border = BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier
                .weight(0.9f)
                .height(54.dp)
                .testTag("quick_manual_add_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Manual Quick Add",
                    tint = LuminaEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quick Log",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AiAlertCarousel(
    alerts: List<BudgetAlert>,
    onDismissAlert: (String) -> Unit,
    onAskGeminiCoaching: () -> Unit,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = LuminaAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "AI Insights & Anomaly Warnings",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }

            Surface(
                onClick = onAskGeminiCoaching,
                shape = RoundedCornerShape(10.dp),
                color = LuminaIndigoContainer,
                border = BorderStroke(1.dp, LuminaIndigo.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("ask_gemini_coach_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask Gemini Coach",
                        tint = LuminaIndigoLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI Coach",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuminaIndigoLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (alerts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = BorderStroke(1.dp, LuminaEmerald.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LuminaEmerald
                    )
                    Column {
                        Text(
                            text = "All Category Budgets & Velocities Nominal",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "No spend anomalies or duplicate charges active right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts, key = { it.key }) { alert ->
                    val accentColor = when (alert.severity) {
                        AlertSeverity.CRITICAL -> LuminaCrimson
                        AlertSeverity.WARNING -> LuminaAmber
                        AlertSeverity.INFO -> LuminaIndigoLight
                    }
                    val containerBg = when (alert.severity) {
                        AlertSeverity.CRITICAL -> LuminaCrimsonContainer.copy(alpha = 0.38f)
                        AlertSeverity.WARNING -> LuminaAmberContainer.copy(alpha = 0.38f)
                        AlertSeverity.INFO -> LuminaIndigoContainer.copy(alpha = 0.38f)
                    }

                    Card(
                        modifier = Modifier
                            .width(310.dp)
                            .testTag("alert_card_${alert.key}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(containerBg)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accentColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "⚠️ ${alert.badgeText}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDismissAlert(alert.key) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("dismiss_alert_${alert.key}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss warning",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alert.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
