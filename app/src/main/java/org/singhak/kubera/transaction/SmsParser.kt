package org.singhak.kubera.transaction

val knownSenderTags: Set<String> get() = senderTagToSmsPattern.keys

private val senderTagToSmsPattern = mapOf(
    "INDBNK" to
        Regex(
            """A/c \*(?<accountNumber>\d+) debited Rs\. (?<amount>[\d,.]+) on [\d-]+ to (?<counterparty>.+?)\. UPI:(?<ref>\d+)"""
        )
)
private val groupNamePattern = Regex("""\(\?<(\w+)>""")

/**
 * Parses a bank sms into a [Transaction].
 *
 * @param sender the sms sender address (e.g. "JD-INDBNK-S")
 * @param sms the raw sms body
 * @return a [Transaction] if the sms matches a known pattern, or `null` otherwise
 */
fun parseSms(sender: String, sms: String): Transaction? {
    val senderTag = findSenderTag(sender) ?: return null
    val smsPattern = senderTagToSmsPattern[senderTag] ?: return null

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
 * Finds the sender tag from a sender address using partial match.
 *
 * @param sender the full sender address (e.g. "JD-INDBNK-S")
 * @return the matching sender tag (e.g. "INDBNK"), or `null` if no match
 */
private fun findSenderTag(sender: String): String? =
    knownSenderTags.firstOrNull { sender.contains(it, ignoreCase = true) }

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
