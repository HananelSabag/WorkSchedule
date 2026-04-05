package com.hananel.workschedule.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Employee::class,
        Schedule::class,
        ShiftTemplate::class,
        ShiftRow::class,
        DayColumn::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun employeeDao(): EmployeeDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun shiftTemplateDao(): ShiftTemplateDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2 - adding isMitgaber field
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE employees ADD COLUMN isMitgaber INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // Migration from version 2 to 3 - adding shift template tables
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create shift_templates table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS shift_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        rowCount INTEGER NOT NULL,
                        columnCount INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdDate INTEGER NOT NULL,
                        lastModified INTEGER NOT NULL
                    )
                """)
                
                // Create shift_rows table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS shift_rows (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateId INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        shiftName TEXT NOT NULL,
                        shiftHours TEXT NOT NULL,
                        displayName TEXT NOT NULL
                    )
                """)
                
                // Create day_columns table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS day_columns (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateId INTEGER NOT NULL,
                        dayIndex INTEGER NOT NULL,
                        dayNameHebrew TEXT NOT NULL,
                        dayNameEnglish TEXT NOT NULL,
                        isEnabled INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // Migration from version 3 to 4 - adding note fields to shift_rows and day_columns
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE shift_rows ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE day_columns ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "work_schedule_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            // Database created - no default employees
            // Users will add their own employees via Employee Management screen
        }
        
        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onOpen(db)
            // No automatic template creation - user must create their own template first
            // This ensures users customize the table to their needs before creating schedules
            validateSchema(db)
        }

        /**
         * Validates that the actual SQLite schema matches the expected entity definitions.
         * Runs once on every DB open — safe, read-only PRAGMA queries.
         * Logs a clear PASS / FAIL per table to Logcat (tag: DB_SCHEMA).
         */
        private fun validateSchema(db: SupportSQLiteDatabase) {
            val expected = mapOf(
                "employees"       to setOf("id", "name", "shabbatObserver", "isMitgaber"),
                "schedules"       to setOf("id", "weekStart", "scheduleData", "blocksData", "canOnlyData", "savingModeData", "createdDate"),
                "shift_templates" to setOf("id", "name", "rowCount", "columnCount", "isActive", "createdDate", "lastModified"),
                "shift_rows"      to setOf("id", "templateId", "orderIndex", "shiftName", "shiftHours", "displayName", "note"),
                "day_columns"     to setOf("id", "templateId", "dayIndex", "dayNameHebrew", "dayNameEnglish", "isEnabled", "note")
            )

            var allOk = true
            expected.forEach { (table, expectedCols) ->
                val cursor = db.query("PRAGMA table_info($table)")
                val actualCols = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    actualCols += cursor.getString(cursor.getColumnIndexOrThrow("name"))
                }
                cursor.close()

                val missing = expectedCols - actualCols
                val extra   = actualCols - expectedCols
                if (missing.isEmpty()) {
                    Log.d("DB_SCHEMA", "✅ $table — OK (${actualCols.size} columns)")
                } else {
                    Log.e("DB_SCHEMA", "❌ $table — MISSING columns: $missing")
                    allOk = false
                }
                if (extra.isNotEmpty()) {
                    Log.w("DB_SCHEMA", "⚠️  $table — extra columns (harmless): $extra")
                }
            }
            if (allOk) Log.d("DB_SCHEMA", "✅ Schema validation PASSED — safe to use")
            else       Log.e("DB_SCHEMA", "❌ Schema validation FAILED — check migrations!")
        }
    }
}


