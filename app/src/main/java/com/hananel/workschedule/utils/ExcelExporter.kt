package com.hananel.workschedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.hananel.workschedule.data.ShiftDefinitions
// Apache POI imports temporarily disabled
// import org.apache.poi.ss.usermodel.*
// import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for exporting schedules to CSV files (Excel temporarily disabled)
 */
object ExcelExporter {
    
    /**
     * Export schedule to CSV file and share
     * Note: Excel export temporarily disabled, using CSV format instead
     */
    fun exportScheduleToExcel(
        context: Context,
        schedule: Map<String, List<String>>,
        savingMode: Map<String, Boolean>,
        weekStart: String = getCurrentDateString()
    ) {
        try {
            // Create CSV content
            val csvContent = buildString {
                // Header row
                append("משמרת,")
                ShiftDefinitions.daysOfWeek.forEach { day ->
                    append("$day,")
                }
                appendLine()
                
                // Get all unique shifts
                val allShifts = ShiftDefinitions.daysOfWeek.flatMap { day ->
                    ShiftDefinitions.getShiftsForDay(day, savingMode[day] ?: false)
                }.distinctBy { it.id }
                
                // Shift rows
                allShifts.forEach { shift ->
                    append("${shift.name} (${shift.startTime}-${shift.endTime}),")
                    
                    ShiftDefinitions.daysOfWeek.forEach { day ->
                        val dayShifts = ShiftDefinitions.getShiftsForDay(day, savingMode[day] ?: false)
                        if (dayShifts.any { it.id == shift.id }) {
                            val key = "$day-${shift.id}"
                            val employees = schedule[key] ?: emptyList()
                            val employeeText = if (employees.isEmpty()) "" else employees.joinToString(" + ")
                            append("\"$employeeText\",")
                        } else {
                            append(",")
                        }
                    }
                    appendLine()
                }
            }
            
            // Save to file
            val fileName = "סידור_עבודה_${weekStart.replace("/", "-")}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(csvContent, Charsets.UTF_8)
            
            // Share file
            shareCSVFile(context, file)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }
    
    private fun shareCSVFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "שתף קובץ CSV"))
    }
    
    private fun getCurrentDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }
}


