package org.singhak.kubera.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_identifiers")
data class PersonIdentifier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val identifier: String,
)
