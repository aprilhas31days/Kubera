package org.singhak.kubera.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MerchantSpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.repository.TransactionRepository

@HiltViewModel
class AnalysisViewModel @Inject constructor(repository: TransactionRepository) : ViewModel() {

    private val _filter = MutableStateFlow(AnalysisFilter(dateFrom = currentMonthStart()))
    val filter: StateFlow<AnalysisFilter> = _filter.asStateFlow()

    private val filtered = combine(repository.getAllTransactions(), _filter) { txns, f ->
        txns.filter { t ->
            (f.dateFrom == null || t.timestamp >= f.dateFrom) &&
                (f.dateTo == null || t.timestamp <= f.dateTo) &&
                (f.categories.isEmpty() || t.category in f.categories) &&
                (f.channels.isEmpty() || t.channel in f.channels) &&
                (f.banks.isEmpty() || t.bank in f.banks) &&
                when (f.flow) {
                    FlowFilter.DEBIT -> t.type == TransactionType.DEBIT
                    FlowFilter.CREDIT -> t.type == TransactionType.CREDIT
                    FlowFilter.ALL -> true
                }
        }
    }

    val monthSummary: StateFlow<MonthSummary> = filtered.map { txns ->
        MonthSummary(
            totalExpenditure = txns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
            totalCredited = txns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
            entryCount = txns.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary(0.0, 0.0, 0))

    val categoryBreakdown: StateFlow<List<CategorySpend>> = filtered.map { txns ->
        txns.filter { it.type == TransactionType.DEBIT }
            .groupBy { it.category }
            .map { (cat, list) -> CategorySpend(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailySpend: StateFlow<Map<String, Double>> = filtered.map { txns ->
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        txns.filter { it.type == TransactionType.DEBIT }
            .groupBy { fmt.format(Date(it.timestamp)) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val topMerchants: StateFlow<List<MerchantSpend>> = filtered.map { txns ->
        txns.filter { it.type == TransactionType.DEBIT && it.merchant != null }
            .groupBy { it.merchant!!.lowercase() }
            .map { (_, list) -> MerchantSpend(list.first().merchant!!, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun applyFilter(filter: AnalysisFilter) {
        _filter.value = filter
    }
}

private fun currentMonthStart(): Long = java.util.Calendar.getInstance().apply {
    set(java.util.Calendar.DAY_OF_MONTH, 1)
    set(java.util.Calendar.HOUR_OF_DAY, 0)
    set(java.util.Calendar.MINUTE, 0)
    set(java.util.Calendar.SECOND, 0)
    set(java.util.Calendar.MILLISECOND, 0)
}.timeInMillis
