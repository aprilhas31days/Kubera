package org.singhak.kubera.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.singhak.kubera.model.Transaction

@Database(
    entities = [Transaction::class, CategoryRule::class, Person::class, PersonIdentifier::class],
    version = 2,
)
abstract class KuberaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun personDao(): PersonDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `persons` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `person_identifiers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `personId` INTEGER NOT NULL,
                        `identifier` TEXT NOT NULL
                    )"""
                )
            }
        }
    }
}

fun seedSystemRules(db: SupportSQLiteDatabase) {
    systemCategoryKeywords().forEach { (keyword, category) ->
        db.execSQL(
            "INSERT INTO category_rules (keyword, category, source) VALUES (?, ?, 'SYSTEM')",
            arrayOf(keyword, category.name)
        )
    }
}
