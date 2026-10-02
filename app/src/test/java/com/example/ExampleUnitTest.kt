package com.example

import com.example.data.model.PolicyDateHelper
import com.example.data.model.PremiumStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSixMonthNextPremiumCalculation() {
        // As requested in prompt: Policy Start: 10/10/2026, Frequency: 6 Months -> Next Premium: 10/04/2027
        val nextDate = PolicyDateHelper.calculateNextPremiumDate("10/10/2026", 6)
        assertEquals("10/04/2027", nextDate)
    }

    @Test
    fun testYearlyNextPremiumCalculation() {
        val nextDate = PolicyDateHelper.calculateNextPremiumDate("15/01/2026", 12)
        assertEquals("15/01/2027", nextDate)
    }

    @Test
    fun testQuarterlyNextPremiumCalculation() {
        val nextDate = PolicyDateHelper.calculateNextPremiumDate("01/01/2026", 3)
        assertEquals("01/04/2026", nextDate)
    }

    @Test
    fun testPremiumStatusDetermination() {
        assertEquals(PremiumStatus.OVERDUE, PolicyDateHelper.determineStatus(-5))
        assertEquals(PremiumStatus.DUE_SOON, PolicyDateHelper.determineStatus(0))
        assertEquals(PremiumStatus.DUE_SOON, PolicyDateHelper.determineStatus(15))
        assertEquals(PremiumStatus.DUE_SOON, PolicyDateHelper.determineStatus(30))
        assertEquals(PremiumStatus.PAID, PolicyDateHelper.determineStatus(190))
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = PolicyDateHelper.formatCurrency(5000.0)
        assertTrue(formatted.contains("5,000") || formatted.contains("5000"))
        assertTrue(formatted.contains("₹"))
    }
}
