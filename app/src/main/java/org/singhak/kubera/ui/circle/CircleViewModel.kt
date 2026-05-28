package org.singhak.kubera.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.repository.PersonRepository

enum class PeriodFilter(val label: String) {
    THIS_MONTH("THIS MONTH"),
    ALL_TIME("ALL TIME"),
}

@HiltViewModel
class CircleViewModel @Inject constructor(private val repository: PersonRepository) : ViewModel() {

    val selectedFilter = MutableStateFlow(PeriodFilter.THIS_MONTH)

    val personSummaries = selectedFilter
        .flatMapLatest { filter ->
            repository.getPersonSummaries(filter.toTimestamp())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val people = repository.getPeople()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(filter: PeriodFilter) {
        selectedFilter.value = filter
    }

    fun addPerson(name: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.addPerson(name) }
    }

    fun addPersonWithIdentifier(name: String, identifier: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.addPersonWithIdentifier(name, identifier) }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch(Dispatchers.IO) { repository.deletePerson(person) }
    }

    fun addIdentifier(personId: Long, identifier: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.addIdentifier(personId, identifier) }
    }

    fun deleteIdentifier(identifier: PersonIdentifier) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteIdentifier(identifier) }
    }
}

private fun PeriodFilter.toTimestamp(): Long = when (this) {
    PeriodFilter.THIS_MONTH -> Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    PeriodFilter.ALL_TIME -> 0L
}
