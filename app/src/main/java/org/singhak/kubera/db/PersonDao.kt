package org.singhak.kubera.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Transaction
    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersonsWithIdentifiers(): Flow<List<PersonWithIdentifiers>>

    @Insert
    suspend fun insertPerson(person: Person): Long

    @Insert
    suspend fun insertIdentifier(identifier: PersonIdentifier)

    @Delete
    suspend fun deletePerson(person: Person)

    @Delete
    suspend fun deleteIdentifier(identifier: PersonIdentifier)
}
