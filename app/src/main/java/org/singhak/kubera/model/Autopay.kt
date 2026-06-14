package org.singhak.kubera.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autopays")
data class Autopay(
    @PrimaryKey val merchant: String,
    val amount: Double,
    val bank: Bank,
    val nextDueDate: Long,
)
