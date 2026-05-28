package org.singhak.kubera.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.singhak.kubera.repository.TransactionRepository

@HiltViewModel
class AnalysisViewModel @Inject constructor(repository: TransactionRepository) : ViewModel() {

    private val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val sixMonthsAgo = Calendar.getInstance().apply {
        add(Calendar.MONTH, -5)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val categoryBreakdown = repository.getCurrentMonthCategoryBreakdown()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTrend = repository.getMonthlySpend(sixMonthsAgo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val topMerchants = repository.getTopMerchants(monthStart)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
