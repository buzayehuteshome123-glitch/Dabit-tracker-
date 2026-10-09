package com.example.data.model

data class TransactionWithCustomer(
    val transaction: TransactionRecord,
    val customerName: String,
    val customerPhone: String = ""
)
