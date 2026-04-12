package org.singhak.kubera.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.singhak.kubera.model.Transaction

@Database(entities = [Transaction::class, CategoryRule::class], version = 1)
abstract class KuberaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryRuleDao(): CategoryRuleDao
}

fun seedSystemRules(db: SupportSQLiteDatabase) {
    systemCategoryKeywords().forEach { (keyword, category) ->
        db.execSQL(
            "INSERT INTO category_rules (keyword, category, source) VALUES (?, ?, 'SYSTEM')",
            arrayOf(keyword, category.name)
        )
    }
}
