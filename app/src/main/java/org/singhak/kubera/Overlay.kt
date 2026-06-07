package org.singhak.kubera

import org.singhak.kubera.model.Transaction

sealed interface Overlay {
    object Transactions : Overlay
    data class TxnDetail(val t: Transaction) : Overlay
    data class EditTxn(val t: Transaction) : Overlay
    object Settings : Overlay
}
