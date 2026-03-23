package org.singhak.kubera.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    CREDIT,
    DEBIT
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val smsId: Long,
    val amount: Double,
    val type: TransactionType,
val timestamp: Long,
    val bank: String
)
