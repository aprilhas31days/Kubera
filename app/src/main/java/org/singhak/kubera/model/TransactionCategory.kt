package org.singhak.kubera.model

import androidx.compose.ui.graphics.Color

enum class TransactionCategory(val displayName: String) {
    FOOD("Food"),
    RIDES("Rides"),
    TRAVEL("Travel"),
    GROCERIES("Groceries"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    FUEL("Fuel"),
    INVESTMENTS("Investments"),
    RENT("Rent"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other"),
}

val TransactionCategory.color: Color
    get() = when (this) {
        TransactionCategory.FOOD -> Color(0xFFf97316)
        TransactionCategory.RIDES -> Color(0xFF3b82f6)
        TransactionCategory.TRAVEL -> Color(0xFF60a5fa)
        TransactionCategory.GROCERIES -> Color(0xFF22c55e)
        TransactionCategory.SHOPPING -> Color(0xFFec4899)
        TransactionCategory.BILLS -> Color(0xFF06b6d4)
        TransactionCategory.FUEL -> Color(0xFFf59e0b)
        TransactionCategory.INVESTMENTS -> Color(0xFFa855f7)
        TransactionCategory.RENT -> Color(0xFF8b5cf6)
        TransactionCategory.ENTERTAINMENT -> Color(0xFF14b8a6)
        TransactionCategory.OTHER -> Color(0xFF6b7280)
    }
