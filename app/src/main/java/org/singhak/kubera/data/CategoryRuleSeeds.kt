package org.singhak.kubera.data

import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionCategory.BILLS
import org.singhak.kubera.model.TransactionCategory.ENTERTAINMENT
import org.singhak.kubera.model.TransactionCategory.FOOD
import org.singhak.kubera.model.TransactionCategory.FUEL
import org.singhak.kubera.model.TransactionCategory.GROCERIES
import org.singhak.kubera.model.TransactionCategory.INVESTMENTS
import org.singhak.kubera.model.TransactionCategory.RENT
import org.singhak.kubera.model.TransactionCategory.RIDES
import org.singhak.kubera.model.TransactionCategory.SHOPPING
import org.singhak.kubera.model.TransactionCategory.TRAVEL

fun systemCategoryKeywords(): List<Pair<String, TransactionCategory>> = listOf(
    "blinkit" to GROCERIES,

    "irani chai" to FOOD,
    "burger king" to FOOD,

    "uber" to RIDES,

    "indian railw" to TRAVEL,
    "makemytrip" to TRAVEL,
    "le travenues" to TRAVEL,

    "townscript" to ENTERTAINMENT,

    "amazon" to SHOPPING,
    "myntra" to SHOPPING,
    "croma" to SHOPPING,

    "jio" to BILLS,
    "microsoft" to BILLS,
    "netflix" to BILLS,

    "indian oil" to FUEL,

    "mutual fund" to INVESTMENTS,

    "nobroker" to RENT,
)
