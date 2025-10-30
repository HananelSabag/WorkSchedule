package com.hananel.workschedule.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Employee::class, Schedule::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun employeeDao(): EmployeeDao
    abstract fun scheduleDao(): ScheduleDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2 - adding isMitgaber field
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE employees ADD COLUMN isMitgaber INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "work_schedule_database"
                )
                    .addMigrations(MIGRATION_1_2)
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
            // Pre-populate with default employees
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.employeeDao())
                }
            }
        }
        
        private suspend fun populateDatabase(employeeDao: EmployeeDao) {
            // Insert default employees from specification
            val defaultEmployees = listOf(
                Employee(name = "מאור", shabbatObserver = false),
                Employee(name = "דוד", shabbatObserver = false),
                Employee(name = "אלכס", shabbatObserver = false),
                Employee(name = "דן", shabbatObserver = false),
                Employee(name = "סלים", shabbatObserver = false),
                Employee(name = "חננאל", shabbatObserver = false)
            )
            
            defaultEmployees.forEach { employee ->
                employeeDao.insertEmployee(employee)
            }
        }
    }
}


