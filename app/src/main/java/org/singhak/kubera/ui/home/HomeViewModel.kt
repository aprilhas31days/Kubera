package org.singhak.kubera.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.data.TransactionRepository


sealed interface BackfillState {
    data object Idle : BackfillState
    data object Loading : BackfillState
    data object NoResults : BackfillState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val transactions = repository.getCurrentMonthTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _backfillState = MutableStateFlow<BackfillState>(BackfillState.Idle)
    val backfillState: StateFlow<BackfillState> = _backfillState.asStateFlow()

    fun backfillFromDate(fromDate: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _backfillState.value = BackfillState.Loading
            val found = repository.backfillFromDate(fromDate)
            _backfillState.value = if (found) BackfillState.Idle else BackfillState.NoResults
        }
    }
}
