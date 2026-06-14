package org.singhak.kubera.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.db.AutopayDao
import org.singhak.kubera.model.Autopay
import org.singhak.kubera.sms.SmsReader

class AutopayRepository @Inject constructor(
    private val autopayDao: AutopayDao,
    private val smsReader: SmsReader,
) {
    fun getAll(): Flow<List<Autopay>> = autopayDao.getAll()

    suspend fun upsert(autopay: Autopay) = autopayDao.upsert(autopay)

    suspend fun revoke(merchant: String) = autopayDao.deleteByMerchant(merchant)

    suspend fun backfillFromDate(fromDate: Long) {
        val autopays = smsReader.readAutopays(afterTimestamp = fromDate)
        if (autopays.isNotEmpty()) autopayDao.upsertAll(autopays)
    }
}
