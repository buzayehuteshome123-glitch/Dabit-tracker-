package com.example.data.model

data class CustomerWithBalance(
    val customer: Customer,
    val totalDebt: Double,
    val totalPaid: Double,
    val balance: Double,
    val transactionCount: Int = 0,
    val lastTransactionDate: Long? = null
) {
    val isPaid: Boolean
        get() = balance <= 0.0001

    val isOutstanding: Boolean
        get() = balance > 0.0001
}
