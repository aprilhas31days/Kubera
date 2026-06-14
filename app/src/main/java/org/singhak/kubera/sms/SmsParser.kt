package org.singhak.kubera.sms

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType

private typealias BankPatterns = Map<String, Map<TransactionChannel, Map<TransactionType, List<Regex>>>>

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

private fun parseSms(
    sender: String,
    sms: String,
    bankPatterns: BankPatterns,
): Transaction? {
    val bankKey = bankPatterns.keys.firstOrNull { sender.contains(it, ignoreCase = true) } ?: return null
    val bank = Bank.valueOf(bankKey)

    for ((channel, patternsByType) in bankPatterns.getValue(bankKey)) {
        for ((type, regexes) in patternsByType) {
            for (regex in regexes) {
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
    }

    return null
}

/**
 * Parses bank_sms_patterns.json into a nested map.
 * Each type key maps to a list of compiled regexes — the JSON value may be
 * a single string or an array of strings for multiple pattern variants.
 */
private fun loadPatterns(json: String): BankPatterns {
    val root = Json.parseToJsonElement(json).jsonObject
    return root.mapValues { channelEntry ->
        channelEntry.value.jsonObject
            .mapKeys { TransactionChannel.valueOf(it.key) }
            .mapValues { typeEntry ->
                typeEntry.value.jsonObject
                    .mapKeys { TransactionType.valueOf(it.key) }
                    .mapValues { entry ->
                        when (val value = entry.value) {
                            is JsonArray -> value.map { compileTemplate(it.jsonPrimitive.content) }
                            else -> listOf(compileTemplate(value.jsonPrimitive.content))
                        }
                    }
            }
    }
}

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
