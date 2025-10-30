package com.hananel.workschedule.data

// Shift definitions according to specification
data class ShiftInfo(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    val hours: Double
) {
    // Extract start hour from startTime (e.g., "06:45" -> 6)
    val startHour: Int
        get() = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 7
}

object ShiftDefinitions {
    
    // Days of the week in Hebrew
    val daysOfWeek = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
    
    // Regular day shifts (Sunday-Thursday)
    val regularDayShifts = listOf(
        ShiftInfo("בוקר", "בוקר", "06:45", "15:00", 8.25),
        ShiftInfo("בוקר-ארוך", "בוקר ארוך", "06:45", "19:00", 12.25),
        ShiftInfo("צהריים", "צהריים", "14:45", "23:00", 8.25),
        ShiftInfo("לילה", "לילה", "22:30", "07:00", 8.5)
    )
    
    // Regular day shifts in saving mode (Sunday-Thursday)
    val regularDaySavingShifts = listOf(
        ShiftInfo("בוקר-ארוך", "בוקר ארוך", "06:45", "19:00", 12.25),
        ShiftInfo("לילה-ארוך", "לילה ארוך", "18:45", "07:00", 12.25)
    )
    
    // Friday shifts (normal mode) - יש גם בוקר רגיל וגם בוקר קצר!
    val fridayShifts = listOf(
        ShiftInfo("בוקר", "בוקר", "06:45", "15:00", 8.25),        // בוקר רגיל
        ShiftInfo("בוקר-קצר", "בוקר קצר", "06:45", "13:00", 6.25), // בוקר קצר
        ShiftInfo("צהריים", "צהריים", "14:45", "23:00", 8.25),
        ShiftInfo("לילה", "לילה", "22:30", "07:00", 8.5)
    )
    
    // Friday shifts (saving mode) - במצב חסכון מה צריך להיות?
    val fridaySavingShifts = listOf(
        ShiftInfo("בוקר", "בוקר", "06:45", "15:00", 8.25),        // בוקר רגיל
        ShiftInfo("בוקר-קצר", "בוקר קצר", "06:45", "13:00", 6.25), // בוקר קצר
        ShiftInfo("לילה-ארוך", "לילה ארוך", "18:45", "07:00", 12.25)
    )
    
    // Saturday shifts (no saving mode available)
    val saturdayShifts = listOf(
        ShiftInfo("בוקר", "בוקר", "06:45", "15:00", 8.25),
        ShiftInfo("צהריים", "צהריים", "14:45", "23:00", 8.25),
        ShiftInfo("לילה", "לילה", "22:30", "07:00", 8.5)
    )
    
    // Shabbat observer blocked shifts - According to user request
    val shabbatBlockedShifts = setOf(
        "שישי-צהריים",
        "שישי-לילה",
        "שבת-בוקר", 
        "שבת-צהריים"
    )
    
    /**
     * Get shifts for a specific day based on saving mode
     */
    fun getShiftsForDay(day: String, savingMode: Boolean): List<ShiftInfo> {
        return when (day) {
            "ראשון", "שני", "שלישי", "רביעי", "חמישי" -> {
                if (savingMode) regularDaySavingShifts else regularDayShifts
            }
            "שישי" -> {
                if (savingMode) fridaySavingShifts else fridayShifts
            }
            "שבת" -> saturdayShifts // Saturday never has saving mode
            else -> emptyList()
        }
    }
    
    /**
     * Get all shift-day combinations for the week
     */
    fun getAllShiftKeys(savingModeMap: Map<String, Boolean>): List<String> {
        val keys = mutableListOf<String>()
        daysOfWeek.forEach { day ->
            val shifts = getShiftsForDay(day, savingModeMap[day] ?: false)
            shifts.forEach { shift ->
                keys.add("$day-${shift.id}")
            }
        }
        return keys
    }
}


