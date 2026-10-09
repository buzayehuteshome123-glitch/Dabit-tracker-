package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionRecord
import com.example.ui.dialogs.ConfirmDeleteTransactionDialog
import com.example.ui.viewmodel.DebtViewModel
import com.example.ui.viewmodel.TransactionFilter

@Composable
fun ActivityScreen(
    viewModel: DebtViewModel,
    onOpenCustomer: (Long) -> Unit,
    onEditTransaction: (TransactionRecord, customerName: String, customerBalance: Double) -> Unit
) {
    val languageManager = viewModel.languageManager
    val activeFilter by viewModel.transactionFilter.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val activeCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()

    var transactionToDelete by remember { mutableStateOf<TransactionRecord?>(null) }

    if (transactionToDelete != null) {
        ConfirmDeleteTransactionDialog(
            languageManager = languageManager,
            onDismiss = { transactionToDelete = null },
            onConfirm = {
                viewModel.deleteTransaction(transactionToDelete!!.id) {
                    transactionToDelete = null
                }
            }
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("activity_screen")
        ) {
            // Header & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = languageManager.getString("nav_activity"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == TransactionFilter.ALL,
                        onClick = { viewModel.setTransactionFilter(TransactionFilter.ALL) },
                        label = { Text(languageManager.getString("filter_all")) },
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = activeFilter == TransactionFilter.DEBT,
                        onClick = { viewModel.setTransactionFilter(TransactionFilter.DEBT) },
                        label = { Text(languageManager.getString("type_debt")) },
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = activeFilter == TransactionFilter.PAYMENT,
                        onClick = { viewModel.setTransactionFilter(TransactionFilter.PAYMENT) },
                        label = { Text(languageManager.getString("type_payment")) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = languageManager.getString("no_transactions_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = languageManager.getString("no_transactions_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { item ->
                        CustomerTransactionRow(
                            transaction = item.transaction,
                            languageManager = languageManager,
                            onEdit = {
                                val c = activeCustomers.find { it.customer.id == item.transaction.customerId }
                                onEditTransaction(
                                    item.transaction,
                                    item.customerName,
                                    c?.balance ?: 0.0
                                )
                            },
                            onDelete = { transactionToDelete = item.transaction }
                        )
                    }
                }
            }
        }
    }
}
