package org.singhak.kubera.data

enum class SmsPlaceholder(val key: String, val pattern: String) {
    AMOUNT("amount", """[\d,.]+"""),
    SKIP("...", """.+?"""),
}

private val placeholderMap = SmsPlaceholder.entries.associateBy { it.key }
private val templateTokenPattern = Regex("""\{([^}]+)\}""")

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
            val placeholder = placeholderMap[key]
                ?: error("Unknown placeholder: {$key}")

            if (placeholder == SmsPlaceholder.SKIP) {
                append(placeholder.pattern)
            } else {
                append("(${placeholder.pattern})")
            }

            lastEnd = match.range.last + 1
        }

        if (lastEnd < template.length) {
            append(Regex.escape(template.substring(lastEnd)))
        }
    }

    return Regex(regexString)
}
