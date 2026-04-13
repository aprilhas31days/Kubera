package org.singhak.kubera.sms

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType

class ParseSmsTest {
    @ParameterizedTest
    @MethodSource("smsCases")
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
        assertNull(
            parseSms("JD-INDBNK-S", "Your INDBNK account debited but format is totally different")
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("templateCases")
    fun `compileTemplate produces correct regex`(
        description: String,
        template: String,
        expectedPattern: String
    ) {
        assertEquals(expectedPattern, compileTemplate(template).pattern)
    }

    @Test
    fun `unknown placeholder throws`() {
        assertThrows(IllegalStateException::class.java) {
            compileTemplate("Hello {unknown} world")
        }
    }

    companion object {
        @JvmStatic
        fun smsCases(): List<SmsTestCase> {
            val json = ParseSmsTest::class.java
                .getResourceAsStream("/org/singhak/kubera/sms/parse_sms_cases.json")!!
                .bufferedReader()
                .readText()
            return Json.decodeFromString(json)
        }

        @JvmStatic
        fun templateCases() = listOf(
            Arguments.of("escapes literal text", "Rs. {amount}", """\QRs. \E(?<amount>[\d,.]+)"""),
            Arguments.of(
                "merchant becomes named group",
                "paid to {merchant}.",
                """\Qpaid to \E(?<merchant>[^.]+)\Q.\E"""
            ),
            Arguments.of(
                "skip is non-capturing",
                "A/c {...} Rs. {amount}",
                """\QA/c \E.+?\Q Rs. \E(?<amount>[\d,.]+)"""
            ),
            Arguments.of(
                "both groups captured",
                "Rs. {amount} to {merchant}.",
                """\QRs. \E(?<amount>[\d,.]+)\Q to \E(?<merchant>[^.]+)\Q.\E"""
            )
        )
    }
}

@Serializable
data class SmsTestCase(val input: Input, val expected: Expected)

@Serializable
data class Input(val sender: String, val sms: String)

@Serializable
data class Expected(val amount: Double, val type: String, val merchant: String? = null) {
    fun toTransaction(sender: String) = Transaction(
        amount = amount,
        type = TransactionType.valueOf(type),
        timestamp = 0L,
        bank = sender,
        merchant = merchant
    )
}
