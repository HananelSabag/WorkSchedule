package com.hananel.workschedule.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftTemplateDao {
    
    // Template operations
    @Query("SELECT * FROM shift_templates WHERE isActive = 1 LIMIT 1")
    fun getActiveTemplate(): Flow<ShiftTemplate?>
    
    @Query("SELECT * FROM shift_templates WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTemplateSync(): ShiftTemplate?
    
    @Query("SELECT * FROM shift_templates")
    fun getAllTemplates(): Flow<List<ShiftTemplate>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ShiftTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: ShiftTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: ShiftTemplate)
    
    @Query("UPDATE shift_templates SET isActive = 0")
    suspend fun deactivateAllTemplates()
    
    // Shift rows operations
    @Query("SELECT * FROM shift_rows WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun getShiftRows(templateId: Int): Flow<List<ShiftRow>>
    
    @Query("SELECT * FROM shift_rows WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getShiftRowsSync(templateId: Int): List<ShiftRow>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftRow(shiftRow: ShiftRow): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftRows(shiftRows: List<ShiftRow>)
    
    @Update
    suspend fun updateShiftRow(shiftRow: ShiftRow)
    
    @Delete
    suspend fun deleteShiftRow(shiftRow: ShiftRow)
    
    @Query("DELETE FROM shift_rows WHERE templateId = :templateId")
    suspend fun deleteAllShiftRows(templateId: Int)
    
    // Day columns operations
    @Query("SELECT * FROM day_columns WHERE templateId = :templateId ORDER BY dayIndex ASC")
    fun getDayColumns(templateId: Int): Flow<List<DayColumn>>
    
    @Query("SELECT * FROM day_columns WHERE templateId = :templateId ORDER BY dayIndex ASC")
    suspend fun getAllDayColumnsSync(templateId: Int): List<DayColumn>
    
    @Query("SELECT * FROM day_columns WHERE templateId = :templateId AND isEnabled = 1 ORDER BY dayIndex ASC")
    suspend fun getEnabledDayColumnsSync(templateId: Int): List<DayColumn>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayColumn(dayColumn: DayColumn): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayColumns(dayColumns: List<DayColumn>)
    
    @Update
    suspend fun updateDayColumn(dayColumn: DayColumn)
    
    @Delete
    suspend fun deleteDayColumn(dayColumn: DayColumn)
    
    @Query("DELETE FROM day_columns WHERE templateId = :templateId")
    suspend fun deleteAllDayColumns(templateId: Int)
    
    // Combined operations
    @Transaction
    suspend fun createCompleteTemplate(
        template: ShiftTemplate,
        shiftRows: List<ShiftRow>,
        dayColumns: List<DayColumn>
    ): Long {
        val templateId = insertTemplate(template).toInt()
        
        // Update foreign keys
        val rowsWithId = shiftRows.map { it.copy(templateId = templateId) }
        val columnsWithId = dayColumns.map { it.copy(templateId = templateId) }
        
        insertShiftRows(rowsWithId)
        insertDayColumns(columnsWithId)
        
        return templateId.toLong()
    }
    
    @Transaction
    suspend fun deleteCompleteTemplate(templateId: Int) {
        deleteAllShiftRows(templateId)
        deleteAllDayColumns(templateId)
        // Template will be deleted separately if needed
    }
}


