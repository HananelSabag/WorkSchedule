package com.hananel.workschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Main template for the schedule table
 * Contains metadata about the custom schedule configuration
 */
@Entity(tableName = "shift_templates")
data class ShiftTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "תבנית ברירת מחדל", // Template name
    val rowCount: Int, // Number of shift rows (2-8)
    val columnCount: Int, // Number of enabled day columns (4-7)
    val isActive: Boolean = true, // Only one template can be active
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)


