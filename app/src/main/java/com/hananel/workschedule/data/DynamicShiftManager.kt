package com.hananel.workschedule.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Manager for dynamic shift templates
 * Replaces the hardcoded ShiftDefinitions with flexible database-driven configuration
 */
class DynamicShiftManager(private val dao: ShiftTemplateDao) {
    
    // Flow that combines template, rows, and columns
    fun getActiveTemplateWithData(): Flow<TemplateData?> = combine(
        dao.getActiveTemplate(),
        dao.getActiveTemplate()
    ) { template, _ ->
        template?.let {
            val rows = dao.getShiftRowsSync(it.id)
            val columns = dao.getEnabledDayColumnsSync(it.id)
            TemplateData(it, rows, columns)
        }
    }
    
    suspend fun getActiveTemplateDataSync(): TemplateData? {
        val template = dao.getActiveTemplateSync() ?: return null
        val rows = dao.getShiftRowsSync(template.id)
        val columns = dao.getEnabledDayColumnsSync(template.id)
        return TemplateData(template, rows, columns)
    }
    
    // Create default template (similar to current hardcoded shifts)
    suspend fun createDefaultTemplate(): Long {
        // Deactivate any existing templates
        dao.deactivateAllTemplates()
        
        val template = ShiftTemplate(
            name = "תבנית ברירת מחדל",
            rowCount = 3,
            columnCount = 7,
            isActive = true
        )
        
        val shiftRows = listOf(
            ShiftRow(
                templateId = 0, // Will be updated
                orderIndex = 0,
                shiftName = "בוקר",
                shiftHours = "07:00-15:00",
                displayName = "בוקר (07:00-15:00)"
            ),
            ShiftRow(
                templateId = 0,
                orderIndex = 1,
                shiftName = "צהריים",
                shiftHours = "15:00-23:00",
                displayName = "צהריים (15:00-23:00)"
            ),
            ShiftRow(
                templateId = 0,
                orderIndex = 2,
                shiftName = "לילה",
                shiftHours = "23:00-07:00",
                displayName = "לילה (23:00-07:00)"
            )
        )
        
        val dayColumns = listOf(
            DayColumn(templateId = 0, dayIndex = 0, dayNameHebrew = "ראשון", dayNameEnglish = "Sunday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 1, dayNameHebrew = "שני", dayNameEnglish = "Monday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 2, dayNameHebrew = "שלישי", dayNameEnglish = "Tuesday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 3, dayNameHebrew = "רביעי", dayNameEnglish = "Wednesday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 4, dayNameHebrew = "חמישי", dayNameEnglish = "Thursday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 5, dayNameHebrew = "שישי", dayNameEnglish = "Friday", isEnabled = true),
            DayColumn(templateId = 0, dayIndex = 6, dayNameHebrew = "שבת", dayNameEnglish = "Saturday", isEnabled = true)
        )
        
        return dao.createCompleteTemplate(template, shiftRows, dayColumns)
    }
    
    // Helper functions for backward compatibility
    suspend fun getShiftNames(): List<String> {
        val data = getActiveTemplateDataSync() ?: return emptyList()
        return data.shiftRows.map { it.shiftName }
    }
    
    suspend fun getShiftDisplayNames(): List<String> {
        val data = getActiveTemplateDataSync() ?: return emptyList()
        return data.shiftRows.map { it.displayName }
    }
    
    suspend fun getDayNames(): List<String> {
        val data = getActiveTemplateDataSync() ?: return emptyList()
        return data.dayColumns.map { it.dayNameHebrew }
    }
    
    suspend fun getEnabledDayIndices(): List<Int> {
        val data = getActiveTemplateDataSync() ?: return emptyList()
        return data.dayColumns.map { it.dayIndex }
    }
}

// Data class to hold complete template information
data class TemplateData(
    val template: ShiftTemplate,
    val shiftRows: List<ShiftRow>,
    val dayColumns: List<DayColumn>
) {
    val rowCount: Int get() = shiftRows.size
    val columnCount: Int get() = dayColumns.size
    
    fun getShiftName(rowIndex: Int): String? {
        return shiftRows.getOrNull(rowIndex)?.shiftName
    }
    
    fun getShiftDisplayName(rowIndex: Int): String? {
        return shiftRows.getOrNull(rowIndex)?.displayName
    }
    
    fun getDayName(colIndex: Int): String? {
        return dayColumns.getOrNull(colIndex)?.dayNameHebrew
    }
    
    fun getDayIndex(colIndex: Int): Int? {
        return dayColumns.getOrNull(colIndex)?.dayIndex
    }
}


