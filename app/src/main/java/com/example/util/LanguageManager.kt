package com.example.util

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    AMHARIC("am", "Amharic", "አማርኛ")
}

class LanguageManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
    private val _currentLanguage = mutableStateOf(
        if (prefs.getString("language", "en") == "am") AppLanguage.AMHARIC else AppLanguage.ENGLISH
    )
    val currentLanguage: State<AppLanguage> = _currentLanguage

    val isAmharic: Boolean
        get() = _currentLanguage.value == AppLanguage.AMHARIC

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.code).apply()
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.AMHARIC else AppLanguage.ENGLISH
        setLanguage(next)
    }

    fun getString(key: String): String {
        val isAm = isAmharic
        return when (key) {
            // Navigation
            "nav_home" -> if (isAm) "መነሻ" else "Home"
            "nav_customers" -> if (isAm) "ደንበኞች" else "Customers"
            "nav_activity" -> if (isAm) "እንቅስቃሴ" else "Activity"
            "nav_settings" -> if (isAm) "ቅንብሮች" else "Settings"

            // Dashboard
            "total_outstanding" -> if (isAm) "ጠቅላላ ቀሪ ዕዳ" else "Total Outstanding"
            "total_customers" -> if (isAm) "ጠቅላላ ደንበኞች" else "Total Customers"
            "total_debt_given" -> if (isAm) "ጠቅላላ የተሰጠ ዕዳ" else "Total Debt Given"
            "total_payments_received" -> if (isAm) "ጠቅላላ የተቀበሉት ክፍያ" else "Total Payments Received"
            "recent_activity" -> if (isAm) "የቅርብ ጊዜ እንቅስቃሴ" else "Recent Activity"
            "add_debt" -> if (isAm) "+ ዕዳ መዝግብ" else "+ Add Debt"
            "record_payment" -> if (isAm) "ክፍያ መዝግብ" else "Record Payment"
            "view_all" -> if (isAm) "ሁሉንም እይ" else "View All"
            "customers_count_suffix" -> if (isAm) " ደንበኞች" else " Customers"

            // Customer List
            "search_customers" -> if (isAm) "በስም ወይም በስልክ ይፈልጉ..." else "Search by name or phone..."
            "filter_all" -> if (isAm) "ሁሉም" else "All"
            "filter_with_debt" -> if (isAm) "ዕዳ ያለባቸው" else "With Debt"
            "filter_paid" -> if (isAm) "የተከፈለ" else "Fully Paid"
            "owed_suffix" -> if (isAm) "ያለበት" else "owed"
            "status_outstanding" -> if (isAm) "ቀሪ ዕዳ" else "Outstanding"
            "status_paid" -> if (isAm) "ተከፍሏል" else "Paid"
            "add_customer" -> if (isAm) "+ ደንበኛ ጨምር" else "+ Add Customer"
            "edit_customer" -> if (isAm) "ደንበኛ አርትዕ" else "Edit Customer"
            "archive_customer" -> if (isAm) "ደንበኛ አስቀምጥ (Archive)" else "Archive Customer"
            "unarchive_customer" -> if (isAm) "ደንበኛ መልስ" else "Restore Customer"
            "archived_badge" -> if (isAm) "የተቀመጠ" else "Archived"

            // Customer Details
            "customer_name" -> if (isAm) "የደንበኛ ስም" else "Customer Name"
            "phone_number" -> if (isAm) "ስልክ ቁጥር" else "Phone Number"
            "address" -> if (isAm) "አድራሻ" else "Address"
            "notes" -> if (isAm) "ማስታወሻ" else "Notes"
            "save_customer" -> if (isAm) "ደንበኛ አስቀምጥ" else "Save Customer"
            "total_debt" -> if (isAm) "ጠቅላላ ዕዳ" else "Total Debt"
            "total_paid" -> if (isAm) "ጠቅላላ የተከፈለ" else "Total Paid"
            "transaction_history" -> if (isAm) "የግብይት ታሪክ" else "Transaction History"
            "no_history_yet" -> if (isAm) "እስካሁን ምንም ግብይት የለም" else "No transactions yet"

            // Empty States
            "no_customers_title" -> if (isAm) "ምንም ደንበኞች የሉም" else "No customers yet"
            "no_customers_desc" -> if (isAm) "ዕዳ መከታተል ለመጀመር የመጀመሪያዎን ደንበኛ ያክሉ።" else "Add your first customer to start tracking debt."
            "no_transactions_title" -> if (isAm) "ምንም ግብይቶች የሉም" else "No transactions yet"
            "no_transactions_desc" -> if (isAm) "ቀሪ ሂሳቦችን ለመከታተል ዕዳዎችንና ክፍያዎችን ይመዝግቡ።" else "Record debts and payments to keep track of balances."
            "no_search_results" -> if (isAm) "ምንም የተገኘ ውጤት የለም" else "No matching customers found"

            // Forms & Dialogs
            "save_debt" -> if (isAm) "ዕዳ አስቀምጥ" else "Save Debt"
            "save_payment" -> if (isAm) "ክፍያ አስቀምጥ" else "Save Payment"
            "amount" -> if (isAm) "መጠን" else "Amount"
            "description" -> if (isAm) "ዝርዝር መግለጫ" else "Description"
            "date" -> if (isAm) "ቀን" else "Date"
            "select_customer" -> if (isAm) "ደንበኛ ይምረጡ" else "Select Customer"
            "payment_method" -> if (isAm) "የክፍያ ዘዴ" else "Payment Method"
            "method_cash" -> if (isAm) "ጥሬ ገንዘብ" else "Cash"
            "method_bank" -> if (isAm) "ባንክ" else "Bank"
            "method_other" -> if (isAm) "ሌላ" else "Other"
            "current_balance" -> if (isAm) "የአሁኑ ቀሪ ሂሳብ" else "Current Balance"
            "new_balance" -> if (isAm) "አዲስ ቀሪ ሂሳብ" else "New Balance"
            "previous_balance" -> if (isAm) "የቀድሞ ቀሪ ሂሳብ" else "Previous Balance"
            "optional_label" -> if (isAm) "(አማራጭ)" else "(Optional)"
            "required_label" -> if (isAm) "*" else "*"

            // Transaction Types
            "type_debt" -> if (isAm) "ዕዳ" else "Debt"
            "type_payment" -> if (isAm) "ክፍያ" else "Payment"

            // Validations & Errors
            "error_name_required" -> if (isAm) "የደንበኛ ስም ያስፈልጋል።" else "Customer name is required."
            "error_amount_required" -> if (isAm) "ትክክለኛ መጠን ያስገቡ።" else "Enter a valid amount."
            "error_amount_positive" -> if (isAm) "መጠኑ ከ 0 በላይ መሆን አለበት።" else "Amount must be greater than 0."
            "error_payment_exceeds" -> if (isAm) "የክፍያው መጠን ከቀሪ ዕዳው መብለጥ አይችልም።" else "Payment cannot be greater than the outstanding balance."
            "error_customer_required" -> if (isAm) "እባክዎ ደንበኛ ይምረጡ።" else "Please select a customer."
            "error_generic" -> if (isAm) "ስህተት ተፈጥሯል፤ እባክዎ እንደገና ይሞክሩ።" else "Something went wrong. Please try again."

            // Actions & Confirmations
            "cancel" -> if (isAm) "ሰርዝ" else "Cancel"
            "delete" -> if (isAm) "አጥፋ" else "Delete"
            "save" -> if (isAm) "አስቀምጥ" else "Save"
            "edit" -> if (isAm) "አርትዕ" else "Edit"
            "archive" -> if (isAm) "አስቀምጥ" else "Archive"
            "delete_transaction_title" -> if (isAm) "ይህን ግብይት ይሰርዙ?" else "Delete this transaction?"
            "delete_transaction_desc" -> if (isAm) "ይህን ግብይት መሰረዝ የደንበኛውን ቀሪ ዕዳ ይለውጣል።" else "Deleting this transaction will change the customer's outstanding balance."
            "archive_customer_title" -> if (isAm) "ይህን ደንበኛ ማህደር ውስጥ ያስቀምጡ?" else "Archive this customer?"
            "archive_customer_desc" -> if (isAm) "ደንበኛው ከንቁ ዝርዝር ይደበቃል፤ ነገር ግን ሁሉም የዕዳ እና የክፍያ መዝገቦች በጥንቃቄ ተጠብቀው ይቆያሉ።" else "The customer will be hidden from the active list, but all debt and payment records will be safely preserved."

            // Settings & Backup
            "settings_language" -> if (isAm) "ቋንቋ / Language" else "Language / ቋንቋ"
            "settings_currency" -> if (isAm) "የገንዘብ ምንዛሪ" else "Currency"
            "settings_currency_val" -> if (isAm) "የኢትዮጵያ ብር (ETB / ብር)" else "Ethiopian Birr (ETB / ብር)"
            "settings_backup" -> if (isAm) "ምትኬ እና ማጋራት" else "Backup & Export"
            "export_csv" -> if (isAm) "ውሂብ በ CSV ላክ" else "Export CSV"
            "export_csv_desc" -> if (isAm) "ከ Excel እና Google Sheets ጋር የሚስማማ የደንበኞች እና ግብይቶች ፋይል" else "Customer and transaction records compatible with Excel & Google Sheets"
            "backup_json" -> if (isAm) "ሙሉ ውሂብ ምትኬ (JSON)" else "Full Backup (JSON)"
            "backup_json_desc" -> if (isAm) "ሁሉንም መረጃዎች ወደ JSON ፋይል አስቀምጥ" else "Save all records into a portable backup file"
            "restore_json" -> if (isAm) "ውሂብ ከመጠባበቂያ ፋይል መልስ" else "Restore from Backup"
            "restore_json_desc" -> if (isAm) "ቀደም ሲል የተዘጋጀ የ JSON ምትኬ ፋይል ይጫኑ" else "Load previously saved backup data"
            "restore_confirm_title" -> if (isAm) "ውሂብ መመለስ ይፈልጋሉ?" else "Restore Backup Data?"
            "restore_confirm_desc" -> if (isAm) "ማስጠንቀቂያ፡ ውሂብ መመለስ አሁን ያሉትን መረጃዎች በምትኬው ፋይል ይተካል!" else "Warning: Restoring will overwrite existing customers and transactions with the backup file!"
            "restore_confirm_btn" -> if (isAm) "አዎ፣ መልስ" else "Yes, Restore"
            "archived_customers_title" -> if (isAm) "የተቀመጡ ደንበኞች" else "Archived Customers"
            "archived_customers_desc" -> if (isAm) "ማህደር ውስጥ የተቀመጡ ደንበኞችን ይመልከቱ ወይም ይመልሱ" else "View or restore archived customer records"
            "about_app" -> if (isAm) "ስለ መተግበሪያው" else "About Debt Tracker"
            "about_version" -> if (isAm) "ስሪት 1.0 (ከመስመር ውጭ)" else "Version 1.0 (Offline-first)"
            "about_privacy" -> if (isAm) "የግል መረጃ ጥበቃ: ሁሉም መረጃዎች በስልክዎ ላይ ብቻ የተጠበቁ ናቸው።" else "Privacy: All business records are stored strictly on your local device."
            "export_ready" -> if (isAm) "ፋይሉ ተዘጋጅቷል" else "Export file is ready"

            // Onboarding
            "onboarding_1_title" -> if (isAm) "የደንበኞችን ዕዳ በቀላሉ ይከታተሉ" else "Track Customer Debt Easily"
            "onboarding_1_desc" -> if (isAm) "ሁሉንም የደንበኞች ዕዳና ብድር በአንድ አስተማማኝ ቦታ ያደራጁ።" else "Keep all customer credits and loans organized in one secure place."
            "onboarding_2_title" -> if (isAm) "ዕዳና ክፍያ ይመዝግቡ" else "Record debts and payments"
            "onboarding_2_desc" -> if (isAm) "ደንበኞች እቃ በብድር ሲወስዱ ዕዳ ይመዝግቡ፤ ሲከፍሉ ደግሞ ክፍያ ይመዝግቡ።" else "Record debt when items are loaned, and record payments as customers pay you back."
            "onboarding_3_title" -> if (isAm) "ማን ምን ያህል እንደሚያወራህ በቀላሉ ይወቁ" else "Know exactly who owes you"
            "onboarding_3_desc" -> if (isAm) "ቀሪ ሂሳቦችን፣ የግብይት ታሪክን እና አጠቃላይ የሚፈለገውን ገንዘብ በቅጽበት ይወቁ።" else "Instant balances, transaction history, and total money owed at a single glance."
            "get_started" -> if (isAm) "ጀምር" else "Get Started"
            "skip" -> if (isAm) "ዝለል" else "Skip"
            "next" -> if (isAm) "ቀጣይ" else "Next"

            else -> key
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LanguageManager? = null

        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
