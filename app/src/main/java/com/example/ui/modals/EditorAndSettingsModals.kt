package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.domain.AlertSeverity
import com.example.domain.BudgetAlert
import com.example.ui.theme.CategoryColorSwatches
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCrimson
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoContainer
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseHexColor
import com.example.util.DateUtils
import com.example.util.SupportedCurrency

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorModal(
    existingTransaction: TransactionEntity?,
    categories: List<CategoryEntity>,
    currencyCode: String,
    defaultAutoLearn: Boolean,
    onSave: (
        id: Long,
        merchant: String,
        amount: Double,
        dateString: String,
        categoryName: String,
        note: String,
        tagsCsv: String,
        taxAmount: Double,
        lineItems: String,
        isSubscription: Boolean,
        learnMerchantRule: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var merchant by rememberSaveable { mutableStateOf(existingTransaction?.merchant ?: "") }
    var amountText by rememberSaveable {
        mutableStateOf(existingTransaction?.amount?.toString() ?: "")
    }
    var dateString by rememberSaveable {
        mutableStateOf(existingTransaction?.dateString ?: DateUtils.currentDateString())
    }
    var categoryName by rememberSaveable {
        mutableStateOf(
            existingTransaction?.categoryName
                ?: categories.firstOrNull()?.name
                ?: "Food & Dining"
        )
    }
    var note by rememberSaveable { mutableStateOf(existingTransaction?.note ?: "") }
    var tagsCsv by rememberSaveable { mutableStateOf(existingTransaction?.tagsCsv ?: "") }
    var taxText by rememberSaveable {
        mutableStateOf(existingTransaction?.taxAmount?.takeIf { it > 0 }?.toString() ?: "")
    }
    var lineItems by rememberSaveable { mutableStateOf(existingTransaction?.lineItemsSummary ?: "") }
    var isSubscription by rememberSaveable { mutableStateOf(existingTransaction?.isSubscription ?: false) }
    var learnRule by rememberSaveable { mutableStateOf(defaultAutoLearn) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existingTransaction == null) "Log Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Vendor") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("tx_editor_merchant_input")
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencyCode)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tx_editor_amount_input")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category (or type new)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Tap a category pill or type a custom category above:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val selected = categoryName.equals(cat.name, ignoreCase = true)
                    Surface(
                        onClick = { categoryName = cat.name },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) LuminaIndigoContainer else ObsidianCard,
                        border = BorderStroke(
                            1.dp,
                            if (selected) LuminaIndigo else ObsidianBorder
                        )
                    ) {
                        Text(
                            text = "${cat.emoji} ${cat.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextPrimary else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Description / Note") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tagsCsv,
                    onValueChange = { tagsCsv = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = taxText,
                    onValueChange = { taxText = it },
                    label = { Text("Tax ($currencyCode)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.8f)
                )
            }

            OutlinedTextField(
                value = lineItems,
                onValueChange = { lineItems = it },
                label = { Text("Line Items (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Recurring Subscription / Utility", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text("Include in fixed monthly commitment tracking", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = isSubscription,
                    onCheckedChange = { isSubscription = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuminaIndigo)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Remember Merchant Category Rule", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text("Auto-map future expenses from this vendor", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = learnRule,
                    onCheckedChange = { learnRule = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuminaEmerald)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val tax = taxText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onSave(
                            existingTransaction?.id ?: 0L,
                            merchant.ifBlank { "General Expense" },
                            amt,
                            dateString,
                            categoryName,
                            note,
                            tagsCsv,
                            tax,
                            lineItems,
                            isSubscription,
                            learnRule
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuminaIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_transaction_modal_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingTransaction == null) "Save Expense" else "Update Expense",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorModal(
    existingCategory: CategoryEntity?,
    currencyCode: String,
    onSave: (id: Long, name: String, emoji: String, colorHex: String, monthlyBudget: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf(existingCategory?.name ?: "") }
    var emoji by rememberSaveable { mutableStateOf(existingCategory?.emoji ?: "✨") }
    var colorHex by rememberSaveable {
        mutableStateOf(existingCategory?.colorHex ?: CategoryColorSwatches.first())
    }
    var budgetText by rememberSaveable {
        mutableStateOf(existingCategory?.monthlyBudget?.toInt()?.toString() ?: "8000")
    }

    val emojiOptions = listOf(
        "🍔", "🛒", "🚗", "🛍️", "⚡", "💊", "✈️", "🎮",
        "🏠", "🐾", "📚", "☕", "🎁", "🏋️", "🎬", "✨"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (existingCategory == null) "New Custom Category" else "Edit Category & Ceiling",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { if (it.length <= 4) emoji = it },
                    label = { Text("Icon") },
                    singleLine = true,
                    modifier = Modifier.width(84.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_name_input")
                )
            }

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Monthly Budget Ceiling ($currencyCode)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_budget_input")
            )

            Text(
                text = "Pick an Emoji Icon:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojiOptions.forEach { itemEmoji ->
                    val isSelected = emoji == itemEmoji
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) LuminaIndigoContainer else ObsidianCard)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) LuminaIndigo else ObsidianBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { emoji = itemEmoji }
                    ) {
                        Text(text = itemEmoji, fontSize = 20.sp)
                    }
                }
            }

            Text(
                text = "Pick a Neon Accent Color:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryColorSwatches.forEach { swatchHex ->
                    val swatchColor = parseHexColor(swatchHex)
                    val isSelected = colorHex.equals(swatchHex, ignoreCase = true)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorHex = swatchHex }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val budget = budgetText.toDoubleOrNull() ?: 5000.0
                    if (name.isNotBlank()) {
                        onSave(
                            existingCategory?.id ?: 0L,
                            name,
                            emoji,
                            colorHex,
                            budget
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuminaIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_category_modal_button")
            ) {
                Text(
                    text = if (existingCategory == null) "Create Category" else "Save Category Changes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsCenterModal(
    alerts: List<BudgetAlert>,
    onDismissAlert: (String) -> Unit,
    onRestoreAllAlerts: () -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = LuminaAmber
                    )
                    Text(
                        text = "Velocity & Anomaly Alerts (${alerts.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = onRestoreAllAlerts,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore Dismissed Alerts",
                        tint = LuminaIndigoLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelMedium, color = LuminaIndigoLight)
                }
            }

            if (alerts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All active alerts have been reviewed or dismissed. Tap Reset above to restore dismissed alerts for this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                alerts.forEach { alert ->
                    val accent = when (alert.severity) {
                        AlertSeverity.CRITICAL -> LuminaCrimson
                        AlertSeverity.WARNING -> LuminaAmber
                        AlertSeverity.INFO -> LuminaIndigoLight
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚠️ ${alert.badgeText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = { onDismissAlert(alert.key) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Alert",
                                    tint = TextSecondary,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndBudgetModal(
    currentCurrency: String,
    currentBudgetOverride: Double,
    categoryBudgetSum: Double,
    autoLearnMerchants: Boolean,
    onSaveSettings: (currency: String, budgetOverride: Double, autoLearn: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCurrency by rememberSaveable { mutableStateOf(currentCurrency) }
    var budgetText by rememberSaveable {
        mutableStateOf(
            if (currentBudgetOverride > 0.0) currentBudgetOverride.toInt().toString() else ""
        )
    }
    var autoLearn by rememberSaveable { mutableStateOf(autoLearnMerchants) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Currency & Budget Hub Settings",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Text(
                text = "Primary Display Currency:",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SupportedCurrency.entries.forEach { curr ->
                    val isSelected = selectedCurrency == curr.code
                    Surface(
                        onClick = { selectedCurrency = curr.code },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LuminaIndigoContainer else ObsidianCard,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) LuminaIndigo else ObsidianBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_option_${curr.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = curr.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = curr.code,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) LuminaIndigoLight else TextMuted
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = {
                    Text("Custom Monthly Cap Override (Blank = Sum of Categories: ${categoryBudgetSum.toInt()})")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_budget_override_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Self-Learning Merchant Categorization",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Automatically map recurring merchants to your chosen categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = autoLearn,
                    onCheckedChange = { autoLearn = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = LuminaEmerald)
                )
            }

            Button(
                onClick = {
                    val overrideVal = budgetText.toDoubleOrNull() ?: 0.0
                    onSaveSettings(selectedCurrency, overrideVal, autoLearn)
                    onClose()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuminaIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Text("Save Preferences", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
