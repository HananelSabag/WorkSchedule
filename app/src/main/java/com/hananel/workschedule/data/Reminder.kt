package com.hananel.workschedule.data

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: Int,         // Calendar.SUNDAY=1 ... SATURDAY=7
    val times: List<ReminderTime>,
    val isRecurring: Boolean = true,
    val isEnabled: Boolean = true
)

data class ReminderTime(
    val hour: Int,
    val minute: Int
)
