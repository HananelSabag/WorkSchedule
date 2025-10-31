package com.hananel.workschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a day column in the schedule table
 * User can enable/disable days (e.g., skip Sunday or Saturday)
 */
@Entity(tableName = "day_columns")
data class DayColumn(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val templateId: Int, // Foreign key to ShiftTemplate
    val dayIndex: Int, // 0=Sunday, 1=Monday, ..., 6=Saturday
    val dayNameHebrew: String, // "ראשון", "שני", etc.
    val dayNameEnglish: String, // "Sunday", "Monday", etc.
    val isEnabled: Boolean = true // Can be disabled by user
)


