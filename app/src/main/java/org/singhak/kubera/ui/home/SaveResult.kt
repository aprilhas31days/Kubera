package org.singhak.kubera.ui.home

sealed interface SaveResult {
    object Success : SaveResult
    object DuplicateError : SaveResult
}
