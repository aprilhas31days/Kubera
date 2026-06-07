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
    val channels: Set<TransactionChannel> = emptySet(),
    val categories: Set<TransactionCategory> = emptySet(),
    val fromDate: Long? = null,
    val toDate: Long? = null,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || channels.isNotEmpty() ||
            categories.isNotEmpty() || fromDate != null || toDate != null

    val activeCount: Int
        get() = (if (query.isNotBlank()) 1 else 0) +
            (if (type != null) 1 else 0) +
            (if (channels.isNotEmpty()) 1 else 0) +
            (if (categories.isNotEmpty()) 1 else 0) +
            (if (fromDate != null || toDate != null) 1 else 0)
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
                    (f.channels.isEmpty() || txn.channel in f.channels) &&
                    (f.categories.isEmpty() || txn.category in f.categories) &&
                    (f.fromDate == null || txn.timestamp >= f.fromDate) &&
                    (f.toDate == null || txn.timestamp < f.toDate + ONE_DAY_MS)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(query: String) = _filter.update { it.copy(query = query) }
    fun updateType(type: TransactionType?) = _filter.update { it.copy(type = type) }

    fun toggleChannel(channel: TransactionChannel) = _filter.update { f ->
        val updated = if (channel in f.channels) f.channels - channel else f.channels + channel
        f.copy(channels = updated)
    }

    fun toggleCategory(category: TransactionCategory) = _filter.update { f ->
        val updated = if (category in f.categories) f.categories - category else f.categories + category
        f.copy(categories = updated)
    }

    fun updateFromDate(date: Long?) = _filter.update { it.copy(fromDate = date) }
    fun updateToDate(date: Long?) = _filter.update { it.copy(toDate = date) }
    fun clearFilters() { _filter.value = TransactionFilter() }
}
