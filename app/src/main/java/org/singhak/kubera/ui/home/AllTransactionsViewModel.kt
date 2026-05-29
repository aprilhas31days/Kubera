package org.singhak.kubera.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.repository.TransactionRepository

private const val ONE_DAY_MS = 24 * 60 * 60 * 1_000L

data class TransactionFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val channel: TransactionChannel? = null,
    val category: TransactionCategory? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || channel != null ||
            category != null || fromDate != null || toDate != null
}

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    repository: TransactionRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .combine(_filter) { txns, f ->
            txns.filter { txn ->
                (f.query.isBlank() || txn.merchant?.contains(f.query, ignoreCase = true) == true) &&
                    (f.type == null || txn.type == f.type) &&
                    (f.channel == null || txn.channel == f.channel) &&
                    (f.category == null || txn.category == f.category) &&
                    (f.fromDate == null || txn.timestamp >= f.fromDate) &&
                    (f.toDate == null || txn.timestamp < f.toDate + ONE_DAY_MS)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(query: String) = _filter.update { it.copy(query = query) }
    fun updateType(type: TransactionType?) = _filter.update { it.copy(type = type) }
    fun updateChannel(channel: TransactionChannel?) = _filter.update { it.copy(channel = channel) }
    fun updateCategory(category: TransactionCategory?) = _filter.update { it.copy(category = category) }
    fun updateFromDate(date: Long?) = _filter.update { it.copy(fromDate = date) }
    fun updateToDate(date: Long?) = _filter.update { it.copy(toDate = date) }
    fun clearFilters() { _filter.value = TransactionFilter() }
}
