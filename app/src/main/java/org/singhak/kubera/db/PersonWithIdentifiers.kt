package org.singhak.kubera.db

import androidx.room.Embedded
import androidx.room.Relation

data class PersonWithIdentifiers(
    @Embedded val person: Person,
    @Relation(parentColumn = "id", entityColumn = "personId")
    val identifiers: List<PersonIdentifier>,
)
