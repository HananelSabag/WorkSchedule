package com.hananel.workschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,              // שם העובד
    val shabbatObserver: Boolean,  // שומר שבת
    val isMitgaber: Boolean = false // מתגבר - גמיש יותר בשיבוץ
)


