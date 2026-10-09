package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val integerFormat = DecimalFormat("#,##0", symbols)
    private val decimalFormat = DecimalFormat("#,##0.00", symbols)

    fun formatBirr(amount: Double): String {
        val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val formatted = if (bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            integerFormat.format(bd)
        } else {
            decimalFormat.format(bd)
        }
        return "$formatted ብር"
    }

    fun parseAmount(input: String): Double? {
        val cleaned = input.trim().replace(",", "").replace(" ", "").replace("ብር", "")
        if (cleaned.isEmpty()) return null
        return try {
            val bd = BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP)
            if (bd > BigDecimal.ZERO) bd.toDouble() else null
        } catch (e: Exception) {
            null
        }
    }

    fun calculateBalance(debts: List<Double>, payments: List<Double>): Double {
        var totalDebt = BigDecimal.ZERO
        for (d in debts) {
            totalDebt = totalDebt.add(BigDecimal.valueOf(d))
        }
        var totalPaid = BigDecimal.ZERO
        for (p in payments) {
            totalPaid = totalPaid.add(BigDecimal.valueOf(p))
        }
        val diff = totalDebt.subtract(totalPaid).setScale(2, RoundingMode.HALF_UP)
        return if (diff < BigDecimal.ZERO) 0.0 else diff.toDouble()
    }
}
