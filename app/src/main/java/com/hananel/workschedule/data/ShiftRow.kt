package com.hananel.workschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single shift row in the schedule table
 * User can customize shift name and hours
 */
@Entity(tableName = "shift_rows")
data class ShiftRow(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val templateId: Int, // Foreign key to ShiftTemplate
    val orderIndex: Int, // Order in the table (0, 1, 2, ...)
    val shiftName: String, // e.g., "בוקר", "צהריים"
    val shiftHours: String, // e.g., "06:45-15:00"
    val displayName: String // Combined: "בוקר (06:45-15:00)"
)







