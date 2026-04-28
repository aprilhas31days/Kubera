package org.singhak.kubera.sms

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType

private typealias BankPatterns = Map<String, Map<TransactionChannel, Map<TransactionType, Regex>>>

class SmsParser(private val bankPatterns: BankPatterns) {
    val registeredBanks: Set<String> get() = bankPatterns.keys

    fun parse(sender: String, sms: String): Transaction? = parseSms(sender, sms, bankPatterns)

    companion object {
        fun fromAssets(context: Context): SmsParser {
            val json = context.assets.open("bank_sms_patterns.json").bufferedReader().readText()
            return fromJson(json)
        }

        fun fromJson(json: String): SmsParser = SmsParser(loadPatterns(json))
    }
}

/**
 * Parses a bank SMS into a [Transaction].
 *
 * @param sender the SMS sender address (e.g. "JD-INDBNK-S")
 * @param sms the raw SMS body
 * @param bankPatterns the compiled bank patterns map
 *
 * @return a [Transaction] if the SMS matches a known pattern, or `null` otherwise
 */
private fun parseSms(
    sender: String,
    sms: String,
    bankPatterns: BankPatterns,
): Transaction? {
    val bankKey = bankPatterns.keys.firstOrNull { sender.contains(it, ignoreCase = true) } ?: return null
    val bank = Bank.valueOf(bankKey)

    for ((channel, patternsByType) in bankPatterns.getValue(bankKey)) {
        for ((type, regex) in patternsByType) {
            val match = regex.find(sms) ?: continue

            val amount = match.groups["amount"]?.value
                ?.replace(",", "")?.toDoubleOrNull() ?: continue
            val merchant = runCatching { match.groups["merchant"]?.value?.trim() }.getOrNull()
            val account = runCatching { match.groups["account"]?.value?.trim() }.getOrNull()

            return Transaction(
                amount = amount,
                type = type,
                channel = channel,
                account = account,
                timestamp = System.currentTimeMillis(),
                bank = bank,
                merchant = merchant,
            )
        }
    }

    return null
}

/**
 * Parses a bank SMS patterns JSON into a nested map of bank → channel → type → compiled [Regex].
 *
 * The JSON structure mirrors the map:
 * `{ "INDBNK": { "UPI": { "DEBIT": "<template>", ... }, ... }, ... }`
 *
 * @param json the raw JSON string (e.g. from `bank_sms_patterns.json`)
 *
 * @return a [BankPatterns] map of bank key → channel → transaction type → compiled [Regex]
 */
private fun loadPatterns(json: String): BankPatterns {
    val root = Json.parseToJsonElement(json).jsonObject
    return root.mapValues { channelEntry ->
        channelEntry.value.jsonObject
            .mapKeys { TransactionChannel.valueOf(it.key) }
            .mapValues { typeEntry ->
                typeEntry.value.jsonObject
                    .mapKeys { TransactionType.valueOf(it.key) }
                    .mapValues { compileTemplate(it.value.jsonPrimitive.content) }
            }
    }
}

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
private fun compileTemplate(template: String): Regex {
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

private enum class SmsPlaceholder(val key: String, val pattern: String) {
    AMOUNT("amount", """[\d,.]+"""),
    MERCHANT("merchant", """.+?"""),
    ACCOUNT("account", """[Xx*\d]+"""),
    SKIP("...", """.+?"""),
}

private val placeholderMap = SmsPlaceholder.entries.associateBy { it.key }
private val templateTokenPattern = Regex("""\{([^}]+)\}""")
