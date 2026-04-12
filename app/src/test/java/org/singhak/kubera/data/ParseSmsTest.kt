package org.singhak.kubera.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType

class ParseSmsTest {
    @ParameterizedTest
    @MethodSource("cases")
    fun `parses SMS correctly`(case: SmsTestCase) {
        val txn = parseSms(case.input.sender, case.input.sms)
        assertNotNull(txn, "Expected match for: ${case.input.sms}")
        assertEquals(case.expected.toTransaction(case.input.sender), txn!!.copy(timestamp = 0L))
    }

    @Test
    fun `returns null for unregistered bank`() {
        val sms = "A/c XX1234 debited Rs. 500.00 on 20-Mar-26 by UPI ref 412345678901"
        assertNull(parseSms("JD-HDFCBK", sms))
    }

    @Test
    fun `returns null when no debit or credit keyword`() {
        assertNull(parseSms("JD-INDBNK-S", "Your OTP for transaction is 123456"))
    }

    @Test
    fun `returns null when SMS does not match pattern`() {
        assertNull(parseSms("JD-INDBNK-S", "Your INDBNK account debited but format is totally different"))
    }

    companion object {
        @JvmStatic
        fun cases(): List<SmsTestCase> {
            val json = ParseSmsTest::class.java
                .getResourceAsStream("parse_sms_cases.json")!!
                .bufferedReader()
                .readText()
            return Json.decodeFromString(json)
        }
    }
}

@Serializable
data class SmsTestCase(
    val input: Input,
    val expected: Expected,
)

@Serializable
data class Input(
    val sender: String,
    val sms: String,
)

@Serializable
data class Expected(
    val amount: Double,
    val type: String,
    val merchant: String? = null,
) {
    fun toTransaction(sender: String) = Transaction(
        amount = amount,
        type = TransactionType.valueOf(type),
        timestamp = 0L,
        bank = sender,
        merchant = merchant,
    )
}
