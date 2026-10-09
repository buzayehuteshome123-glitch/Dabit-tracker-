package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val enDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    private val enTimeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
    private val enDateTimeFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.ENGLISH)

    private val amMonths = arrayOf(
        "ጃንዋሪ", "ፌብሩወሪ", "ማርች", "ኤፕሪል", "ሜይ", "ጁን",
        "ጁላይ", "ኦገስት", "ሴፕቴምበር", "ኦክቶበር", "ኖቬምበር", "ዲሴምበር"
    )

    fun formatDate(timestamp: Long, isAmharic: Boolean): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()

        val isToday = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return if (isAmharic) "ዛሬ" else "Today"
        }
        if (isYesterday) {
            return if (isAmharic) "ትናንት" else "Yesterday"
        }

        return if (isAmharic) {
            val month = amMonths[cal.get(Calendar.MONTH)]
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val year = cal.get(Calendar.YEAR)
            "$month $day, $year"
        } else {
            enDateFormat.format(Date(timestamp))
        }
    }

    fun formatTime(timestamp: Long, isAmharic: Boolean): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR)
        val displayHour = if (hour == 0) 12 else hour
        val minute = cal.get(Calendar.MINUTE)
        val amPm = cal.get(Calendar.AM_PM)
        val minuteStr = if (minute < 10) "0$minute" else "$minute"

        return if (isAmharic) {
            val period = if (amPm == Calendar.AM) "ጠዋት" else "ከሰዓት"
            "$displayHour:$minuteStr $period"
        } else {
            val period = if (amPm == Calendar.AM) "AM" else "PM"
            "$displayHour:$minuteStr $period"
        }
    }

    fun formatDateTime(timestamp: Long, isAmharic: Boolean): String {
        return "${formatDate(timestamp, isAmharic)} • ${formatTime(timestamp, isAmharic)}"
    }
}
