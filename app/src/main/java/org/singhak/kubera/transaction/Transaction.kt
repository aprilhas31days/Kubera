package org.singhak.kubera.transaction

enum class TransactionType {
    CREDIT,
    DEBIT
}

data class Transaction(
    val amount: Double,
    val type: TransactionType,
    val accountNumber: String,
    val timestamp: Long,
    val bank: String
)
