package com.tp.tpapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tp.tpapp.data.dao.AccountDao
import com.tp.tpapp.data.dao.AppDao
import com.tp.tpapp.data.dao.RecordDao
import com.tp.tpapp.data.model.AccountEntity
import com.tp.tpapp.data.model.AppEntity
import com.tp.tpapp.data.model.RecordEntity

@Database(
    entities = [AppEntity::class, AccountEntity::class, RecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
    abstract fun accountDao(): AccountDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE apps ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE apps ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tp_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
