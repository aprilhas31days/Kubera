package org.singhak.kubera.data

import org.singhak.kubera.data.SmsType.BALANCE
import org.singhak.kubera.data.SmsType.UPI
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.TransactionType.CREDIT
import org.singhak.kubera.model.TransactionType.DEBIT

/**
 * Parses a bank SMS into a [Transaction].
 *
 * @param smsId the unique ID of the SMS message
 * @param sender the SMS sender address (e.g. "JD-INDBNK-S")
 * @param sms the raw SMS body
 *
 * @return a [Transaction] if the SMS matches a known pattern, or `null` otherwise
 */
fun parseSms(smsId: Long, sender: String, sms: String): Transaction? {
    val bank = registeredBanks.firstOrNull {
        sender.contains(it, ignoreCase = true)
    } ?: return null

    val transactionType = transactionType(sms) ?: return null
    val smsType = smsType(sms)

    val regex = patternFor(bank, smsType, transactionType) ?: return null
    val match = regex.find(sms) ?: return null

    val amount = match.groupValues.getOrNull(1)
        ?.replace(",", "")?.toDoubleOrNull() ?: return null

    return Transaction(
        smsId = smsId,
        amount = amount,
        type = transactionType,
        timestamp = System.currentTimeMillis(),
        bank = sender,
    )
}

private fun transactionType(sms: String): TransactionType? = when {
    sms.contains("debited", ignoreCase = true) -> DEBIT
    sms.contains("credited", ignoreCase = true) -> CREDIT
    else -> null
}

private fun smsType(sms: String): SmsType =
    if (sms.contains("UPI", ignoreCase = true)) UPI else BALANCE
