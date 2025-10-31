package com.hananel.workschedule.utils

import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.data.ShiftRow
import com.hananel.workschedule.data.DayColumn
import java.time.LocalTime
import java.time.Duration

/**
 * GENERIC Schedule Generator - Works with ANY shift template!
 * 
 * Parses shift hours from template and calculates:
 * - Overlaps based on actual times
 * - Rest periods between shifts
 * - Night/morning conflicts
 * - Maximum hours per day
 * 
 * No hardcoded shift names - completely dynamic!
 */
object GenericScheduleGenerator {
    
    data class ShiftAssignment(
        val employeeName: String,
        val day: String,
        val dayIndex: Int,
        val shift: ParsedShift
    )
    
    data class ParsedShift(
        val shiftName: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val durationHours: Double,
        val isNightShift: Boolean,
        val originalHoursString: String
    )
    
    /**
     * Main generation function
     */
    fun generateSchedule(
        employees: List<Employee>,
        blocks: Map<String, Boolean>,
        canOnlyBlocks: Map<String, Boolean>,
        templateData: TemplateData
    ): Pair<Map<String, List<String>>, List<String>> {
        
        val schedule = mutableMapOf<String, MutableList<String>>()
        val employeeShifts = mutableMapOf<String, MutableList<ShiftAssignment>>()
        val impossibleShifts = mutableListOf<String>()
        
        // Initialize
        employees.forEach { employeeShifts[it.name] = mutableListOf() }
        
        // Parse all shifts from template
        val parsedShifts = parseAllShifts(templateData)
        
        // Sort by difficulty (fewer available employees first)
        val sortedShifts = parsedShifts.sortedBy { (day, dayIndex, shift) ->
            getAvailableEmployees(
                day, dayIndex, shift, employees, blocks, canOnlyBlocks, 
                employeeShifts, templateData
            ).size
        }
        
        // Assign each shift
        sortedShifts.forEach { (day, dayIndex, shift) ->
            val key = "${day}-${shift.shiftName}"
            
            val availableEmployees = getAvailableEmployees(
                day, dayIndex, shift, employees, blocks, canOnlyBlocks,
                employeeShifts, templateData
            )
            
            if (availableEmployees.isNotEmpty()) {
                // Calculate scores and choose best employee
                val employeeScores = availableEmployees.map { emp ->
                    val score = calculateEmployeeScore(
                        emp, day, dayIndex, shift, employeeShifts[emp.name]!!
                    )
                    emp to score
                }.sortedBy { it.second }
                
                val chosenEmployee = employeeScores.first().first
                schedule[key] = mutableListOf(chosenEmployee.name)
                employeeShifts[chosenEmployee.name]!!.add(
                    ShiftAssignment(chosenEmployee.name, day, dayIndex, shift)
                )
            } else {
                // No available employees - leave empty
                schedule[key] = mutableListOf()
                impossibleShifts.add(key)
            }
        }
        
        return schedule to impossibleShifts
    }
    
    /**
     * Parse all shifts from template into structured data
     */
    private fun parseAllShifts(templateData: TemplateData): List<Triple<String, Int, ParsedShift>> {
        val shifts = mutableListOf<Triple<String, Int, ParsedShift>>()
        
        templateData.dayColumns.filter { it.isEnabled }.forEach { dayColumn ->
            templateData.shiftRows.forEach { shiftRow ->
                val parsedShift = parseShiftHours(shiftRow)
                shifts.add(Triple(dayColumn.dayNameHebrew, dayColumn.dayIndex, parsedShift))
            }
        }
        
        return shifts
    }
    
    /**
     * Parse shift hours string into structured time data
     * Format: "HH:MM-HH:MM" or "H:MM-H:MM"
     */
    private fun parseShiftHours(shiftRow: ShiftRow): ParsedShift {
        return try {
            val parts = shiftRow.shiftHours.split("-")
            if (parts.size != 2) {
                // Invalid format - return default 8-hour shift
                return ParsedShift(
                    shiftName = shiftRow.shiftName,
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(16, 0),
                    durationHours = 8.0,
                    isNightShift = false,
                    originalHoursString = shiftRow.shiftHours
                )
            }
            
            val startParts = parts[0].trim().split(":")
            val endParts = parts[1].trim().split(":")
            
            val startHour = startParts[0].toIntOrNull() ?: 8
            val startMin = startParts.getOrNull(1)?.toIntOrNull() ?: 0
            val endHour = endParts[0].toIntOrNull() ?: 16
            val endMin = endParts.getOrNull(1)?.toIntOrNull() ?: 0
            
            val startTime = LocalTime.of(startHour, startMin)
            var endTime = LocalTime.of(endHour, endMin)
            
            // Calculate duration (handle overnight shifts)
            var duration = if (endTime.isAfter(startTime)) {
                Duration.between(startTime, endTime).toMinutes() / 60.0
            } else {
                // Overnight shift (e.g., 23:00-07:00)
                (Duration.between(startTime, LocalTime.MAX).toMinutes() + 
                 Duration.between(LocalTime.MIN, endTime).toMinutes() + 1) / 60.0
            }
            
            // Determine if night shift (starts after 18:00 or ends before 06:00)
            val isNightShift = startHour >= 18 || endHour <= 6
            
            ParsedShift(
                shiftName = shiftRow.shiftName,
                startTime = startTime,
                endTime = endTime,
                durationHours = duration,
                isNightShift = isNightShift,
                originalHoursString = shiftRow.shiftHours
            )
            
        } catch (e: Exception) {
            // Parsing failed - return default
            ParsedShift(
                shiftName = shiftRow.shiftName,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(16, 0),
                durationHours = 8.0,
                isNightShift = false,
                originalHoursString = shiftRow.shiftHours
            )
        }
    }
    
    /**
     * Get available employees for a shift (all rules applied)
     */
    private fun getAvailableEmployees(
        day: String,
        dayIndex: Int,
        shift: ParsedShift,
        employees: List<Employee>,
        blocks: Map<String, Boolean>,
        canOnlyBlocks: Map<String, Boolean>,
        employeeShifts: Map<String, List<ShiftAssignment>>,
        templateData: TemplateData
    ): List<Employee> {
        
        return employees.filter { emp ->
            // HARD RULE 1: Check blocks
            val blockKey = "${emp.name}-${day}-${shift.shiftName}"
            if (blocks[blockKey] == true) return@filter false
            
            // HARD RULE 2: Check "can only" blocks
            val hasCanOnly = canOnlyBlocks.any { it.key.startsWith("${emp.name}-") && it.value }
            if (hasCanOnly) {
                if (canOnlyBlocks[blockKey] != true) return@filter false
            }
            
            // HARD RULE 3: Shabbat Observer - block Friday after 15:00 and all Saturday
            if (emp.shabbatObserver && !emp.isMitgaber) {
                val isShabbatShift = when {
                    // Friday shifts starting after 15:00
                    day.contains("שישי") && shift.startTime.hour >= 15 -> true
                    // All Saturday shifts
                    day.contains("שבת") -> true
                    else -> false
                }
                if (isShabbatShift) return@filter false
            }
            
            val currentShifts = employeeShifts[emp.name] ?: emptyList()
            
            // HARD RULE 4: Maximum hours per day (12h for normal, 16h for Mitgaber)
            val maxHours = if (emp.isMitgaber) 16.0 else 12.0
            if (violatesMaxHours(day, shift, currentShifts, maxHours)) return@filter false
            
            // HARD RULE 5: Same-day overlaps (cannot work overlapping shifts)
            if (hasSameDayOverlap(day, shift, currentShifts)) return@filter false
            
            // HARD RULE 6: Night to morning conflict (need rest!)
            val minRestHours = if (emp.isMitgaber) 8.0 else 11.0
            if (hasInsufficientRest(dayIndex, shift, currentShifts, minRestHours)) return@filter false
            
            true
        }
    }
    
    /**
     * Check if adding shift violates max hours per day
     */
    private fun violatesMaxHours(
        day: String,
        newShift: ParsedShift,
        currentShifts: List<ShiftAssignment>,
        maxHours: Double
    ): Boolean {
        val sameDayShifts = currentShifts.filter { it.day == day }
        val currentDayHours = sameDayShifts.sumOf { it.shift.durationHours }
        return (currentDayHours + newShift.durationHours) > maxHours
    }
    
    /**
     * Check if shifts overlap on the same day (TIME-BASED, not name-based!)
     */
    private fun hasSameDayOverlap(
        day: String,
        newShift: ParsedShift,
        currentShifts: List<ShiftAssignment>
    ): Boolean {
        val sameDayShifts = currentShifts.filter { it.day == day }
        
        return sameDayShifts.any { existing ->
            shiftsOverlap(existing.shift, newShift)
        }
    }
    
    /**
     * Check if two shifts overlap in time
     */
    private fun shiftsOverlap(shift1: ParsedShift, shift2: ParsedShift): Boolean {
        // Simple overlap check: shift1 starts before shift2 ends AND shift2 starts before shift1 ends
        return !(shift1.endTime.isBefore(shift2.startTime) || 
                 shift1.endTime == shift2.startTime ||
                 shift2.endTime.isBefore(shift1.startTime) ||
                 shift2.endTime == shift1.startTime)
    }
    
    /**
     * Check if there's insufficient rest between shifts
     */
    private fun hasInsufficientRest(
        newDayIndex: Int,
        newShift: ParsedShift,
        currentShifts: List<ShiftAssignment>,
        minRestHours: Double
    ): Boolean {
        // Check previous day
        val previousDayIndex = if (newDayIndex == 0) 6 else newDayIndex - 1
        val previousDayShifts = currentShifts.filter { it.dayIndex == previousDayIndex }
        
        previousDayShifts.forEach { prevShift ->
            val restHours = calculateRestHours(prevShift.shift.endTime, newShift.startTime)
            if (restHours < minRestHours) {
                return true
            }
        }
        
        // Check next day (if assigning night shift)
        if (newShift.isNightShift) {
            val nextDayIndex = if (newDayIndex == 6) 0 else newDayIndex + 1
            val nextDayShifts = currentShifts.filter { it.dayIndex == nextDayIndex }
            
            nextDayShifts.forEach { nextShift ->
                val restHours = calculateRestHours(newShift.endTime, nextShift.shift.startTime)
                if (restHours < minRestHours) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Calculate rest hours between two shifts
     */
    private fun calculateRestHours(endTime: LocalTime, startTime: LocalTime): Double {
        return if (startTime.isAfter(endTime)) {
            Duration.between(endTime, startTime).toMinutes() / 60.0
        } else {
            // Next day
            (Duration.between(endTime, LocalTime.MAX).toMinutes() + 
             Duration.between(LocalTime.MIN, startTime).toMinutes() + 1) / 60.0
        }
    }
    
    /**
     * Calculate employee score (lower is better)
     */
    private fun calculateEmployeeScore(
        employee: Employee,
        day: String,
        dayIndex: Int,
        shift: ParsedShift,
        currentShifts: List<ShiftAssignment>
    ): Int {
        var score = currentShifts.size * 10 // Prefer employees with fewer shifts
        
        // SOFT RULE: Prefer not to assign short rest periods (but allow for Mitgaber)
        if (!employee.isMitgaber) {
            val previousDayIndex = if (dayIndex == 0) 6 else dayIndex - 1
            val previousDayShifts = currentShifts.filter { it.dayIndex == previousDayIndex }
            
            previousDayShifts.forEach { prevShift ->
                val restHours = calculateRestHours(prevShift.shift.endTime, shift.startTime)
                if (restHours < 13.0) {
                    score += 5 // Penalty for short rest
                }
            }
        }
        
        // SOFT RULE: Prefer to give night shifts to employees who already have them (pattern)
        if (shift.isNightShift) {
            val hasNightShifts = currentShifts.any { it.shift.isNightShift }
            if (!hasNightShifts) {
                score += 3 // Small penalty for first night shift
            }
        }
        
        return score
    }
    
    /**
     * Generate error message for impossible schedules
     */
    fun generateErrorMessage(impossibleShifts: List<String>): String {
        return if (impossibleShifts.isNotEmpty()) {
            """
            ⚠️ לא ניתן ליצור סידור שלם!
            
            יש ${impossibleShifts.size} משמרות שלא ניתן למלא בגלל:
            • יותר מדי חסימות
            • חוסר איזון בין "יכול רק" לחסימות
            • עובדים לא זמינים
            • אילוצי זמני מנוחה
            
            הסידור נוצר עם חורים - תצטרך למלא ידנית! ✏️
            """.trimIndent()
        } else {
            "✅ הסידור נוצר בהצלחה!"
        }
    }
}

