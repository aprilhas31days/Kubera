package org.singhak.kubera.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.singhak.kubera.data.KuberaDatabase
import org.singhak.kubera.data.TransactionDao

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
        ).build()

    @Provides
    fun provideTransactionDao(database: KuberaDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver
}
