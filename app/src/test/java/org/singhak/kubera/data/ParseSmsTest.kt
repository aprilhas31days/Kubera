package org.singhak.kubera.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.singhak.kubera.model.TransactionType

class ParseSmsTest {
    @ParameterizedTest
    @CsvFileSource(resources = ["parse_sms_cases.csv"], numLinesToSkip = 1)
    fun `parses SMS correctly`(
        sender: String,
        sms: String,
        expectedAmount: Double,
        expectedType: String,
    ) {
        val txn = parseSms(1L, sender, sms)
        assertNotNull(txn, "Expected match for: $sms")
        assertEquals(expectedAmount, txn!!.amount)
        assertEquals(TransactionType.valueOf(expectedType), txn.type)
        assertEquals(sender, txn.bank)
    }

    @Test
    fun `returns null for unregistered bank`() {
        val sms = "A/c XX1234 debited Rs. 500.00 on 20-Mar-26 by UPI ref 412345678901"
        assertNull(parseSms(1L, "JD-HDFCBK", sms))
    }

    @Test
    fun `returns null when no debit or credit keyword`() {
        assertNull(parseSms(1L, "JD-INDBNK-S", "Your OTP for transaction is 123456"))
    }

    @Test
    fun `returns null when SMS does not match pattern`() {
        assertNull(parseSms(1L, "JD-INDBNK-S", "Your INDBNK account debited but format is totally different"))
    }
}
