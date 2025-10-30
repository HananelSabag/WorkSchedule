package com.hananel.workschedule.utils

import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.ShiftDefinitions

/**
 * Schedule generation algorithm implementing all hard and soft rules from specification
 */
object ScheduleGenerator {
    
    data class ShiftAssignment(
        val day: String,
        val shiftId: String,
        val dayIndex: Int
    )
    
    /**
     * Generate weekly schedule according to specification rules
     */
    fun generateSchedule(
        employees: List<Employee>,
        blocks: Map<String, Boolean>,
        canOnlyBlocks: Map<String, Boolean>,
        savingMode: Map<String, Boolean>
    ): Pair<Map<String, List<String>>, List<String>> {
        
        val schedule = mutableMapOf<String, MutableList<String>>()
        val employeeShifts = mutableMapOf<String, MutableList<ShiftAssignment>>()
        val impossibleShifts = mutableListOf<String>()
        
        // Initialize
        employees.forEach { employeeShifts[it.name] = mutableListOf() }
        
        // Collect all shifts for the week
        val allShifts = collectAllShifts(savingMode)
        
        // Sort by difficulty (fewer available employees first)
        allShifts.sortBy { shift ->
            getAvailableEmployees(shift, employees, blocks, canOnlyBlocks, employeeShifts).size
        }
        
        // Assign each shift
        allShifts.forEach { shift ->
            val key = "${shift.day}-${shift.shiftId}"
            
            val availableEmployees = getAvailableEmployees(shift, employees, blocks, canOnlyBlocks, employeeShifts)
            
            if (availableEmployees.isNotEmpty()) {
                // Calculate scores and choose best employee
                val employeeScores = availableEmployees.map { emp ->
                    val score = calculateEmployeeScore(emp, shift, employeeShifts[emp.name]!!)
                    emp to score
                }.sortedBy { it.second }
                
                val chosenEmployee = employeeScores.first().first
                schedule[key] = mutableListOf(chosenEmployee.name)
                employeeShifts[chosenEmployee.name]!!.add(shift)
            } else {
                // No available employees - leave empty and mark as impossible
                schedule[key] = mutableListOf()
                impossibleShifts.add(key)
            }
        }
        
        return schedule to impossibleShifts
    }
    
    private fun collectAllShifts(savingMode: Map<String, Boolean>): MutableList<ShiftAssignment> {
        val shifts = mutableListOf<ShiftAssignment>()
        
        ShiftDefinitions.daysOfWeek.forEachIndexed { dayIndex, day ->
            val dayShifts = ShiftDefinitions.getShiftsForDay(day, savingMode[day] ?: false)
            dayShifts.forEach { shiftInfo ->
                shifts.add(ShiftAssignment(day, shiftInfo.id, dayIndex))
            }
        }
        
        return shifts
    }
    
    private fun getAvailableEmployees(
        shift: ShiftAssignment,
        employees: List<Employee>,
        blocks: Map<String, Boolean>,
        canOnlyBlocks: Map<String, Boolean>,
        employeeShifts: Map<String, List<ShiftAssignment>>
    ): List<Employee> {
        
        return employees.filter { emp ->
            // Check HARD RULE 1: Block Rules
            val blockKey = "${emp.name}-${shift.day}-${shift.shiftId}"
            if (blocks[blockKey] == true) return@filter false
            
            // Check HARD RULE 1: Can Only Rules
            val hasCanOnly = canOnlyBlocks.any { it.key.startsWith("${emp.name}-") && it.value }
            if (hasCanOnly) {
                if (canOnlyBlocks[blockKey] != true) return@filter false
            }
            
            // Check HARD RULE 5: Shabbat Observer Auto-Blocks
            if (emp.shabbatObserver) {
                val shiftKey = "${shift.day}-${shift.shiftId}"
                if (ShiftDefinitions.shabbatBlockedShifts.contains(shiftKey)) {
                    return@filter false
                }
            }
            
            val currentShifts = employeeShifts[emp.name] ?: emptyList()
            
            // Check HARD RULE 2: Maximum Hours (12 hours per day)
            if (violatesMaxHours(shift, currentShifts)) return@filter false
            
            // Check HARD RULE 3: Same-Day Overlaps
            if (hasSameDayOverlap(shift, currentShifts)) return@filter false
            
            // Check HARD RULE 4: Night to Morning = FORBIDDEN
            if (hasNightToMorningConflict(shift, currentShifts)) return@filter false
            
            true
        }
    }
    
    private fun violatesMaxHours(
        newShift: ShiftAssignment,
        currentShifts: List<ShiftAssignment>
    ): Boolean {
        // Get all shifts on the same day
        val sameDayShifts = currentShifts.filter { it.day == newShift.day }
        
        // Calculate total hours if we add this shift
        val newShiftHours = getShiftHours(newShift)
        val currentDayHours = sameDayShifts.sumOf { getShiftHours(it) }
        
        return (currentDayHours + newShiftHours) > 12.0
    }
    
    private fun getShiftHours(shift: ShiftAssignment): Double {
        return when (shift.shiftId) {
            "בוקר" -> 8.25
            "בוקר-ארוך" -> 12.25
            "בוקר-קצר" -> 6.25
            "צהריים" -> 8.25
            "לילה" -> 8.5
            "לילה-ארוך" -> 12.25
            else -> 0.0
        }
    }
    
    private fun hasSameDayOverlap(
        newShift: ShiftAssignment,
        currentShifts: List<ShiftAssignment>
    ): Boolean {
        val sameDayShifts = currentShifts.filter { it.day == newShift.day }
        
        return sameDayShifts.any { existing ->
            when {
                // בוקר + בוקר ארוך (Morning + Long Morning)
                (existing.shiftId == "בוקר" && newShift.shiftId == "בוקר-ארוך") ||
                (existing.shiftId == "בוקר-ארוך" && newShift.shiftId == "בוקר") -> true
                
                // בוקר + צהריים (Morning + Afternoon)
                (existing.shiftId == "בוקר" && newShift.shiftId == "צהריים") ||
                (existing.shiftId == "צהריים" && newShift.shiftId == "בוקר") -> true
                
                // בוקר קצר + צהריים (Short Morning + Afternoon) - only valid combination
                (existing.shiftId == "בוקר-קצר" && newShift.shiftId == "צהריים") ||
                (existing.shiftId == "צהריים" && newShift.shiftId == "בוקר-קצר") -> false
                
                // בוקר ארוך + צהריים (Long Morning + Afternoon)
                (existing.shiftId == "בוקר-ארוך" && newShift.shiftId == "צהריים") ||
                (existing.shiftId == "צהריים" && newShift.shiftId == "בוקר-ארוך") -> true
                
                // צהריים + לילה (Afternoon + Night)
                (existing.shiftId == "צהריים" && newShift.shiftId == "לילה") ||
                (existing.shiftId == "לילה" && newShift.shiftId == "צהריים") -> true
                
                // Any other overlapping combinations
                existing.shiftId == newShift.shiftId -> true
                
                else -> false
            }
        }
    }
    
    private fun hasNightToMorningConflict(
        newShift: ShiftAssignment,
        currentShifts: List<ShiftAssignment>
    ): Boolean {
        // Check if assigning morning shift after night shift (FORBIDDEN)
        if (newShift.shiftId.contains("בוקר")) {
            // Check previous day for night shifts
            val previousDayIndex = if (newShift.dayIndex == 0) 6 else newShift.dayIndex - 1
            val previousDayShifts = currentShifts.filter { it.dayIndex == previousDayIndex }
            
            return previousDayShifts.any { 
                it.shiftId == "לילה" || it.shiftId == "לילה-ארוך" 
            }
        }
        
        // Check if assigning night shift before morning shift next day
        if (newShift.shiftId.contains("לילה")) {
            val nextDayIndex = if (newShift.dayIndex == 6) 0 else newShift.dayIndex + 1
            val nextDayShifts = currentShifts.filter { it.dayIndex == nextDayIndex }
            
            return nextDayShifts.any { 
                it.shiftId.contains("בוקר")
            }
        }
        
        return false
    }
    
    private fun calculateEmployeeScore(
        employee: Employee,
        shift: ShiftAssignment,
        currentShifts: List<ShiftAssignment>
    ): Int {
        var score = currentShifts.size * 10 // Fair distribution: prefer employees with fewer shifts
        
        // Add penalties for SOFT RULES violations
        
        // SOFT RULE 1: Short Rest Periods
        if (hasShortRestPeriod(shift, currentShifts)) {
            score += 5
        }
        
        return score
    }
    
    private fun hasShortRestPeriod(
        newShift: ShiftAssignment,
        currentShifts: List<ShiftAssignment>
    ): Boolean {
        // Night → Afternoon next day (only 8 hours rest)
        if (newShift.shiftId == "צהריים") {
            val previousDayIndex = if (newShift.dayIndex == 0) 6 else newShift.dayIndex - 1
            val previousDayShifts = currentShifts.filter { it.dayIndex == previousDayIndex }
            
            if (previousDayShifts.any { it.shiftId.contains("לילה") }) {
                return true
            }
        }
        
        // Afternoon → Morning next day (not enough rest)
        if (newShift.shiftId.contains("בוקר")) {
            val previousDayIndex = if (newShift.dayIndex == 0) 6 else newShift.dayIndex - 1
            val previousDayShifts = currentShifts.filter { it.dayIndex == previousDayIndex }
            
            if (previousDayShifts.any { it.shiftId == "צהריים" }) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Generate error message for impossible schedules
     */
    fun generateErrorMessage(impossibleShifts: List<String>): String {
        return if (impossibleShifts.isNotEmpty()) {
            """
            ⚠️ לא ניתן ליצור סידור שלם!
            
            יש משמרות שלא ניתן למלא בגלל:
            • יותר מדי חסימות
            • חוסר איזון בין "יכול רק" לחסימות
            • עובדים לא זמינים
            
            הסידור נוצר עם חורים - תצטרך למלא ידנית בעריכה! ✏️
            """.trimIndent()
        } else {
            ""
        }
    }
}


