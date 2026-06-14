package org.singhak.kubera.debug

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.singhak.kubera.BuildConfig

@HiltViewModel
class DebugViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val _smsLoggingEnabled = MutableStateFlow(
        if (BuildConfig.DEBUG) DebugPreferences.isSmsLoggingEnabled(context) else false,
    )
    val smsLoggingEnabled: StateFlow<Boolean> = _smsLoggingEnabled.asStateFlow()

    private val _watchedSender = MutableStateFlow(
        if (BuildConfig.DEBUG) DebugPreferences.getWatchedSender(context) ?: "" else "",
    )
    val watchedSender: StateFlow<String> = _watchedSender.asStateFlow()

    private val _scanDone = MutableStateFlow(false)
    val scanDone: StateFlow<Boolean> = _scanDone.asStateFlow()

    fun setSmsLoggingEnabled(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        _smsLoggingEnabled.value = enabled
        DebugPreferences.setSmsLoggingEnabled(context, enabled)
    }

    fun setWatchedSender(sender: String) {
        _watchedSender.value = sender
    }

    fun saveWatchedSender() {
        if (!BuildConfig.DEBUG) return
        DebugPreferences.setWatchedSender(context, _watchedSender.value)
    }

    fun scanInbox() {
        viewModelScope.launch(Dispatchers.IO) {
            _scanDone.value = false
            SmsLogger.scanInbox(context, contentResolver)
            _scanDone.value = true
        }
    }

    fun clearScanDone() {
        _scanDone.value = false
    }
}
