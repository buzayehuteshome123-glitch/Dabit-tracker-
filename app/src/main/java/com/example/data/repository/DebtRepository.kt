package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerWithBalance
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionWithCustomer
import com.example.util.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DebtRepository(private val db: AppDatabase, private val context: Context) {
    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()

    val activeCustomersWithBalance: Flow<List<CustomerWithBalance>> = combine(
        customerDao.getActiveCustomers(),
        transactionDao.getAllTransactionsFlow()
    ) { customers, transactions ->
        val txByCustomer = transactions.groupBy { it.customerId }
        customers.map { customer ->
            computeCustomerBalance(customer, txByCustomer[customer.id] ?: emptyList())
        }
    }

    val archivedCustomersWithBalance: Flow<List<CustomerWithBalance>> = combine(
        customerDao.getArchivedCustomers(),
        transactionDao.getAllTransactionsFlow()
    ) { customers, transactions ->
        val txByCustomer = transactions.groupBy { it.customerId }
        customers.map { customer ->
            computeCustomerBalance(customer, txByCustomer[customer.id] ?: emptyList())
        }
    }

    val allTransactionsWithCustomer: Flow<List<TransactionWithCustomer>> = combine(
        transactionDao.getAllTransactionsFlow(),
        customerDao.getActiveCustomers(),
        customerDao.getArchivedCustomers()
    ) { transactions, activeCustomers, archivedCustomers ->
        val customerMap = (activeCustomers + archivedCustomers).associateBy { it.id }
        transactions.map { tx ->
            val customer = customerMap[tx.customerId]
            TransactionWithCustomer(
                transaction = tx,
                customerName = customer?.name ?: "Unknown Customer",
                customerPhone = customer?.phone ?: ""
            )
        }
    }

    fun getCustomerWithBalance(customerId: Long): Flow<CustomerWithBalance?> = combine(
        customerDao.getCustomerByIdFlow(customerId),
        transactionDao.getTransactionsForCustomerFlow(customerId)
    ) { customer, transactions ->
        if (customer == null) null
        else computeCustomerBalance(customer, transactions)
    }

    fun getCustomerTransactions(customerId: Long): Flow<List<TransactionRecord>> {
        return transactionDao.getTransactionsForCustomerFlow(customerId)
    }

    private fun computeCustomerBalance(customer: Customer, txs: List<TransactionRecord>): CustomerWithBalance {
        var totalDebt = BigDecimal.ZERO
        var totalPaid = BigDecimal.ZERO
        var lastTxDate: Long? = null

        for (tx in txs) {
            if (lastTxDate == null || tx.transactionDate > lastTxDate) {
                lastTxDate = tx.transactionDate
            }
            val amount = BigDecimal.valueOf(tx.amount).setScale(2, RoundingMode.HALF_UP)
            if (tx.type.equals("DEBT", ignoreCase = true)) {
                totalDebt = totalDebt.add(amount)
            } else if (tx.type.equals("PAYMENT", ignoreCase = true)) {
                totalPaid = totalPaid.add(amount)
            }
        }

        val balance = totalDebt.subtract(totalPaid).setScale(2, RoundingMode.HALF_UP)
        val finalBalance = if (balance < BigDecimal.ZERO) 0.0 else balance.toDouble()

        return CustomerWithBalance(
            customer = customer,
            totalDebt = totalDebt.toDouble(),
            totalPaid = totalPaid.toDouble(),
            balance = finalBalance,
            transactionCount = txs.size,
            lastTransactionDate = lastTxDate
        )
    }

    suspend fun getCustomerCurrentBalance(customerId: Long): Double = withContext(Dispatchers.IO) {
        val txs = transactionDao.getTransactionsForCustomer(customerId)
        var totalDebt = BigDecimal.ZERO
        var totalPaid = BigDecimal.ZERO
        for (tx in txs) {
            val amount = BigDecimal.valueOf(tx.amount).setScale(2, RoundingMode.HALF_UP)
            if (tx.type.equals("DEBT", ignoreCase = true)) {
                totalDebt = totalDebt.add(amount)
            } else {
                totalPaid = totalPaid.add(amount)
            }
        }
        val diff = totalDebt.subtract(totalPaid)
        if (diff < BigDecimal.ZERO) 0.0 else diff.toDouble()
    }

    suspend fun insertCustomer(name: String, phone: String, address: String, notes: String): Long = withContext(Dispatchers.IO) {
        val customer = Customer(
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            notes = notes.trim()
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun archiveCustomer(customerId: Long) = withContext(Dispatchers.IO) {
        customerDao.archiveCustomer(customerId)
    }

    suspend fun unarchiveCustomer(customerId: Long) = withContext(Dispatchers.IO) {
        customerDao.unarchiveCustomer(customerId)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun addDebt(
        customerId: Long,
        amount: Double,
        description: String,
        date: Long
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (amount <= 0.0) {
            return@withContext Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        }
        val tx = TransactionRecord(
            customerId = customerId,
            type = "DEBT",
            amount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toDouble(),
            paymentMethod = "OTHER",
            description = description.trim(),
            transactionDate = date,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        Result.success(id)
    }

    suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        description: String,
        date: Long
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (amount <= 0.0) {
            return@withContext Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        }
        val currentBalance = getCustomerCurrentBalance(customerId)
        val currentBalanceBd = BigDecimal.valueOf(currentBalance).setScale(2, RoundingMode.HALF_UP)
        val amountBd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)

        if (amountBd > currentBalanceBd) {
            return@withContext Result.failure(
                IllegalArgumentException("Payment cannot be greater than the outstanding balance.")
            )
        }

        val tx = TransactionRecord(
            customerId = customerId,
            type = "PAYMENT",
            amount = amountBd.toDouble(),
            paymentMethod = paymentMethod.uppercase(),
            description = description.trim(),
            transactionDate = date,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        Result.success(id)
    }

    suspend fun updateTransaction(
        transactionId: Long,
        newAmount: Double,
        newMethod: String,
        newDescription: String,
        newDate: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (newAmount <= 0.0) {
            return@withContext Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        }
        val existing = transactionDao.getTransactionById(transactionId)
            ?: return@withContext Result.failure(IllegalArgumentException("Transaction not found"))

        val newAmountBd = BigDecimal.valueOf(newAmount).setScale(2, RoundingMode.HALF_UP)

        // If editing a payment, check if new amount exceeds the customer balance excluding this payment
        if (existing.type.equals("PAYMENT", ignoreCase = true)) {
            val allTxs = transactionDao.getTransactionsForCustomer(existing.customerId)
            var totalDebt = BigDecimal.ZERO
            var otherPayments = BigDecimal.ZERO
            for (tx in allTxs) {
                val a = BigDecimal.valueOf(tx.amount).setScale(2, RoundingMode.HALF_UP)
                if (tx.type.equals("DEBT", ignoreCase = true)) {
                    totalDebt = totalDebt.add(a)
                } else if (tx.id != transactionId) {
                    otherPayments = otherPayments.add(a)
                }
            }
            val maxPayable = totalDebt.subtract(otherPayments)
            if (newAmountBd > maxPayable) {
                return@withContext Result.failure(
                    IllegalArgumentException("Payment cannot be greater than the outstanding balance.")
                )
            }
        }

        val updated = existing.copy(
            amount = newAmountBd.toDouble(),
            paymentMethod = newMethod.uppercase(),
            description = newDescription.trim(),
            transactionDate = newDate,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.updateTransaction(updated)
        Result.success(Unit)
    }

    suspend fun deleteTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(transactionId)
    }

    suspend fun exportCsvUri(): Uri = withContext(Dispatchers.IO) {
        val customers = customerDao.getAllCustomersList()
        val customerMap = customers.associateBy { it.id }
        val transactions = transactionDao.getAllTransactionsList()

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "debt_tracker_export_${System.currentTimeMillis()}.csv")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility with Amharic
        sb.append('\uFEFF')
        sb.append("Customer Name,Phone,Transaction Type,Amount (ETB),Payment Method,Description,Date\n")

        for (tx in transactions) {
            val c = customerMap[tx.customerId]
            val cName = escapeCsv(c?.name ?: "Unknown")
            val cPhone = escapeCsv(c?.phone ?: "")
            val type = tx.type
            val amount = String.format(Locale.US, "%.2f", tx.amount)
            val method = tx.paymentMethod
            val desc = escapeCsv(tx.description)
            val date = sdf.format(Date(tx.transactionDate))
            sb.append("$cName,$cPhone,$type,$amount,$method,$desc,$date\n")
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }

    suspend fun exportJsonData(): String = withContext(Dispatchers.IO) {
        val customers = customerDao.getAllCustomersList()
        val transactions = transactionDao.getAllTransactionsList()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())

        val custArray = JSONArray()
        for (c in customers) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("notes", c.notes)
                put("created_at", c.createdAt)
                put("updated_at", c.updatedAt)
                put("archived_at", c.archivedAt ?: JSONObject.NULL)
            }
            custArray.put(obj)
        }
        root.put("customers", custArray)

        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("customer_id", tx.customerId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("payment_method", tx.paymentMethod)
                put("description", tx.description)
                put("transaction_date", tx.transactionDate)
                put("created_at", tx.createdAt)
                put("updated_at", tx.updatedAt)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        root.toString(2)
    }

    suspend fun restoreJsonData(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val custArray = root.getJSONArray("customers")
            val txArray = root.getJSONArray("transactions")

            // Clear old data
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()

            var count = 0
            for (i in 0 until custArray.length()) {
                val obj = custArray.getJSONObject(i)
                val c = Customer(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updated_at", System.currentTimeMillis()),
                    archivedAt = if (obj.isNull("archived_at")) null else obj.optLong("archived_at")
                )
                customerDao.insertCustomer(c)
                count++
            }

            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val tx = TransactionRecord(
                    id = obj.optLong("id", 0L),
                    customerId = obj.getLong("customer_id"),
                    type = obj.getString("type"),
                    amount = obj.getDouble("amount"),
                    paymentMethod = obj.optString("payment_method", "CASH"),
                    description = obj.optString("description", ""),
                    transactionDate = obj.optLong("transaction_date", System.currentTimeMillis()),
                    createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updated_at", System.currentTimeMillis())
                )
                transactionDao.insertTransaction(tx)
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
