package com.hananel.workschedule.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.hananel.workschedule.data.ShiftDefinitions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for sharing schedules via WhatsApp
 */
object WhatsAppSharer {
    
    /**
     * Share schedule as formatted text via WhatsApp
     */
    fun shareScheduleText(
        context: Context,
        schedule: Map<String, List<String>>,
        savingMode: Map<String, Boolean>,
        weekStart: String = getCurrentWeekString()
    ) {
        val scheduleText = formatScheduleText(schedule, savingMode, weekStart)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, scheduleText)
            setPackage("com.whatsapp")
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "שתף בווצאפ"))
        } catch (e: Exception) {
            // WhatsApp not installed or other error
            shareAsGeneralText(context, scheduleText)
        }
    }
    
    /**
     * Share schedule as image via WhatsApp
     */
    fun shareScheduleImage(
        context: Context,
        bitmap: Bitmap
    ) {
        try {
            // Save bitmap to cache directory
            val file = File(context.cacheDir, "schedule_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            
            context.startActivity(Intent.createChooser(intent, "שתף בווצאפ"))
        } catch (e: Exception) {
            // Handle error - could show toast or fallback
            e.printStackTrace()
        }
    }
    
    private fun formatScheduleText(
        schedule: Map<String, List<String>>,
        savingMode: Map<String, Boolean>,
        weekStart: String
    ): String {
        val stringBuilder = StringBuilder()
        
        stringBuilder.append("📅 *סידור עבודה - שבוע $weekStart*\n\n")
        
        // Create table-like format matching real schedule
        val realScheduleShifts = listOf(
            "בוקר 6:45-15:00",
            "בוקר ארוך 06:45-18:45", 
            "שישי עד 6:45-13:00",
            "צהריים 23:00-14:45",
            "לילה 7:00-22:30"
        )
        
        // Header with days and dates
        stringBuilder.append("```\n")
        stringBuilder.append("        ")
        ShiftDefinitions.daysOfWeek.forEachIndexed { index, day ->
            val date = getCurrentDateString(index)
            stringBuilder.append("$day($date)  ")
        }
        stringBuilder.append("\n")
        stringBuilder.append("=" .repeat(60) + "\n")
        
        // Shift rows
        realScheduleShifts.forEach { shiftDisplay ->
            stringBuilder.append("${shiftDisplay.padEnd(12)} ")
            
            ShiftDefinitions.daysOfWeek.forEach { day ->
                val shiftId = getShiftIdFromDisplay(shiftDisplay)
                
                // Special handling for Friday in "בוקר ארוך" row - should be בוקר-קצר
                val actualKey = if (day == "שישי" && shiftId == "בוקר-ארוך") {
                    "$day-בוקר-קצר"
                } else {
                    "$day-$shiftId"
                }
                
                val employees = schedule[actualKey] ?: emptyList()
                val employeeText = if (employees.isEmpty()) "-----" else employees.joinToString(",")
                
                stringBuilder.append("${employeeText.padEnd(8)} ")
            }
            stringBuilder.append("\n")
        }
        
        stringBuilder.append("```\n\n")
        stringBuilder.append("_נוצר באמצעות מערכת שיבוץ עובדים_\n")
        stringBuilder.append("_פותח על ידי חננאל סבג - כל הזכויות שמורות_")
        
        return stringBuilder.toString()
    }
    
    private fun getShiftIdFromDisplay(displayName: String): String {
        return when {
            displayName.contains("בוקר ארוך") -> "בוקר-ארוך"
            displayName.contains("בוקר") -> "בוקר"
            displayName.contains("שישי עד") -> "בוקר-קצר"
            displayName.contains("צהריים") -> "צהריים"
            displayName.contains("לילה") -> "לילה"
            else -> "unknown"
        }
    }
    
    private fun getCurrentDateString(dayOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        val formatter = SimpleDateFormat("dd/MM", Locale("he", "IL"))
        return formatter.format(calendar.time)
    }
    
    private fun shareAsGeneralText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        context.startActivity(Intent.createChooser(intent, "שתף סידור"))
    }
    
    private fun getCurrentWeekString(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("he", "IL"))
        return formatter.format(Date())
    }
}
