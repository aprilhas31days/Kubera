package org.singhak.kubera.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.singhak.kubera.model.TransactionCategory.OTHER

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["amount", "timestamp", "bank"], unique = true)]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val channel: TransactionChannel,
    val account: String? = null,
    val timestamp: Long,
    val bank: Bank,
    val merchant: String? = null,
    val category: TransactionCategory = OTHER,
)
