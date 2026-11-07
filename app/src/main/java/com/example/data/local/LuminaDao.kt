package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaDao {

    // Categories
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY dateString DESC, createdAt DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth ORDER BY dateString DESC, createdAt DESC")
    fun observeTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateString DESC, createdAt DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Query("UPDATE transactions SET categoryName = :newName WHERE categoryId = :categoryId")
    suspend fun updateTransactionCategoryNames(categoryId: Long, newName: String)

    // Self-Learning Merchant Rules
    @Query("SELECT * FROM merchant_rules ORDER BY updatedAt DESC")
    fun observeMerchantRules(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules")
    suspend fun getMerchantRulesOnce(): List<MerchantRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMerchantRule(rule: MerchantRuleEntity)

    @Query("DELETE FROM merchant_rules WHERE merchantKeyword = :keyword")
    suspend fun deleteMerchantRule(keyword: String)

    // Dismissed Alerts
    @Query("SELECT * FROM dismissed_alerts")
    fun observeDismissedAlerts(): Flow<List<DismissedAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dismissAlert(entity: DismissedAlertEntity)

    @Query("DELETE FROM dismissed_alerts WHERE yearMonth = :yearMonth")
    suspend fun restoreAlertsForMonth(yearMonth: String)
}
