package org.singhak.kubera.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.db.CategoryRule
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.repository.TransactionRepository

@HiltViewModel
class RulesViewModel @Inject constructor(private val repository: TransactionRepository) : ViewModel() {

    val userRules = repository.getUserRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRule(keyword: String, category: TransactionCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUserRule(keyword, category)
        }
    }

    fun deleteRule(rule: CategoryRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserRule(rule)
        }
    }
}
