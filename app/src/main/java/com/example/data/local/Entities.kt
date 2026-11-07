package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val monthlyBudget: Double,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val currency: String = "INR",
    val dateString: String, // YYYY-MM-DD
    val yearMonth: String,  // YYYY-MM
    val categoryId: Long,
    val categoryName: String,
    val note: String = "",
    val tagsCsv: String = "",
    val taxAmount: Double = 0.0,
    val lineItemsSummary: String = "",
    val isSubscription: Boolean = false,
    val sentimentNote: String = "",
    val sourceType: String = "MANUAL", // MANUAL, VOICE_AI, RECEIPT_OCR, SMART_NOTE
    val createdAt: Long = System.currentTimeMillis()
) {
    val tagsList: List<String>
        get() = tagsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}

@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey val merchantKeyword: String, // normalized lowercase merchant keyword
    val targetCategoryId: Long,
    val targetCategoryName: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dismissed_alerts")
data class DismissedAlertEntity(
    @PrimaryKey val alertKey: String,
    val yearMonth: String,
    val dismissedAt: Long = System.currentTimeMillis()
)
