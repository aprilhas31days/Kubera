package org.singhak.kubera.repository

import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonDao
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.db.TransactionDao
import org.singhak.kubera.model.PersonSummary
import org.singhak.kubera.model.TransactionType

class PersonRepository @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
) {
    private fun monthStart(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun getPersonSummaries(fromTimestamp: Long): Flow<List<PersonSummary>> = combine(
        personDao.getAllPersonsWithIdentifiers(),
        transactionDao.getTransactionsSince(fromTimestamp),
    ) { people, transactions ->
        people.map { pwi ->
            val ids = pwi.identifiers.map { it.identifier.lowercase() }
            val matched = transactions.filter { txn ->
                txn.merchant?.lowercase()?.let { m -> ids.any { id -> m == id } } == true
            }
            PersonSummary(
                person = pwi.person,
                identifiers = pwi.identifiers,
                sent = matched.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
                received = matched.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
            )
        }
    }

    fun getPeople(): Flow<List<Person>> =
        personDao.getAllPersonsWithIdentifiers().map { list -> list.map { it.person } }

    suspend fun addPerson(name: String): Long =
        personDao.insertPerson(Person(name = name.trim()))

    suspend fun addPersonWithIdentifier(name: String, identifier: String) {
        val personId = personDao.insertPerson(Person(name = name.trim()))
        personDao.insertIdentifier(PersonIdentifier(personId = personId, identifier = identifier.trim()))
    }

    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    suspend fun addIdentifier(personId: Long, identifier: String) =
        personDao.insertIdentifier(PersonIdentifier(personId = personId, identifier = identifier.trim()))

    suspend fun deleteIdentifier(identifier: PersonIdentifier) = personDao.deleteIdentifier(identifier)
}
