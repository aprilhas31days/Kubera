package org.singhak.kubera.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.singhak.kubera.model.TransactionCategory.OTHER

enum class TransactionType {
    CREDIT,
    DEBIT
}

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["amount", "timestamp", "bank"], unique = true)]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val bank: String,
    val merchant: String? = null,
    val category: TransactionCategory = OTHER
)
