package com.example.data.local

import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class LuminaRepository(private val dao: LuminaDao) {

    val categoriesFlow: Flow<List<CategoryEntity>> = dao.observeAllCategories()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = dao.observeAllTransactions()
    val merchantRulesFlow: Flow<List<MerchantRuleEntity>> = dao.observeMerchantRules()
    val dismissedAlertsFlow: Flow<List<DismissedAlertEntity>> = dao.observeDismissedAlerts()

    suspend fun ensureSeedData() {
        if (dao.getCategoryCount() > 0) return

        val defaultCategories = listOf(
            CategoryEntity(
                name = "Food & Dining",
                emoji = "🍔",
                colorHex = "#F59E0B",
                monthlyBudget = 15000.0,
                isCustom = false,
                sortOrder = 1
            ),
            CategoryEntity(
                name = "Groceries",
                emoji = "🛒",
                colorHex = "#10B981",
                monthlyBudget = 12000.0,
                isCustom = false,
                sortOrder = 2
            ),
            CategoryEntity(
                name = "Transit",
                emoji = "🚗",
                colorHex = "#06B6D4",
                monthlyBudget = 8000.0,
                isCustom = false,
                sortOrder = 3
            ),
            CategoryEntity(
                name = "Shopping",
                emoji = "🛍️",
                colorHex = "#6366F1",
                monthlyBudget = 12000.0,
                isCustom = false,
                sortOrder = 4
            ),
            CategoryEntity(
                name = "Subscriptions & Utilities",
                emoji = "⚡",
                colorHex = "#A855F7",
                monthlyBudget = 7000.0,
                isCustom = false,
                sortOrder = 5
            ),
            CategoryEntity(
                name = "Healthcare",
                emoji = "💊",
                colorHex = "#EC4899",
                monthlyBudget = 6000.0,
                isCustom = false,
                sortOrder = 6
            )
        )

        val insertedIds = dao.insertCategories(defaultCategories)
        val catMap = defaultCategories.mapIndexed { idx, cat ->
            cat.name to (insertedIds.getOrNull(idx) ?: (idx + 1).toLong())
        }.toMap()

        val foodId = catMap["Food & Dining"] ?: 1L
        val groceryId = catMap["Groceries"] ?: 2L
        val transitId = catMap["Transit"] ?: 3L
        val shoppingId = catMap["Shopping"] ?: 4L
        val subId = catMap["Subscriptions & Utilities"] ?: 5L
        val healthId = catMap["Healthcare"] ?: 6L

        // Pre-populate self-learning merchant rules
        val seedRules = listOf(
            MerchantRuleEntity("paradise biryani", foodId, "Food & Dining"),
            MerchantRuleEntity("swiggy", foodId, "Food & Dining"),
            MerchantRuleEntity("zomato", foodId, "Food & Dining"),
            MerchantRuleEntity("starbucks", foodId, "Food & Dining"),
            MerchantRuleEntity("bigbasket", groceryId, "Groceries"),
            MerchantRuleEntity("blinkit", groceryId, "Groceries"),
            MerchantRuleEntity("indian oil", transitId, "Transit"),
            MerchantRuleEntity("uber", transitId, "Transit"),
            MerchantRuleEntity("ola", transitId, "Transit"),
            MerchantRuleEntity("croma", shoppingId, "Shopping"),
            MerchantRuleEntity("amazon", shoppingId, "Shopping"),
            MerchantRuleEntity("airtel", subId, "Subscriptions & Utilities"),
            MerchantRuleEntity("netflix", subId, "Subscriptions & Utilities"),
            MerchantRuleEntity("apollo pharmacy", healthId, "Healthcare")
        )
        seedRules.forEach { dao.upsertMerchantRule(it) }

        val currentYm = DateUtils.currentYearMonth()
        val prevYm = DateUtils.shiftYearMonth(currentYm, -1)

        // 8 realistic current-month seed transactions totaling ₹42,500 / ₹60,000 (71% spent)
        val seedTransactions = listOf(
            TransactionEntity(
                merchant = "Paradise Biryani",
                amount = 3850.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 7),
                yearMonth = currentYm,
                categoryId = foodId,
                categoryName = "Food & Dining",
                note = "Weekend dinner with friends at Jubilee Hills",
                tagsCsv = "dining,weekend,friends",
                taxAmount = 192.5,
                lineItemsSummary = "2x Royal Mutton Biryani, Kebabs, Lime Soda",
                isSubscription = false,
                sentimentNote = "Dining spend is 28% higher than usual this week",
                sourceType = "VOICE_AI",
                createdAt = System.currentTimeMillis() - 3_600_000L
            ),
            TransactionEntity(
                merchant = "Swiggy Gourmet & Cafe",
                amount = 6950.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 6),
                yearMonth = currentYm,
                categoryId = foodId,
                categoryName = "Food & Dining",
                note = "Team sprint lunch & artisan coffee",
                tagsCsv = "delivery,team,dining",
                taxAmount = 347.5,
                lineItemsSummary = "Woodfired Pizzas, Cold Brews, Desserts",
                isSubscription = false,
                sentimentNote = "Elevated weekday food delivery frequency",
                sourceType = "SMART_NOTE",
                createdAt = System.currentTimeMillis() - 18_000_000L
            ),
            TransactionEntity(
                merchant = "Croma Electronics",
                amount = 11500.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 5),
                yearMonth = currentYm,
                categoryId = shoppingId,
                categoryName = "Shopping",
                note = "ANC Wireless Earbuds & USB-C GaN Charger",
                tagsCsv = "electronics,gadgets,audio",
                taxAmount = 2070.0,
                lineItemsSummary = "Sony WF-C700N ANC, 65W GaN Adapter",
                isSubscription = false,
                sentimentNote = "96% of Shopping budget consumed in a single purchase",
                sourceType = "RECEIPT_OCR",
                createdAt = System.currentTimeMillis() - 86_400_000L
            ),
            TransactionEntity(
                merchant = "BigBasket Superstore",
                amount = 6400.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 4),
                yearMonth = currentYm,
                categoryId = groceryId,
                categoryName = "Groceries",
                note = "Monthly pantry restock & organic produce",
                tagsCsv = "groceries,organic,pantry",
                taxAmount = 320.0,
                lineItemsSummary = "Basmati Rice 5kg, Extra Virgin Olive Oil, Fresh Greens, Almond Milk",
                isSubscription = false,
                sentimentNote = "On track with monthly grocery cadence",
                sourceType = "RECEIPT_OCR",
                createdAt = System.currentTimeMillis() - 172_800_000L
            ),
            TransactionEntity(
                merchant = "Indian Oil Fuel Station",
                amount = 4200.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 3),
                yearMonth = currentYm,
                categoryId = transitId,
                categoryName = "Transit",
                note = "Full tank XP95 petrol refill",
                tagsCsv = "fuel,commute,car",
                taxAmount = 0.0,
                lineItemsSummary = "XP95 Petrol 38.4L @ ₹109.3/L",
                isSubscription = false,
                sentimentNote = "Standard bi-weekly fuel fill-up",
                sourceType = "SMART_NOTE",
                createdAt = System.currentTimeMillis() - 259_200_000L
            ),
            TransactionEntity(
                merchant = "Airtel Fiber & Postpaid",
                amount = 4150.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 2),
                yearMonth = currentYm,
                categoryId = subId,
                categoryName = "Subscriptions & Utilities",
                note = "1 Gbps Fiber + Family Infinity Plan",
                tagsCsv = "utilities,broadband,recurring",
                taxAmount = 747.0,
                lineItemsSummary = "Airtel Black 1Gbps Plan + 3 Add-on SIMs",
                isSubscription = true,
                sentimentNote = "Recurring utility bill auto-detected",
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 345_600_000L
            ),
            TransactionEntity(
                merchant = "Apollo Pharmacy & Diagnostics",
                amount = 3600.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 2),
                yearMonth = currentYm,
                categoryId = healthId,
                categoryName = "Healthcare",
                note = "Preventive blood panel & daily supplements",
                tagsCsv = "health,pharmacy,wellness",
                taxAmount = 432.0,
                lineItemsSummary = "Omega-3, Vitamin D3, Comprehensive Metabolic Panel",
                isSubscription = false,
                sentimentNote = "Routine quarterly wellness expense",
                sourceType = "RECEIPT_OCR",
                createdAt = System.currentTimeMillis() - 400_000_000L
            ),
            TransactionEntity(
                merchant = "Netflix & Workspace Cloud",
                amount = 1850.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(currentYm, 1),
                yearMonth = currentYm,
                categoryId = subId,
                categoryName = "Subscriptions & Utilities",
                note = "4K Ultra HD Plan + 2TB Cloud Backup",
                tagsCsv = "streaming,cloud,subscription",
                taxAmount = 333.0,
                lineItemsSummary = "Netflix Premium ₹649 + Cloud Pro ₹1,201",
                isSubscription = true,
                sentimentNote = "Subscriptions & Utilities reached 86% of ceiling",
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 480_000_000L
            ),
            // Previous month transactions for rich monthly comparison charts
            TransactionEntity(
                merchant = "BigBasket Superstore",
                amount = 9800.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(prevYm, 18),
                yearMonth = prevYm,
                categoryId = groceryId,
                categoryName = "Groceries",
                note = "Monthly household groceries",
                tagsCsv = "groceries,household",
                isSubscription = false,
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 2_400_000_000L
            ),
            TransactionEntity(
                merchant = "Zomato Dining & Delivery",
                amount = 8400.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(prevYm, 14),
                yearMonth = prevYm,
                categoryId = foodId,
                categoryName = "Food & Dining",
                note = "Last month dining & weekend takeout",
                tagsCsv = "dining,food",
                isSubscription = false,
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 2_600_000_000L
            ),
            TransactionEntity(
                merchant = "Uber & Metro Pass",
                amount = 6100.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(prevYm, 10),
                yearMonth = prevYm,
                categoryId = transitId,
                categoryName = "Transit",
                note = "Airport rides & daily metro commute",
                tagsCsv = "transit,commute",
                isSubscription = false,
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 2_800_000_000L
            ),
            TransactionEntity(
                merchant = "Amazon India",
                amount = 7900.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(prevYm, 5),
                yearMonth = prevYm,
                categoryId = shoppingId,
                categoryName = "Shopping",
                note = "Home office desk lamp & ergonomic stand",
                tagsCsv = "shopping,office",
                isSubscription = false,
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 3_000_000_000L
            ),
            TransactionEntity(
                merchant = "Airtel & Electricity Bill",
                amount = 6200.0,
                currency = "INR",
                dateString = DateUtils.dateForCurrentMonthDay(prevYm, 2),
                yearMonth = prevYm,
                categoryId = subId,
                categoryName = "Subscriptions & Utilities",
                note = "Monthly electricity & fiber internet",
                tagsCsv = "utilities,recurring",
                isSubscription = true,
                sourceType = "MANUAL",
                createdAt = System.currentTimeMillis() - 3_200_000_000L
            )
        )

        dao.insertTransactions(seedTransactions)
    }

    suspend fun addOrUpdateTransaction(
        transaction: TransactionEntity,
        learnMerchantRule: Boolean
    ): Long {
        val id = if (transaction.id == 0L) {
            dao.insertTransaction(transaction)
        } else {
            dao.updateTransaction(transaction)
            transaction.id
        }

        if (learnMerchantRule && transaction.merchant.isNotBlank()) {
            val normalized = transaction.merchant.trim().lowercase()
            dao.upsertMerchantRule(
                MerchantRuleEntity(
                    merchantKeyword = normalized,
                    targetCategoryId = transaction.categoryId,
                    targetCategoryName = transaction.categoryName
                )
            )
        }
        return id
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransactionById(id)
    }

    suspend fun saveCategory(category: CategoryEntity): Long {
        return if (category.id == 0L) {
            val count = dao.getCategoryCount()
            dao.insertCategory(category.copy(sortOrder = count + 1))
        } else {
            dao.updateCategory(category)
            dao.updateTransactionCategoryNames(category.id, category.name)
            category.id
        }
    }

    suspend fun deleteCategory(categoryId: Long) {
        dao.deleteCategoryById(categoryId)
    }

    suspend fun deleteMerchantRule(keyword: String) {
        dao.deleteMerchantRule(keyword)
    }

    suspend fun dismissAlert(alertKey: String, yearMonth: String) {
        dao.dismissAlert(DismissedAlertEntity(alertKey = alertKey, yearMonth = yearMonth))
    }

    suspend fun restoreAlertsForMonth(yearMonth: String) {
        dao.restoreAlertsForMonth(yearMonth)
    }
}
