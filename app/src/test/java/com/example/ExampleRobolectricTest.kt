package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.GeminiExpenseAiService
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.domain.BudgetAlertEngine
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lumina Spend", appName)
    }

    @Test
    fun `currency formatter formats INR with Indian grouping`() {
        assertEquals("₹42,500", CurrencyFormatter.format(42500.0, "INR"))
        assertEquals("₹1,25,000", CurrencyFormatter.format(125000.0, "INR"))
        assertEquals("$1,250", CurrencyFormatter.format(1250.0, "USD"))
    }

    @Test
    fun `budget alert engine computes 71 percent and threshold alerts accurately`() {
        val categories = listOf(
            CategoryEntity(id = 1L, name = "Food & Dining", emoji = "🍔", colorHex = "#F59E0B", monthlyBudget = 15000.0),
            CategoryEntity(id = 2L, name = "Shopping", emoji = "🛍️", colorHex = "#6366F1", monthlyBudget = 45000.0)
        )
        val txs = listOf(
            TransactionEntity(
                id = 1L,
                merchant = "Paradise Biryani",
                amount = 13500.0,
                dateString = "2026-11-05",
                yearMonth = "2026-11",
                categoryId = 1L,
                categoryName = "Food & Dining"
            ),
            TransactionEntity(
                id = 2L,
                merchant = "Croma",
                amount = 29000.0,
                dateString = "2026-11-06",
                yearMonth = "2026-11",
                categoryId = 2L,
                categoryName = "Shopping"
            )
        )

        val metrics = BudgetAlertEngine.computeMetrics(
            yearMonth = "2026-11",
            transactions = txs,
            categories = categories,
            monthlyBudgetOverride = 0.0,
            currencyCode = "INR"
        )
        assertEquals(42500.0, metrics.totalSpent, 0.01)
        assertEquals(60000.0, metrics.totalBudget, 0.01)
        assertEquals(71, metrics.percentSpent)

        val breakdown = BudgetAlertEngine.computeCategoryBreakdown(txs, categories)
        val alerts = BudgetAlertEngine.generateAlerts(
            yearMonth = "2026-11",
            transactions = txs,
            prevMonthTransactions = emptyList(),
            categorySummaries = breakdown,
            metrics = metrics,
            dismissedKeys = emptySet(),
            currencyCode = "INR"
        )
        assertTrue(alerts.any { it.key.contains("cat_80_1") })
    }

    @Test
    fun `local smart parser extracts conversational expense accurately`() {
        val service = GeminiExpenseAiService()
        val draft = service.localSmartParse(
            rawInput = "Spent 350 rupees on dinner with friends at Paradise Biryani",
            currentDate = "2026-11-07",
            existingCategories = listOf("Food & Dining", "Transit"),
            defaultCurrency = "INR"
        )
        assertEquals(350.0, draft.amount, 0.01)
        assertEquals("Food & Dining", draft.category)
        assertTrue(draft.merchant.contains("Paradise Biryani", ignoreCase = true))
    }
}
