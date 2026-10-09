package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerWithBalance
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionWithCustomer
import com.example.data.repository.DebtRepository
import com.example.util.AppLanguage
import com.example.util.CurrencyUtils
import com.example.util.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

enum class CustomerFilter {
    ALL,
    WITH_DEBT,
    PAID
}

enum class TransactionFilter {
    ALL,
    DEBT,
    PAYMENT
}

data class DashboardMetrics(
    val totalOutstanding: Double = 0.0,
    val totalCustomers: Int = 0,
    val totalDebtGiven: Double = 0.0,
    val totalPaymentsReceived: Double = 0.0
)

class DebtViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = DebtRepository(database, application)
    val languageManager = LanguageManager.getInstance(application)

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Customer search & filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _customerFilter = MutableStateFlow(CustomerFilter.ALL)
    val customerFilter: StateFlow<CustomerFilter> = _customerFilter.asStateFlow()

    private val _transactionFilter = MutableStateFlow(TransactionFilter.ALL)
    val transactionFilter: StateFlow<TransactionFilter> = _transactionFilter.asStateFlow()

    // Selected customer for detail screen
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    // Active customers with computed balance
    val allActiveCustomers: StateFlow<List<CustomerWithBalance>> = repository.activeCustomersWithBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArchivedCustomers: StateFlow<List<CustomerWithBalance>> = repository.archivedCustomersWithBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered customers based on search query and status filter
    val filteredCustomers: StateFlow<List<CustomerWithBalance>> = combine(
        allActiveCustomers,
        _searchQuery,
        _customerFilter
    ) { customers, query, filter ->
        customers.filter { c ->
            val matchesQuery = query.isBlank() ||
                    c.customer.name.contains(query, ignoreCase = true) ||
                    c.customer.phone.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.WITH_DEBT -> c.isOutstanding
                CustomerFilter.PAID -> c.isPaid
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transactions with customer info
    val allTransactions: StateFlow<List<TransactionWithCustomer>> = repository.allTransactionsWithCustomer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for Activity screen
    val filteredTransactions: StateFlow<List<TransactionWithCustomer>> = combine(
        allTransactions,
        _transactionFilter
    ) { list, filter ->
        when (filter) {
            TransactionFilter.ALL -> list
            TransactionFilter.DEBT -> list.filter { it.transaction.type.equals("DEBT", ignoreCase = true) }
            TransactionFilter.PAYMENT -> list.filter { it.transaction.type.equals("PAYMENT", ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard metrics calculation
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allActiveCustomers,
        allTransactions
    ) { customers, transactions ->
        var totalOutstandingBd = BigDecimal.ZERO
        for (c in customers) {
            totalOutstandingBd = totalOutstandingBd.add(BigDecimal.valueOf(c.balance))
        }

        var totalDebtGivenBd = BigDecimal.ZERO
        var totalPaymentsReceivedBd = BigDecimal.ZERO

        for (tx in transactions) {
            val amount = BigDecimal.valueOf(tx.transaction.amount).setScale(2, RoundingMode.HALF_UP)
            if (tx.transaction.type.equals("DEBT", ignoreCase = true)) {
                totalDebtGivenBd = totalDebtGivenBd.add(amount)
            } else if (tx.transaction.type.equals("PAYMENT", ignoreCase = true)) {
                totalPaymentsReceivedBd = totalPaymentsReceivedBd.add(amount)
            }
        }

        DashboardMetrics(
            totalOutstanding = totalOutstandingBd.setScale(2, RoundingMode.HALF_UP).toDouble(),
            totalCustomers = customers.size,
            totalDebtGiven = totalDebtGivenBd.setScale(2, RoundingMode.HALF_UP).toDouble(),
            totalPaymentsReceived = totalPaymentsReceivedBd.setScale(2, RoundingMode.HALF_UP).toDouble()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Selected customer detail flow
    val selectedCustomerDetail: StateFlow<CustomerWithBalance?> = combine(
        allActiveCustomers,
        allArchivedCustomers,
        _selectedCustomerId
    ) { active, archived, id ->
        if (id == null) null
        else (active + archived).find { it.customer.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected customer's transactions
    val selectedCustomerTransactions: StateFlow<List<TransactionRecord>> = combine(
        allTransactions,
        _selectedCustomerId
    ) { txs, id ->
        if (id == null) emptyList()
        else txs.filter { it.transaction.customerId == id }.map { it.transaction }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transient UI status / alerts
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _exportedUri = MutableStateFlow<Uri?>(null)
    val exportedUri: StateFlow<Uri?> = _exportedUri.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _onboardingCompleted.value = true
    }

    fun restartOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", false).apply()
        _onboardingCompleted.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCustomerFilter(filter: CustomerFilter) {
        _customerFilter.value = filter
    }

    fun setTransactionFilter(filter: TransactionFilter) {
        _transactionFilter.value = filter
    }

    fun selectCustomer(customerId: Long?) {
        _selectedCustomerId.value = customerId
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun clearExportedUri() {
        _exportedUri.value = null
    }

    fun setLanguage(lang: AppLanguage) {
        languageManager.setLanguage(lang)
    }

    fun toggleLanguage() {
        languageManager.toggleLanguage()
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(languageManager.getString("error_name_required"))
            return
        }
        viewModelScope.launch {
            try {
                val id = repository.insertCustomer(name, phone, address, notes)
                onSuccess(id)
            } catch (e: Exception) {
                onError(languageManager.getString("error_generic"))
            }
        }
    }

    fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (customer.name.isBlank()) {
            onError(languageManager.getString("error_name_required"))
            return
        }
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                onSuccess()
            } catch (e: Exception) {
                onError(languageManager.getString("error_generic"))
            }
        }
    }

    fun archiveCustomer(customerId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.archiveCustomer(customerId)
            onComplete()
        }
    }

    fun unarchiveCustomer(customerId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.unarchiveCustomer(customerId)
            onComplete()
        }
    }

    fun deleteCustomerPermanently(customer: Customer, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            onComplete()
        }
    }

    fun addDebt(
        customerId: Long,
        amount: Double,
        description: String,
        date: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0.0) {
            onError(languageManager.getString("error_amount_positive"))
            return
        }
        viewModelScope.launch {
            val result = repository.addDebt(customerId, amount, description, date)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: languageManager.getString("error_generic"))
            }
        }
    }

    fun recordPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        description: String,
        date: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0.0) {
            onError(languageManager.getString("error_amount_positive"))
            return
        }
        viewModelScope.launch {
            val result = repository.recordPayment(customerId, amount, paymentMethod, description, date)
            if (result.isSuccess) {
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message
                if (msg != null && msg.contains("greater than the outstanding balance")) {
                    onError(languageManager.getString("error_payment_exceeds"))
                } else {
                    onError(msg ?: languageManager.getString("error_generic"))
                }
            }
        }
    }

    fun updateTransaction(
        transactionId: Long,
        amount: Double,
        method: String,
        description: String,
        date: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0.0) {
            onError(languageManager.getString("error_amount_positive"))
            return
        }
        viewModelScope.launch {
            val result = repository.updateTransaction(transactionId, amount, method, description, date)
            if (result.isSuccess) {
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message
                if (msg != null && msg.contains("greater than the outstanding balance")) {
                    onError(languageManager.getString("error_payment_exceeds"))
                } else {
                    onError(msg ?: languageManager.getString("error_generic"))
                }
            }
        }
    }

    fun deleteTransaction(transactionId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            onComplete()
        }
    }

    fun exportCsv(onReady: (Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uri = repository.exportCsvUri()
                _exportedUri.value = uri
                onReady(uri)
            } catch (e: Exception) {
                onError(languageManager.getString("error_generic"))
            }
        }
    }

    fun backupJson(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportJsonData()
                onSuccess(json)
            } catch (e: Exception) {
                onError(languageManager.getString("error_generic"))
            }
        }
    }

    fun restoreJson(jsonString: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreJsonData(jsonString)
            if (result.isSuccess) {
                onSuccess(result.getOrDefault(0))
            } else {
                onError(languageManager.getString("error_generic"))
            }
        }
    }
}
