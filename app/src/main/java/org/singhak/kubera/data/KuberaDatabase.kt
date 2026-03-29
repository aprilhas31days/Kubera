package org.singhak.kubera.data

import androidx.room.Database
import androidx.room.RoomDatabase
import org.singhak.kubera.model.Transaction

@Database(entities = [Transaction::class], version = 2)
abstract class KuberaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
