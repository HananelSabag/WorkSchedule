package com.hananel.workschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekStart: String,         // "2024-10-12"
    val scheduleData: String,      // JSON: Map<"יום-משמרת", List<String>>
    val blocksData: String,        // JSON: Map<"עובד-יום-משמרת", Boolean>
    val canOnlyData: String,       // JSON: Map<"עובד-יום-משמרת", Boolean>
    val savingModeData: String,    // JSON: Map<"יום", Boolean>
    val createdDate: Long          // System.currentTimeMillis()
)


