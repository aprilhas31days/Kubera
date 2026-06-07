package org.singhak.kubera.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.repository.TransactionRepository
import org.singhak.kubera.repository.monthRange

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: TransactionRepository) :
    ViewModel() {

    private val now = Calendar.getInstance()
    private val _year = MutableStateFlow(now.get(Calendar.YEAR))
    private val _month = MutableStateFlow(now.get(Calendar.MONTH))

    val year: StateFlow<Int> = _year.asStateFlow()
    val month: StateFlow<Int> = _month.asStateFlow()

    val isCurrentMonth: StateFlow<Boolean> = combine(_year, _month) { y, m ->
        val cur = Calendar.getInstance()
        y == cur.get(Calendar.YEAR) && m == cur.get(Calendar.MONTH)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _range = combine(_year, _month) { y, m -> monthRange(y, m) }

    val monthSummary: StateFlow<MonthSummary> = _range
        .flatMapLatest { (from, to) -> repository.getMonthSummary(from, to) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary(0.0, 0.0, 0))

    val transactions: StateFlow<List<Transaction>?> = _range
        .flatMapLatest { (from, to) -> repository.getTransactionsForMonth(from, to) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val categoryBreakdown: StateFlow<List<CategorySpend>> = _range
        .flatMapLatest { (from, to) -> repository.getCategoryBreakdown(from, to) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _backfillState = MutableStateFlow<BackfillState>(BackfillState.Idle)
    val backfillState: StateFlow<BackfillState> = _backfillState.asStateFlow()

    fun prevMonth() {
        if (_month.value == 0) {
            _month.value = 11
            _year.value = _year.value - 1
        } else {
            _month.value = _month.value - 1
        }
    }

    fun nextMonth() {
        val cur = Calendar.getInstance()
        val atCurrent = _year.value == cur.get(Calendar.YEAR) && _month.value == cur.get(Calendar.MONTH)
        if (atCurrent) return
        if (_month.value == 11) {
            _month.value = 0
            _year.value = _year.value + 1
        } else {
            _month.value = _month.value + 1
        }
    }

    fun backfillFromDate(fromDate: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _backfillState.value = BackfillState.Loading
            val found = repository.backfillFromDate(fromDate)
            _backfillState.value = if (found) BackfillState.Idle else BackfillState.NoResults
        }
    }
}
