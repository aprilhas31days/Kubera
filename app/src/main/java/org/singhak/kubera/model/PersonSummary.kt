package org.singhak.kubera.model

import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonIdentifier

data class PersonSummary(
    val person: Person,
    val identifiers: List<PersonIdentifier>,
    val sent: Double,
    val received: Double,
) {
    val net: Double get() = received - sent
}
