package org.singhak.kubera.ui.home

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.repository.TransactionRepository

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    private var originalTransaction: Transaction? = null

    var merchant by mutableStateOf("")
    var amount by mutableStateOf("")
    var type by mutableStateOf(TransactionType.DEBIT)
    var channel by mutableStateOf(TransactionChannel.UPI)
    var account by mutableStateOf("")
    var timestamp by mutableStateOf(0L)
    var bank by mutableStateOf(Bank.INDBNK)
    var category by mutableStateOf(TransactionCategory.OTHER)

    val categoryChanged: Boolean get() = originalTransaction?.category != category

    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult: StateFlow<SaveResult?> = _saveResult.asStateFlow()

    fun load(transaction: Transaction) {
        if (transaction.id != 0L && originalTransaction?.id == transaction.id) return
        originalTransaction = transaction
        merchant = transaction.merchant ?: ""
        amount = if (transaction.id == 0L) "" else "%.2f".format(transaction.amount)
        type = transaction.type
        channel = transaction.channel
        account = transaction.account ?: ""
        timestamp = if (transaction.timestamp == 0L) System.currentTimeMillis() else transaction.timestamp
        bank = transaction.bank
        category = transaction.category
        _saveResult.value = null
    }

    fun updateDate(dateMidnightMillis: Long) {
        val pickedCal = Calendar.getInstance().apply { timeInMillis = dateMidnightMillis }
        val currentCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        currentCal.set(Calendar.YEAR, pickedCal.get(Calendar.YEAR))
        currentCal.set(Calendar.MONTH, pickedCal.get(Calendar.MONTH))
        currentCal.set(Calendar.DAY_OF_MONTH, pickedCal.get(Calendar.DAY_OF_MONTH))
        timestamp = currentCal.timeInMillis
    }

    fun updateTime(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        timestamp = cal.timeInMillis
    }

    fun save(applyRule: Boolean) {
        val orig = originalTransaction ?: return
        val parsedAmount = amount.replace(",", "").toDoubleOrNull() ?: orig.amount
        val edited = orig.copy(
            merchant = merchant.trim().ifBlank { null },
            amount = parsedAmount,
            type = type,
            channel = channel,
            account = account.trim().ifBlank { null },
            timestamp = timestamp,
            bank = bank,
            category = category,
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (orig.id == 0L) {
                    repository.insertManual(edited, applyRule)
                } else {
                    repository.updateTransaction(edited, applyRule)
                }
                _saveResult.value = SaveResult.Success
            } catch (e: SQLiteConstraintException) {
                _saveResult.value = SaveResult.DuplicateError
            }
        }
    }
}
