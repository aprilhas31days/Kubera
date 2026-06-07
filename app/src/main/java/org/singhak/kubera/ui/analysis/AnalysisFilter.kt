package org.singhak.kubera.ui.analysis

import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel

enum class FlowFilter { ALL, DEBIT, CREDIT }

data class AnalysisFilter(
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val categories: Set<TransactionCategory> = emptySet(),
    val channels: Set<TransactionChannel> = emptySet(),
    val banks: Set<Bank> = emptySet(),
    val flow: FlowFilter = FlowFilter.ALL,
) {
    val activeCount: Int
        get() = (if (dateFrom != null || dateTo != null) 1 else 0) +
            (if (categories.isNotEmpty()) 1 else 0) +
            (if (channels.isNotEmpty()) 1 else 0) +
            (if (banks.isNotEmpty()) 1 else 0) +
            (if (flow != FlowFilter.ALL) 1 else 0)
}
