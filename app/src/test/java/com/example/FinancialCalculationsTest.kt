package com.example

import com.example.data.admob.AdManager
import com.example.data.repository.BakiKhataRepository
import com.example.data.sms.SmsGatewayManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {

    @Test
    fun testBengaliNumeralConversion() {
        val english = "1234567890"
        val bengali = BakiKhataRepository.toBengaliDigits(english)
        assertEquals("১২৩৪৫৬৭৮৯০", bengali)
    }

    @Test
    fun testCurrencyFormatting() {
        // 150000 poisha = ৳1,500
        val formatted1500 = BakiKhataRepository.formatCurrency(150000L)
        assertEquals("৳১,৫০০", formatted1500)

        // 32000 poisha = ৳320
        val formatted320 = BakiKhataRepository.formatCurrency(32000L)
        assertEquals("৳৩২০", formatted320)

        // 0 poisha = ৳0
        val formattedZero = BakiKhataRepository.formatCurrency(0L)
        assertEquals("৳০", formattedZero)
    }

    @Test
    fun testAddDueFinancialMath() {
        val initialDuePoisha = 100000L // ৳1,000
        val newDueAmountPoisha = 50000L // ৳500
        val expectedNewDue = initialDuePoisha + newDueAmountPoisha

        assertEquals(150000L, expectedNewDue)
        assertEquals("৳১,৫০০", BakiKhataRepository.formatCurrency(expectedNewDue))
    }

    @Test
    fun testPartialPaymentMath() {
        val currentDuePoisha = 150000L // ৳1,500
        val paymentAmountPoisha = 30000L // ৳300
        val remainingDuePoisha = (currentDuePoisha - paymentAmountPoisha).coerceAtLeast(0L)

        assertEquals(120000L, remainingDuePoisha)
        assertEquals("৳১,২০০", BakiKhataRepository.formatCurrency(remainingDuePoisha))
    }

    @Test
    fun testFullPaymentMath() {
        val currentDuePoisha = 120000L // ৳1,200
        val fullPaymentAmountPoisha = 120000L
        val remainingDuePoisha = (currentDuePoisha - fullPaymentAmountPoisha).coerceAtLeast(0L)

        assertEquals(0L, remainingDuePoisha)
        assertEquals("৳০", BakiKhataRepository.formatCurrency(remainingDuePoisha))
    }

    @Test
    fun testQuantityUnitPriceCalculation() {
        // Example from prompt: Rice 2 kg, Unit price ৳70 -> Total ৳140
        val qty = 2.0
        val unitPricePoisha = 7000L // ৳70.00
        val totalPoisha = (qty * unitPricePoisha).toLong()

        assertEquals(14000L, totalPoisha)
        assertEquals("৳১৪০", BakiKhataRepository.formatCurrency(totalPoisha))
    }

    @Test
    fun testSmsTemplateGeneration() {
        val sms = SmsGatewayManager.buildDueSms(
            shopName = "মায়ের দোয়া স্টোর",
            customerName = "রহিম সাহেব",
            amountFormatted = "৳৩২০",
            totalDueFormatted = "৳১,৫০০"
        )
        assertTrue(sms.contains("মায়ের দোয়া স্টোর"))
        assertTrue(sms.contains("রহিম সাহেব"))
        assertTrue(sms.contains("৳৩২০"))
        assertTrue(sms.contains("৳১,৫০০"))
    }

    @Test
    fun testAdMobPolicyProtection() {
        AdManager.resetActionCounter()
        // Should not trigger on initial actions without meeting frequency cap
        assertFalse(AdManager.shouldTriggerInterstitial())
    }
}
