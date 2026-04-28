package org.singhak.kubera.model

enum class TransactionChannel {
    UPI, CREDIT_CARD, ATM, NEFT, IMPS;

    val displayName: String get() = when (this) {
        CREDIT_CARD -> "Credit Card"
        else -> name
    }
}
