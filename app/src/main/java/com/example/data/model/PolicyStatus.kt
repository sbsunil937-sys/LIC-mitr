package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class PremiumStatus {
    PAID,       // 🟢 Premium Paid / Up to date
    DUE_SOON,   // 🟡 Due Soon (within 30 days)
    OVERDUE     // 🔴 Premium Pending / Overdue
}

data class PolicyWithDetails(
    val policy: PolicyEntity,
    val user: UserEntity,
    val receipts: List<PaymentReceiptEntity> = emptyList(),
    val daysRemaining: Long = 0,
    val status: PremiumStatus = PremiumStatus.PAID
)

object PolicyDateHelper {
    private fun getDateFormat(): SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    private fun getDisplayFormat(): SimpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

    fun parseDate(dateStr: String): Date? {
        val clean = dateStr.trim().replace('-', '/')
        val formats = listOf("dd/MM/yyyy", "d/M/yyyy", "yyyy/MM/dd")
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.ENGLISH).apply { isLenient = false }
                val parsed = sdf.parse(clean)
                if (parsed != null) return parsed
            } catch (_: Exception) {}
        }
        return null
    }

    fun formatDate(date: Date): String {
        return getDateFormat().format(date)
    }

    fun formatDisplayDate(dateStr: String): String {
        val parsed = parseDate(dateStr) ?: return dateStr
        return try {
            getDisplayFormat().format(parsed)
        } catch (_: Exception) {
            dateStr
        }
    }

    /**
     * Calculates next premium date by adding frequency in months to the last payment date or start date.
     */
    fun calculateNextPremiumDate(fromDateStr: String, frequencyMonths: Int): String {
        val date = parseDate(fromDateStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, frequencyMonths)
        return getDateFormat().format(cal.time)
    }

    /**
     * Calculates exact calendar days remaining between reference date and next premium date.
     * Aligns both dates to midnight (00:00:00) so time-of-day does not skew remaining days count.
     */
    fun calculateDaysRemaining(nextPremiumDateStr: String, referenceDate: Date = Date()): Long {
        val dueDate = parseDate(nextPremiumDateStr) ?: return 0L
        val calDue = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calRef = Calendar.getInstance().apply {
            time = referenceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = calDue.timeInMillis - calRef.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    /**
     * Determines 🟢 Paid, 🟡 Due Soon, 🔴 Pending/Overdue
     */
    fun determineStatus(daysRemaining: Long): PremiumStatus {
        return when {
            daysRemaining < 0 -> PremiumStatus.OVERDUE
            daysRemaining in 0..30 -> PremiumStatus.DUE_SOON
            else -> PremiumStatus.PAID
        }
    }

    fun formatCurrency(amount: Double): String {
        val longVal = amount.toLong()
        return if (amount == longVal.toDouble()) {
            val formatted = java.text.NumberFormat.getIntegerInstance(Locale("en", "IN")).format(longVal)
            "₹$formatted"
        } else {
            val formatted = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
            formatted.replace("INR", "₹").trim()
        }
    }

    fun getFrequencyLabel(months: Int): String {
        return when (months) {
            1 -> "Monthly (1 Month)"
            3 -> "Quarterly (3 Months)"
            6 -> "Half-Yearly (6 Months)"
            12 -> "Yearly (12 Months)"
            else -> "$months Months"
        }
    }
}
