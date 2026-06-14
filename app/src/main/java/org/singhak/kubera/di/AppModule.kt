package org.singhak.kubera.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.singhak.kubera.db.AutopayDao
import org.singhak.kubera.db.CategoryRuleDao
import org.singhak.kubera.db.KuberaDatabase
import org.singhak.kubera.db.PersonDao
import org.singhak.kubera.db.TransactionDao
import org.singhak.kubera.db.seedSystemRules
import org.singhak.kubera.sms.SmsParser

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KuberaDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            KuberaDatabase::class.java,
            "kubera.db"
        )
            .addMigrations(KuberaDatabase.MIGRATION_1_2, KuberaDatabase.MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    seedSystemRules(db)
                }
            })
            .build()

    @Provides
    fun provideTransactionDao(database: KuberaDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryRuleDao(database: KuberaDatabase): CategoryRuleDao =
        database.categoryRuleDao()

    @Provides
    fun providePersonDao(database: KuberaDatabase): PersonDao = database.personDao()

    @Provides
    fun provideAutopayDao(database: KuberaDatabase): AutopayDao = database.autopayDao()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    @Singleton
    fun provideSmsParser(@ApplicationContext context: Context): SmsParser =
        SmsParser.fromAssets(context)
}
