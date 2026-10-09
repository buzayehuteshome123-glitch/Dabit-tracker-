package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE archived_at IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun getActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE archived_at IS NOT NULL ORDER BY archived_at DESC")
    fun getArchivedCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllCustomersList(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET archived_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun archiveCustomer(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET archived_at = NULL, updated_at = :timestamp WHERE id = :id")
    suspend fun unarchiveCustomer(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
