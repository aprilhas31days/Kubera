package org.singhak.kubera.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.repository.PersonRepository

@HiltViewModel
class CircleViewModel @Inject constructor(private val repository: PersonRepository) : ViewModel() {

    val personSummaries = repository.getPersonSummaries(fromTimestamp = 0L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val people = personSummaries
        .map { summaries -> summaries.map { it.person } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
