package org.singhak.kubera.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.data.TransactionRepository

class HomeViewModel(private val repository: TransactionRepository) : ViewModel() {

    val transactions = repository.getCurrentMonthTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun syncTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncFromSms()
        }
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
    }
}
