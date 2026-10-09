package com.example.ui.dialogs

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Customer
import com.example.data.model.CustomerWithBalance
import com.example.data.model.TransactionRecord
import com.example.ui.theme.DebtRed
import com.example.ui.theme.PaidGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.LanguageManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

@Composable
fun AddEditCustomerDialog(
    initialCustomer: Customer? = null,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialCustomer != null
    val title = if (isEditing) languageManager.getString("edit_customer") else languageManager.getString("add_customer")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("${languageManager.getString("customer_name")} *") },
                    placeholder = { Text("e.g. Abebe Kebede") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("${languageManager.getString("phone_number")} ${languageManager.getString("optional_label")}") },
                    placeholder = { Text("0911 XXX XXX") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_phone_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("${languageManager.getString("address")} ${languageManager.getString("optional_label")}") },
                    placeholder = { Text("e.g. Merkato, Addis Ababa") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_address_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("${languageManager.getString("notes")} ${languageManager.getString("optional_label")}") },
                    placeholder = { Text("e.g. Regular shop customer") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_notes_input"),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(languageManager.getString("cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                errorMessage = languageManager.getString("error_name_required")
                            } else {
                                onSave(name.trim(), phone.trim(), address.trim(), notes.trim())
                            }
                        },
                        modifier = Modifier.testTag("save_customer_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(languageManager.getString("save_customer"))
                    }
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    customers: List<CustomerWithBalance>,
    preSelectedCustomerId: Long? = null,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onSave: (customerId: Long, amount: Double, description: String, date: Long) -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.customer.id == preSelectedCustomerId } ?: customers.firstOrNull())
    }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val parsedAmount = CurrencyUtils.parseAmount(amountText) ?: 0.0
    val currentBalance = selectedCustomer?.balance ?: 0.0
    val newBalance = currentBalance + parsedAmount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.getString("add_debt"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Customer Selector
                if (preSelectedCustomerId == null || customers.size > 1) {
                    Column {
                        Text(
                            text = "${languageManager.getString("select_customer")} *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCustomerPicker = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = selectedCustomer?.customer?.name ?: languageManager.getString("select_customer"),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = showCustomerPicker,
                                onDismissRequest = { showCustomerPicker = false }
                            ) {
                                customers.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(item.customer.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${languageManager.getString("current_balance")}: ${CurrencyUtils.formatBirr(item.balance)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (item.isOutstanding) DebtRed else PaidGreen
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = item
                                            showCustomerPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedCustomer != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(selectedCustomer!!.customer.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${languageManager.getString("current_balance")}: ${CurrencyUtils.formatBirr(selectedCustomer!!.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedCustomer!!.isOutstanding) DebtRed else PaidGreen
                                )
                            }
                        }
                    }
                }

                // Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${languageManager.getString("amount")} (ብር) *") },
                    placeholder = { Text("e.g. 1500") },
                    modifier = Modifier.fillMaxWidth().testTag("debt_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("${languageManager.getString("description")} ${languageManager.getString("optional_label")}") },
                    placeholder = { Text("e.g. Building materials, groceries") },
                    modifier = Modifier.fillMaxWidth().testTag("debt_description_input"),
                    singleLine = true
                )

                // Date selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    selectedDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${languageManager.getString("date")}: ${DateUtils.formatDate(selectedDate, languageManager.isAmharic)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = languageManager.getString("edit"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Live balance calculation card
                if (selectedCustomer != null && parsedAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(languageManager.getString("previous_balance"), style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBirr(currentBalance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(languageManager.getString("new_balance"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(CurrencyUtils.formatBirr(newBalance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DebtRed)
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(languageManager.getString("cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                errorMessage = languageManager.getString("error_customer_required")
                                return@Button
                            }
                            val amount = CurrencyUtils.parseAmount(amountText)
                            if (amount == null || amount <= 0.0) {
                                errorMessage = languageManager.getString("error_amount_positive")
                                return@Button
                            }
                            onSave(selectedCustomer!!.customer.id, amount, description, selectedDate)
                        },
                        modifier = Modifier.testTag("save_debt_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(languageManager.getString("save_debt"))
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    customers: List<CustomerWithBalance>,
    preSelectedCustomerId: Long? = null,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onSave: (customerId: Long, amount: Double, paymentMethod: String, note: String, date: Long) -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember {
        mutableStateOf(
            customers.find { it.customer.id == preSelectedCustomerId }
                ?: customers.firstOrNull { it.isOutstanding }
                ?: customers.firstOrNull()
        )
    }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var noteText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val parsedAmount = CurrencyUtils.parseAmount(amountText) ?: 0.0
    val currentBalance = selectedCustomer?.balance ?: 0.0
    val newBalance = (currentBalance - parsedAmount).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.getString("record_payment"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Customer Selector
                if (preSelectedCustomerId == null || customers.size > 1) {
                    Column {
                        Text(
                            text = "${languageManager.getString("select_customer")} *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCustomerPicker = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = selectedCustomer?.customer?.name ?: languageManager.getString("select_customer"),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = showCustomerPicker,
                                onDismissRequest = { showCustomerPicker = false }
                            ) {
                                customers.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(item.customer.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${languageManager.getString("current_balance")}: ${CurrencyUtils.formatBirr(item.balance)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (item.isOutstanding) DebtRed else PaidGreen
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = item
                                            showCustomerPicker = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedCustomer != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(selectedCustomer!!.customer.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${languageManager.getString("current_balance")}: ${CurrencyUtils.formatBirr(selectedCustomer!!.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedCustomer!!.isOutstanding) DebtRed else PaidGreen
                                )
                            }
                        }
                    }
                }

                // Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${languageManager.getString("amount")} (ብር) *") },
                    placeholder = { Text("e.g. 1000") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Payment Method Chips (Cash / Bank / Other)
                Column {
                    Text(
                        text = languageManager.getString("payment_method"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "CASH" to languageManager.getString("method_cash"),
                            "BANK" to languageManager.getString("method_bank"),
                            "OTHER" to languageManager.getString("method_other")
                        ).forEach { (code, label) ->
                            val isSelected = paymentMethod == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { paymentMethod = code }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Note field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("${languageManager.getString("notes")} ${languageManager.getString("optional_label")}") },
                    placeholder = { Text("e.g. Partial payment, CBE transfer") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_note_input"),
                    singleLine = true
                )

                // Date selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    selectedDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${languageManager.getString("date")}: ${DateUtils.formatDate(selectedDate, languageManager.isAmharic)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = languageManager.getString("edit"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Balance preview
                if (selectedCustomer != null && parsedAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(languageManager.getString("current_balance"), style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBirr(currentBalance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(languageManager.getString("new_balance"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    CurrencyUtils.formatBirr(newBalance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newBalance <= 0) PaidGreen else DebtRed
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(languageManager.getString("cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                errorMessage = languageManager.getString("error_customer_required")
                                return@Button
                            }
                            val amount = CurrencyUtils.parseAmount(amountText)
                            if (amount == null || amount <= 0.0) {
                                errorMessage = languageManager.getString("error_amount_positive")
                                return@Button
                            }

                            // Payment validation: Never allow payment greater than customer's outstanding balance
                            val balanceBd = BigDecimal.valueOf(selectedCustomer!!.balance).setScale(2, RoundingMode.HALF_UP)
                            val amountBd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
                            if (amountBd > balanceBd) {
                                errorMessage = languageManager.getString("error_payment_exceeds")
                                return@Button
                            }

                            onSave(selectedCustomer!!.customer.id, amount, paymentMethod, noteText, selectedDate)
                        },
                        modifier = Modifier.testTag("save_payment_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(languageManager.getString("save_payment"))
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionRecord,
    customerName: String,
    customerBalance: Double,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onSave: (amount: Double, method: String, description: String, date: Long) -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var paymentMethod by remember { mutableStateOf(transaction.paymentMethod) }
    var description by remember { mutableStateOf(transaction.description) }
    var selectedDate by remember { mutableStateOf(transaction.transactionDate) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isDebt = transaction.type.equals("DEBT", ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${languageManager.getString("edit")} ${if (isDebt) languageManager.getString("type_debt") else languageManager.getString("type_payment")}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${languageManager.getString("amount")} (ብር) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                if (!isDebt) {
                    Column {
                        Text(
                            text = languageManager.getString("payment_method"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "CASH" to languageManager.getString("method_cash"),
                                "BANK" to languageManager.getString("method_bank"),
                                "OTHER" to languageManager.getString("method_other")
                            ).forEach { (code, label) ->
                                val isSelected = paymentMethod.equals(code, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { paymentMethod = code }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("${languageManager.getString("description")} ${languageManager.getString("optional_label")}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    selectedDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${languageManager.getString("date")}: ${DateUtils.formatDate(selectedDate, languageManager.isAmharic)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = languageManager.getString("edit"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(languageManager.getString("cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = CurrencyUtils.parseAmount(amountText)
                            if (amount == null || amount <= 0.0) {
                                errorMessage = languageManager.getString("error_amount_positive")
                                return@Button
                            }
                            onSave(amount, paymentMethod, description, selectedDate)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(languageManager.getString("save"))
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteTransactionDialog(
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = languageManager.getString("delete_transaction_title"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = languageManager.getString("delete_transaction_desc"),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(languageManager.getString("delete"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(languageManager.getString("cancel"))
            }
        }
    )
}

@Composable
fun ConfirmArchiveCustomerDialog(
    customerName: String,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = languageManager.getString("archive_customer_title"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = customerName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = languageManager.getString("archive_customer_desc"),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(languageManager.getString("archive"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(languageManager.getString("cancel"))
            }
        }
    )
}

@Composable
fun ArchivedCustomersDialog(
    archivedCustomers: List<CustomerWithBalance>,
    languageManager: LanguageManager,
    onDismiss: () -> Unit,
    onRestore: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.getString("archived_customers_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (archivedCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (languageManager.isAmharic) "ምንም የተቀመጡ ደንበኞች የሉም" else "No archived customers",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archivedCustomers) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.customer.name, fontWeight = FontWeight.Bold)
                                        if (item.customer.phone.isNotEmpty()) {
                                            Text(item.customer.phone, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            "${languageManager.getString("current_balance")}: ${CurrencyUtils.formatBirr(item.balance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (item.isOutstanding) DebtRed else PaidGreen
                                        )
                                    }
                                    Button(
                                        onClick = { onRestore(item.customer.id) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(languageManager.getString("unarchive_customer"))
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(languageManager.getString("cancel"))
                }
            }
        }
    }
}
