package org.singhak.kubera.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.singhak.kubera.model.Transaction

@Database(entities = [Transaction::class], version = 1)
abstract class KuberaDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var instance: KuberaDatabase? = null

        fun getInstance(context: Context): KuberaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                KuberaDatabase::class.java,
                "kubera.db"
            ).build().also { instance = it }
        }
    }
}
