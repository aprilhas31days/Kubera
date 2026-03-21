package org.singhak.kubera.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.data.TransactionRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val transactions = repository.getCurrentMonthTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun syncTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncFromSms()
        }
    }
}
