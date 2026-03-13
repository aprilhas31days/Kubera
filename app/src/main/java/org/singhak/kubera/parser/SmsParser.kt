package org.singhak.kubera.parser

import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType

private val groupNamePattern = Regex("""\(\?<(\w+)>""")

private val senderToSmsPattern = mapOf(
    "IN-INDBNK" to Regex("""A/c \*(?<accountNumber>\d+) debited Rs\. (?<amount>[\d,.]+) on [\d-]+ to (?<counterparty>.+?)\. UPI:(?<ref>\d+)"""),
)

/**
 * Parses a bank sms into a [Transaction].
 *
 * @param sender the sms sender identifier (e.g. "IN-INDBNK")
 * @param sms the raw sms body
 * @return a [Transaction] if the sms matches a known pattern, or `null` otherwise
 */
fun parseSms(sender: String, sms: String): Transaction? {
    val smsPattern = senderToSmsPattern[sender] ?: return null

    val transactionFields = extractNamedGroups(smsPattern, sms)
    if (transactionFields.isEmpty()) return null

    val amount = transactionFields["amount"]
        ?.replace(",", "")?.toDoubleOrNull() ?: return null
    val accountNumber = transactionFields["accountNumber"] ?: return null
    val type = when {
        sms.contains("debit", ignoreCase = true) -> TransactionType.DEBIT
        sms.contains("credit", ignoreCase = true) -> TransactionType.CREDIT
        else -> return null
    }
    val timestamp = System.currentTimeMillis()

    return Transaction(
        amount = amount,
        type = type,
        accountNumber = accountNumber,
        timestamp = timestamp,
        bank = sender
    )
}

/**
 * Extracts named group values from [input] using a regex with named transactionFields.
 *
 * @param pattern a regex like `(?<amount>\d+) worth of (?<item>\w+)`
 * @param input a string like `"42 worth of gold"`
 * @return group names to matched values (e.g. `{amount=42, item=gold}`), or empty map if no match.
 */
private fun extractNamedGroups(pattern: Regex, input: String): Map<String, String> {
    val match = pattern.find(input) ?: return emptyMap()
    val names = groupNamePattern.findAll(pattern.pattern).map { it.groupValues[1] }.toList()

    return names.zip(match.groupValues.drop(1)).toMap()
}

