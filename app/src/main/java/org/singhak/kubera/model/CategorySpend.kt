package org.singhak.kubera.model

import androidx.room.ColumnInfo

data class CategorySpend(
    @ColumnInfo(name = "category") val category: TransactionCategory,
    @ColumnInfo(name = "total") val total: Double
)
