package org.singhak.kubera.sms

import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.TransactionType.CREDIT
import org.singhak.kubera.model.TransactionType.DEBIT
import org.singhak.kubera.sms.SmsType.BALANCE
import org.singhak.kubera.sms.SmsType.UPI

enum class SmsPlaceholder(val key: String, val pattern: String) {
    AMOUNT("amount", """[\d,.]+"""),
    MERCHANT("merchant", """[^.]+"""),
    SKIP("...", """.+?"""),
}

enum class SmsType { UPI, BALANCE }

val registeredBanks get() = bankSmsPatterns.keys

private const val INDIAN_BANK = "INDBNK"

private val placeholderMap = SmsPlaceholder.entries.associateBy { it.key }
private val templateTokenPattern = Regex("""\{([^}]+)\}""")
private val bankSmsPatterns: Map<String, Map<SmsType, Map<TransactionType, Regex>>> = mapOf(
    INDIAN_BANK to mapOf(
        UPI to mapOf(
            DEBIT to compileTemplate("A/c {...} debited Rs. {amount} {...} to {merchant}."),
            CREDIT to compileTemplate(
                "Rs.{amount} credited to a/c {...} by a/c linked to VPA {merchant} ("
            ),
        ),
        BALANCE to mapOf(
            DEBIT to compileTemplate("Your A/c {...} is debited by Rs. {amount} {...}"),
            CREDIT to compileTemplate("Your A/c {...} is credited by Rs. {amount} {...}"),
        ),
    ),
)

/**
 * Compiles a human-readable SMS template into a [Regex].
 *
 * Placeholders like `{amount}` become capturing groups, `{...}` becomes
 * a non-capturing wildcard, and everything else is escaped as literal text.
 *
 * @param template the SMS template (e.g. `"A/c {...} debited Rs. {amount} {...}"`)
 *
 * @return a compiled [Regex] (e.g. `A/c .+? debited Rs\. ([\d,.]+) .+?`)
 */
fun compileTemplate(template: String): Regex {
    val regexString = buildString {
        var lastEnd = 0
        for (match in templateTokenPattern.findAll(template)) {
            append(Regex.escape(template.substring(lastEnd, match.range.first)))
            val key = match.groupValues[1]
            val placeholder = placeholderMap[key] ?: error("Unknown placeholder: {$key}")
            if (placeholder == SmsPlaceholder.SKIP) {
                append(placeholder.pattern)
            } else {
                append("(?<$key>${placeholder.pattern})")
            }
            lastEnd = match.range.last + 1
        }
        if (lastEnd < template.length) {
            append(Regex.escape(template.substring(lastEnd)))
        }
    }
    return Regex(regexString)
}

fun patternFor(bank: String, smsType: SmsType, transactionType: TransactionType): Regex? =
    bankSmsPatterns[bank]?.get(smsType)?.get(transactionType)

/**
 * Parses a bank SMS into a [Transaction].
 *
 * @param sender the SMS sender address (e.g. "JD-INDBNK-S")
 * @param sms the raw SMS body
 *
 * @return a [Transaction] if the SMS matches a known pattern, or `null` otherwise
 */
fun parseSms(sender: String, sms: String): Transaction? {
    val bank = registeredBanks.firstOrNull {
        sender.contains(it, ignoreCase = true)
    } ?: return null

    val transactionType = detectTransactionType(sms) ?: return null
    val smsType = detectSmsType(sms)

    val regex = patternFor(bank, smsType, transactionType) ?: return null
    val match = regex.find(sms) ?: return null

    val amount = match.groups["amount"]?.value
        ?.replace(",", "")?.toDoubleOrNull() ?: return null
    val merchant = runCatching { match.groups["merchant"]?.value?.trim() }.getOrNull()

    return Transaction(
        amount = amount,
        type = transactionType,
        timestamp = System.currentTimeMillis(),
        bank = sender,
        merchant = merchant,
    )
}

private fun detectTransactionType(sms: String): TransactionType? = when {
    sms.contains("debited", ignoreCase = true) -> DEBIT
    sms.contains("credited", ignoreCase = true) -> CREDIT
    else -> null
}

private fun detectSmsType(sms: String): SmsType =
    if (sms.contains("UPI", ignoreCase = true)) UPI else BALANCE
