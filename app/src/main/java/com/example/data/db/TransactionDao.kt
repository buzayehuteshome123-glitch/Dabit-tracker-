package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transaction_date DESC, created_at DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions ORDER BY transaction_date DESC, created_at DESC")
    suspend fun getAllTransactionsList(): List<TransactionRecord>

    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY transaction_date DESC, created_at DESC")
    fun getTransactionsForCustomerFlow(customerId: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY transaction_date DESC, created_at DESC")
    suspend fun getTransactionsForCustomer(customerId: Long): List<TransactionRecord>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionRecord)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
