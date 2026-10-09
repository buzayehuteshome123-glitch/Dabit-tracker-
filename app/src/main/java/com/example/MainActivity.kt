package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.TransactionRecord
import com.example.ui.dialogs.AddDebtDialog
import com.example.ui.dialogs.AddEditCustomerDialog
import com.example.ui.dialogs.EditTransactionDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.CustomerProfileScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DebtViewModel

enum class MainTab(val route: String, val iconKey: String) {
    HOME("home", "nav_home"),
    CUSTOMERS("customers", "nav_customers"),
    ACTIVITY("activity", "nav_activity"),
    SETTINGS("settings", "nav_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: DebtViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DebtApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DebtApp(viewModel: DebtViewModel) {
    val context = LocalContext.current
    val languageManager = viewModel.languageManager
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val activeCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var forceShowOnboarding by remember { mutableStateOf(false) }

    // Dialog state
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var addDebtCustomerId by remember { mutableStateOf<Long?>(null) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var recordPaymentCustomerId by remember { mutableStateOf<Long?>(null) }

    data class EditTxState(val tx: TransactionRecord, val name: String, val balance: Double)
    var editTransactionState by remember { mutableStateOf<EditTxState?>(null) }

    // Dialogs
    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            languageManager = languageManager,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address, notes ->
                viewModel.addCustomer(
                    name = name,
                    phone = phone,
                    address = address,
                    notes = notes,
                    onSuccess = {
                        showAddCustomerDialog = false
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (customerToEdit != null) {
        AddEditCustomerDialog(
            initialCustomer = customerToEdit,
            languageManager = languageManager,
            onDismiss = { customerToEdit = null },
            onSave = { name, phone, address, notes ->
                val updated = customerToEdit!!.copy(
                    name = name,
                    phone = phone,
                    address = address,
                    notes = notes
                )
                viewModel.updateCustomer(
                    customer = updated,
                    onSuccess = { customerToEdit = null },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            customers = activeCustomers,
            preSelectedCustomerId = addDebtCustomerId,
            languageManager = languageManager,
            onDismiss = { showAddDebtDialog = false },
            onSave = { customerId, amount, description, date ->
                viewModel.addDebt(
                    customerId = customerId,
                    amount = amount,
                    description = description,
                    date = date,
                    onSuccess = { showAddDebtDialog = false },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            customers = activeCustomers,
            preSelectedCustomerId = recordPaymentCustomerId,
            languageManager = languageManager,
            onDismiss = { showRecordPaymentDialog = false },
            onSave = { customerId, amount, paymentMethod, note, date ->
                viewModel.recordPayment(
                    customerId = customerId,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    description = note,
                    date = date,
                    onSuccess = { showRecordPaymentDialog = false },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (editTransactionState != null) {
        val st = editTransactionState!!
        EditTransactionDialog(
            transaction = st.tx,
            customerName = st.name,
            customerBalance = st.balance,
            languageManager = languageManager,
            onDismiss = { editTransactionState = null },
            onSave = { amount, method, description, date ->
                viewModel.updateTransaction(
                    transactionId = st.tx.id,
                    amount = amount,
                    method = method,
                    description = description,
                    date = date,
                    onSuccess = { editTransactionState = null },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Onboarding Screen
    if (!onboardingCompleted || forceShowOnboarding) {
        OnboardingScreen(
            languageManager = languageManager,
            onFinish = {
                viewModel.completeOnboarding()
                forceShowOnboarding = false
            }
        )
        return
    }

    // Handle back button when inside customer profile
    if (selectedCustomerId != null) {
        BackHandler {
            viewModel.selectCustomer(null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (selectedCustomerId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val icon = when (tab) {
                            MainTab.HOME -> Icons.Default.Home
                            MainTab.CUSTOMERS -> Icons.Default.People
                            MainTab.ACTIVITY -> Icons.Default.Receipt
                            MainTab.SETTINGS -> Icons.Default.Settings
                        }
                        val tag = when (tab) {
                            MainTab.HOME -> "bottom_nav_home"
                            MainTab.CUSTOMERS -> "bottom_nav_customers"
                            MainTab.ACTIVITY -> "bottom_nav_activity"
                            MainTab.SETTINGS -> "bottom_nav_settings"
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                viewModel.selectCustomer(null)
                            },
                            icon = { Icon(icon, contentDescription = languageManager.getString(tab.iconKey)) },
                            label = {
                                Text(
                                    text = languageManager.getString(tab.iconKey),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedCustomerId != null) {
                CustomerProfileScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.selectCustomer(null) },
                    onAddDebtClick = { id ->
                        addDebtCustomerId = id
                        showAddDebtDialog = true
                    },
                    onRecordPaymentClick = { id ->
                        recordPaymentCustomerId = id
                        showRecordPaymentDialog = true
                    },
                    onEditCustomerClick = { id ->
                        val c = activeCustomers.find { it.customer.id == id }?.customer
                        if (c != null) {
                            customerToEdit = c
                        }
                    },
                    onEditTransactionClick = { tx ->
                        val c = activeCustomers.find { it.customer.id == tx.customerId }
                        editTransactionState = EditTxState(
                            tx = tx,
                            name = c?.customer?.name ?: "Customer",
                            balance = c?.balance ?: 0.0
                        )
                    }
                )
            } else {
                when (currentTab) {
                    MainTab.HOME -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToCustomers = { currentTab = MainTab.CUSTOMERS },
                            onNavigateToActivity = { currentTab = MainTab.ACTIVITY },
                            onOpenCustomer = { id -> viewModel.selectCustomer(id) },
                            onAddDebtClick = {
                                addDebtCustomerId = null
                                showAddDebtDialog = true
                            },
                            onRecordPaymentClick = {
                                recordPaymentCustomerId = null
                                showRecordPaymentDialog = true
                            }
                        )
                    }

                    MainTab.CUSTOMERS -> {
                        CustomerListScreen(
                            viewModel = viewModel,
                            onOpenCustomer = { id -> viewModel.selectCustomer(id) },
                            onAddCustomerClick = { showAddCustomerDialog = true },
                            onQuickAddDebtClick = { id ->
                                addDebtCustomerId = id
                                showAddDebtDialog = true
                            },
                            onQuickPayClick = { id ->
                                recordPaymentCustomerId = id
                                showRecordPaymentDialog = true
                            }
                        )
                    }

                    MainTab.ACTIVITY -> {
                        ActivityScreen(
                            viewModel = viewModel,
                            onOpenCustomer = { id -> viewModel.selectCustomer(id) },
                            onEditTransaction = { tx, name, balance ->
                                editTransactionState = EditTxState(tx, name, balance)
                            }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onShowOnboarding = { forceShowOnboarding = true }
                        )
                    }
                }
            }
        }
    }
}
