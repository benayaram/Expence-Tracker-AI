package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val label: String
) {
    INR("INR", "₹", "Indian Rupee (₹)"),
    USD("USD", "$", "US Dollar ($)"),
    EUR("EUR", "€", "Euro (€)"),
    GBP("GBP", "£", "British Pound (£)"),
    JPY("JPY", "¥", "Japanese Yen (¥)");

    companion object {
        fun fromCode(code: String): SupportedCurrency {
            return entries.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) } ?: INR
        }
    }
}

object CurrencyFormatter {

    fun format(amount: Double, currencyCode: String = "INR", showDecimals: Boolean = false): String {
        val currency = SupportedCurrency.fromCode(currencyCode)
        val isNegative = amount < 0
        val absVal = abs(amount)

        val formattedNumber = if (currency == SupportedCurrency.INR) {
            formatIndianGrouping(absVal, showDecimals)
        } else {
            val pattern = if (showDecimals && currency != SupportedCurrency.JPY) "#,##0.00" else "#,##0"
            DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(absVal)
        }

        val prefix = if (isNegative) "-${currency.symbol}" else currency.symbol
        return "$prefix$formattedNumber"
    }

    fun formatCompact(amount: Double, currencyCode: String = "INR"): String {
        val currency = SupportedCurrency.fromCode(currencyCode)
        val absVal = abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            currency == SupportedCurrency.INR && absVal >= 100_000 -> {
                val lakhs = absVal / 100_000.0
                "$sign${currency.symbol}${String.format(Locale.US, "%.1fL", lakhs)}"
            }
            absVal >= 1_000_000 -> {
                "$sign${currency.symbol}${String.format(Locale.US, "%.1fM", absVal / 1_000_000.0)}"
            }
            absVal >= 10_000 -> {
                format(amount, currencyCode, showDecimals = false)
            }
            else -> format(amount, currencyCode, showDecimals = false)
        }
    }

    private fun formatIndianGrouping(value: Double, showDecimals: Boolean): String {
        val rounded = value.roundToLong()
        val intPart = if (showDecimals) value.toLong() else rounded
        val str = intPart.toString()
        val groupedInt = if (str.length <= 3) {
            str
        } else {
            val lastThree = str.takeLast(3)
            val rest = str.dropLast(3)
            val sb = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                sb.append(rest[i])
                count++
                if (count == 2 && i != 0) {
                    sb.append(',')
                    count = 0
                }
            }
            sb.reverse().append(',').append(lastThree).toString()
        }

        if (!showDecimals) return groupedInt
        val cents = ((value - intPart) * 100).roundToLong().coerceIn(0, 99)
        return if (cents > 0) "$groupedInt.${ cents.toString().padStart(2, '0') }" else groupedInt
    }
}

object DateUtils {
    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val shortMonthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun currentYearMonth(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun currentDateString(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun dateForCurrentMonthDay(yearMonth: String, dayOfMonth: Int): String {
        val maxDays = daysInYearMonth(yearMonth)
        val safeDay = dayOfMonth.coerceIn(1, maxDays)
        return String.format(Locale.US, "%s-%02d", yearMonth, safeDay)
    }

    fun formatYearMonthDisplay(yearMonth: String): String {
        val parts = yearMonth.split("-")
        if (parts.size < 2) return yearMonth
        val year = parts[0].toIntOrNull() ?: return yearMonth
        val monthIndex = (parts[1].toIntOrNull() ?: 1).coerceIn(1, 12) - 1
        return "${monthNames[monthIndex]} $year"
    }

    fun formatShortYearMonth(yearMonth: String): String {
        val parts = yearMonth.split("-")
        if (parts.size < 2) return yearMonth
        val year = parts[0].takeLast(2)
        val monthIndex = (parts[1].toIntOrNull() ?: 1).coerceIn(1, 12) - 1
        return "${shortMonthNames[monthIndex]} '$year"
    }

    fun shiftYearMonth(yearMonth: String, deltaMonths: Int): String {
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 11
        val totalMonths = year * 12 + (month - 1) + deltaMonths
        val newYear = totalMonths / 12
        val newMonth = (totalMonths % 12) + 1
        return String.format(Locale.US, "%04d-%02d", newYear, newMonth)
    }

    fun daysInYearMonth(yearMonth: String): Int {
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 11
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 30
        }
    }

    fun dayOfMonthFromDate(dateString: String): Int {
        val parts = dateString.split("-")
        return parts.getOrNull(2)?.toIntOrNull()?.coerceIn(1, 31) ?: 1
    }

    fun formatFriendlyDateHeader(dateString: String): String {
        val today = currentDateString()
        if (dateString == today) return "Today • $dateString"
        val parts = dateString.split("-")
        if (parts.size < 3) return dateString
        val year = parts[0]
        val monthIdx = (parts[1].toIntOrNull() ?: 1).coerceIn(1, 12) - 1
        val day = parts[2].toIntOrNull() ?: 1
        return "${shortMonthNames[monthIdx]} $day, $year"
    }
}
