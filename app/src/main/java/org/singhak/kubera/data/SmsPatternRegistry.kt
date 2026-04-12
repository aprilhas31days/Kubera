package org.singhak.kubera.data

import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.TransactionType.CREDIT
import org.singhak.kubera.model.TransactionType.DEBIT

enum class SmsType { UPI, BALANCE }

private const val INDIAN_BANK = "INDBNK"

val registeredBanks get() = bankSmsPatterns.keys

fun patternFor(bank: String, smsType: SmsType, transactionType: TransactionType): Regex? =
    bankSmsPatterns[bank]?.get(smsType)?.get(transactionType)

private val bankSmsPatterns: Map<String, Map<SmsType, Map<TransactionType, Regex>>> = mapOf(
    INDIAN_BANK to mapOf(
        SmsType.UPI to mapOf(
            DEBIT to compileTemplate(
                "A/c {...} debited Rs. {amount} {...} to {merchant}."
            ),
            CREDIT to compileTemplate(
                "Rs.{amount} credited to a/c {...} by a/c linked to VPA {merchant} ("
            ),
        ),
        SmsType.BALANCE to mapOf(
            DEBIT to compileTemplate(
                "Your A/c {...} is debited by Rs. {amount} {...}"
            ),
            CREDIT to compileTemplate(
                "Your A/c {...} is credited by Rs. {amount} {...}"
            ),
        ),
    ),
)
