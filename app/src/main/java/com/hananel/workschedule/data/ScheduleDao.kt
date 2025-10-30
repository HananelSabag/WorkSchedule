package com.hananel.workschedule.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    
    @Query("SELECT * FROM schedules WHERE weekStart != '__TEMP_DRAFT__' ORDER BY createdDate DESC")
    fun getAllSchedules(): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): Schedule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long
    
    @Update
    suspend fun updateSchedule(schedule: Schedule)
    
    @Delete
    suspend fun deleteSchedule(schedule: Schedule)
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Int)
    
    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getScheduleCount(): Int
    
    // Smart save system - check for duplicates by weekStart
    @Query("SELECT * FROM schedules WHERE weekStart = :weekStart ORDER BY createdDate DESC")
    suspend fun getSchedulesByWeekStart(weekStart: String): List<Schedule>
    
    @Query("SELECT COUNT(*) FROM schedules WHERE weekStart = :weekStart")
    suspend fun getScheduleCountByWeekStart(weekStart: String): Int
}


