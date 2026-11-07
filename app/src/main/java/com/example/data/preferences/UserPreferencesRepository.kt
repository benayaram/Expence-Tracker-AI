package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lumina_user_prefs")

data class UserPreferences(
    val currencyCode: String = "INR",
    val monthlyBudgetOverride: Double = 0.0, // 0 means sum of category budgets
    val autoLearnMerchants: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val MONTHLY_BUDGET_OVERRIDE = doublePreferencesKey("monthly_budget_override")
        val AUTO_LEARN_MERCHANTS = booleanPreferencesKey("auto_learn_merchants")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "INR",
            monthlyBudgetOverride = prefs[Keys.MONTHLY_BUDGET_OVERRIDE] ?: 0.0,
            autoLearnMerchants = prefs[Keys.AUTO_LEARN_MERCHANTS] ?: true
        )
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENCY_CODE] = code
        }
    }

    suspend fun setMonthlyBudgetOverride(budget: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MONTHLY_BUDGET_OVERRIDE] = budget.coerceAtLeast(0.0)
        }
    }

    suspend fun setAutoLearnMerchants(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_LEARN_MERCHANTS] = enabled
        }
    }
}
