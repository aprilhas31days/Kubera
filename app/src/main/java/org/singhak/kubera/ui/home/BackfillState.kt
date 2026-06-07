package org.singhak.kubera.ui.home

sealed interface BackfillState {
    object Idle : BackfillState
    object Loading : BackfillState
    object NoResults : BackfillState
}
