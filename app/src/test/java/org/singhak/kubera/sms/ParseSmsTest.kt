package org.singhak.kubera.sms

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType

class ParseSmsTest {
    private val parser by lazy {
        SmsParser.fromJson(File("src/main/assets/bank_sms_patterns.json").readText())
    }

    @ParameterizedTest
    @MethodSource("smsCases")
    fun `parses SMS correctly`(case: SmsTestCase) {
        val txn = parser.parse(case.input.sender, case.input.sms)
        assertNotNull(txn, "Expected match for: ${case.input.sms}")
        assertEquals(case.expected.toTransaction(), txn!!.copy(timestamp = 0L))
    }

    @Test
    fun `returns null for unregistered bank`() {
        assertNull(parser.parse("JD-HDFCBK", "A/c XX1234 debited Rs. 500.00 on 20-Mar-26 by UPI ref 412345678901"))
    }

    @Test
    fun `returns null when SMS matches no pattern`() {
        assertNull(parser.parse("JD-INDBNK-S", "Your OTP for transaction is 123456"))
    }

    @Test
    fun `returns null when SMS does not match pattern`() {
        assertNull(parser.parse("JD-INDBNK-S", "Your INDBNK account debited but format is totally different"))
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
    }
}

@Serializable
data class SmsTestCase(val input: Input, val expected: Expected)

@Serializable
data class Input(val sender: String, val sms: String)

@Serializable
data class Expected(
    val bank: String,
    val amount: Double,
    val type: String,
    val channel: String,
    val account: String? = null,
    val merchant: String? = null,
) {
    fun toTransaction() = Transaction(
        amount = amount,
        type = TransactionType.valueOf(type),
        channel = TransactionChannel.valueOf(channel),
        account = account,
        timestamp = 0L,
        bank = Bank.valueOf(bank),
        merchant = merchant,
        category = TransactionCategory.OTHER,
    )
}
