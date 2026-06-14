package org.singhak.kubera.ui.autopay

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.Autopay
import org.singhak.kubera.repository.AutopayRepository

@HiltViewModel
class AutopayViewModel @Inject constructor(repository: AutopayRepository) : ViewModel() {
    val autopays: Flow<List<Autopay>> = repository.getAll()
}
