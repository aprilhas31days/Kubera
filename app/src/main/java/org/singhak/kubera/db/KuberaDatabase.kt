package org.singhak.kubera.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.singhak.kubera.model.Autopay
import org.singhak.kubera.model.Transaction

@Database(
    entities = [Transaction::class, CategoryRule::class, Person::class, PersonIdentifier::class, Autopay::class],
    version = 3,
)
abstract class KuberaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun personDao(): PersonDao
    abstract fun autopayDao(): AutopayDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `autopays` (
                        `merchant` TEXT NOT NULL PRIMARY KEY,
                        `amount` REAL NOT NULL,
                        `bank` TEXT NOT NULL,
                        `nextDueDate` INTEGER NOT NULL
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
