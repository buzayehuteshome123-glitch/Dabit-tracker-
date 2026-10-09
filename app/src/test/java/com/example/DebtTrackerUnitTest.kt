package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Customer
import com.example.data.repository.DebtRepository
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DebtTrackerUnitTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DebtRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DebtRepository(db, context)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testCurrencyFormatting() {
        assertEquals("1,500 ብር", CurrencyUtils.formatBirr(1500.0))
        assertEquals("1,000,000 ብር", CurrencyUtils.formatBirr(1000000.0))
        assertEquals("125,500 ብር", CurrencyUtils.formatBirr(125500.0))
        assertEquals("25.50 ብር", CurrencyUtils.formatBirr(25.5))
        assertEquals("0 ብር", CurrencyUtils.formatBirr(0.0))
    }

    @Test
    fun testCurrencyParsing() {
        assertEquals(1500.0, CurrencyUtils.parseAmount("1,500") ?: 0.0, 0.001)
        assertEquals(250000.0, CurrencyUtils.parseAmount("250,000 ብር") ?: 0.0, 0.001)
        assertEquals(45.5, CurrencyUtils.parseAmount("45.50") ?: 0.0, 0.001)
    }

    @Test
    fun testDateFormatting() {
        val now = System.currentTimeMillis()
        val enDate = DateUtils.formatDate(now, isAmharic = false)
        val amDate = DateUtils.formatDate(now, isAmharic = true)
        assertEquals("Today", enDate)
        assertEquals("ዛሬ", amDate)
    }

    @Test
    fun testCustomerLifecycleAndBalanceCalculation() = runBlocking {
        // 1. Create Customer
        val customerId = repository.insertCustomer(
            name = "Abebe Bikila",
            phone = "0911223344",
            address = "Merkato, Addis Ababa",
            notes = "Wholesale merchant"
        )
        assertTrue(customerId > 0)

        // Verify customer has zero balance initially
        val initialBalance = repository.getCustomerCurrentBalance(customerId)
        assertEquals(0.0, initialBalance, 0.001)

        // 2. Add Debt: 1,000 ብር
        val debtResult = repository.addDebt(
            customerId = customerId,
            amount = 1000.0,
            description = "Wheat flour 50kg",
            date = System.currentTimeMillis()
        )
        assertTrue(debtResult.isSuccess)

        // Outstanding Balance should now be 1,000 ብር
        val balanceAfterDebt = repository.getCustomerCurrentBalance(customerId)
        assertEquals(1000.0, balanceAfterDebt, 0.001)

        // 3. Customer pays 400 ብር
        val payResult = repository.recordPayment(
            customerId = customerId,
            amount = 400.0,
            paymentMethod = "CASH",
            description = "Partial cash payment",
            date = System.currentTimeMillis()
        )
        assertTrue(payResult.isSuccess)

        // Outstanding balance should now be 600 ብር
        val balanceAfterPayment = repository.getCustomerCurrentBalance(customerId)
        assertEquals(600.0, balanceAfterPayment, 0.001)

        // 4. Overpayment validation: Customer owes 600, tries to pay 800 -> must fail
        val overpayResult = repository.recordPayment(
            customerId = customerId,
            amount = 800.0,
            paymentMethod = "BANK",
            description = "Overpayment attempt",
            date = System.currentTimeMillis()
        )
        assertFalse(overpayResult.isSuccess)
        assertTrue(overpayResult.exceptionOrNull()?.message?.contains("outstanding balance") == true)

        // Balance should still be 600 ብር
        assertEquals(600.0, repository.getCustomerCurrentBalance(customerId), 0.001)

        // 5. Pay the exact remaining balance of 600 ብር
        val payRemaining = repository.recordPayment(
            customerId = customerId,
            amount = 600.0,
            paymentMethod = "BANK",
            description = "CBE transfer",
            date = System.currentTimeMillis()
        )
        assertTrue(payRemaining.isSuccess)

        // Customer balance should now be 0.0 (Fully Paid)
        val finalBalance = repository.getCustomerCurrentBalance(customerId)
        assertEquals(0.0, finalBalance, 0.001)

        // 6. Verify CustomerWithBalance flow
        val customerList = repository.activeCustomersWithBalance.first()
        assertEquals(1, customerList.size)
        val customerItem = customerList[0]
        assertEquals("Abebe Bikila", customerItem.customer.name)
        assertEquals(1000.0, customerItem.totalDebt, 0.001)
        assertEquals(1000.0, customerItem.totalPaid, 0.001)
        assertEquals(0.0, customerItem.balance, 0.001)
        assertTrue(customerItem.isPaid)
        assertFalse(customerItem.isOutstanding)

        // 7. Archive customer
        repository.archiveCustomer(customerId)
        val activeAfterArchive = repository.activeCustomersWithBalance.first()
        assertEquals(0, activeAfterArchive.size)

        val archivedList = repository.archivedCustomersWithBalance.first()
        assertEquals(1, archivedList.size)
        assertEquals("Abebe Bikila", archivedList[0].customer.name)

        // 8. Unarchive customer
        repository.unarchiveCustomer(customerId)
        val activeAfterUnarchive = repository.activeCustomersWithBalance.first()
        assertEquals(1, activeAfterUnarchive.size)
    }

    @Test
    fun testJsonBackupAndRestore() = runBlocking {
        val cId = repository.insertCustomer(
            name = "Hana Girma",
            phone = "0922334455",
            address = "Piassa",
            notes = "Boutique clothes"
        )
        repository.addDebt(cId, 3500.0, "Dresses loan", System.currentTimeMillis())

        val jsonBackup = repository.exportJsonData()
        assertNotNull(jsonBackup)
        assertTrue(jsonBackup.contains("Hana Girma"))
        assertTrue(jsonBackup.contains("3500"))

        // Restore into database
        val restoreResult = repository.restoreJsonData(jsonBackup)
        assertTrue(restoreResult.isSuccess)
        assertEquals(1, restoreResult.getOrDefault(0))

        val customers = repository.activeCustomersWithBalance.first()
        assertEquals(1, customers.size)
        assertEquals("Hana Girma", customers[0].customer.name)
        assertEquals(3500.0, customers[0].balance, 0.001)
    }
}
