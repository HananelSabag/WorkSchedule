package com.hananel.workschedule.utils

import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.widget.Toast
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.data.TemplateData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for generating schedule images for WhatsApp sharing
 * Now supports dynamic templates!
 */
object ImageSharer {
    
    /**
     * Generate schedule image that matches EXACTLY the app's table
     * @param templateData Dynamic template (null = use hardcoded ShiftDefinitions)
     */
    fun generateScheduleImage(
        context: Context,
        schedule: Map<String, List<String>>,
        savingMode: Map<String, Boolean>,
        weekStart: String = getCurrentDateString(),
        templateData: TemplateData? = null
    ): Bitmap {
        
        // Use EXACT dimensions from SimpleScheduleTable
        val cellWidth = 140 * 3 // Scale up for better quality
        val cellHeight = 70 * 3
        val headerHeight = 70 * 3
        val shiftColumnWidth = 180 * 3
        
        // RTL layout - days should be RIGHT to LEFT like in the app (dynamic or hardcoded)
        val daysOfWeek = templateData?.dayColumns?.map { it.dayNameHebrew } ?: ShiftDefinitions.daysOfWeek
        val daysRTL = daysOfWeek.reversed()
        
        // Get shift rows from template (dynamic count!)
        val shiftRows = templateData?.shiftRows ?: emptyList()
        val numShifts = if (templateData != null) shiftRows.size else 4 // Dynamic or 4 for legacy
        
        val totalWidth = shiftColumnWidth + (daysRTL.size * cellWidth)
        val totalHeight = headerHeight + (numShifts * cellHeight)
        
        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fill with white background
        canvas.drawColor(Color.WHITE)
        
        // Paint objects with exact colors from the app
        // Colors according to new specification
        val headerBackgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#3E7C3A") // New dark green for headers
        }
        
        val shiftColumnBackgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#3E7C3A") // Same new dark green for shift column
        }
        
        val cellBackgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#B6D7A8") // New light green for schedule cells
        }
        
        val blackCornerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black corner cell
        }
        
        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black text (#000000)
            textAlign = Paint.Align.CENTER
            textSize = 60f // MUCH larger headers - lots of space available
            isFakeBoldText = true
        }
        
        val shiftTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black text (#000000)
            textAlign = Paint.Align.CENTER
            textSize = 55f // MUCH larger shift names
            isFakeBoldText = true
        }
        
        val bodyTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black text (#000000)
            textAlign = Paint.Align.CENTER
            textSize = 50f // MUCH larger employee names
        }
        
        val whiteTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE // White text for black corner
            textAlign = Paint.Align.CENTER
            textSize = 55f // MUCH larger "סידור עבודה"
            isFakeBoldText = true
        }
        
        val timeTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK // Black text for times
            textAlign = Paint.Align.CENTER
            textSize = 40f // Separate smaller paint for times
        }
        
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f // Clean borders
        }
        
        // Draw header row - TRUE RTL: Start from RIGHT side and go LEFT
        var currentX = totalWidth - shiftColumnWidth // Start from the rightmost position
        
        // SHIFT COLUMN HEADER (rightmost in RTL) - "סידור עבודה"
        canvas.drawRect(
            currentX.toFloat(), 
            0f, 
            (currentX + shiftColumnWidth).toFloat(), 
            headerHeight.toFloat(), 
            blackCornerPaint
        )
        canvas.drawText(
            "סידור",
            currentX + shiftColumnWidth / 2f,
            headerHeight / 2f - 25f,
            whiteTextPaint
        )
        canvas.drawText(
            "עבודה",
            currentX + shiftColumnWidth / 2f,
            headerHeight / 2f + 25f,
            whiteTextPaint
        )
        canvas.drawRect(
            currentX.toFloat(), 
            0f, 
            (currentX + shiftColumnWidth).toFloat(), 
            headerHeight.toFloat(), 
            borderPaint
        )
        
        // Move LEFT for day headers
        currentX -= cellWidth
        
        // Day headers - RTL order (ראשון → שבת) going from right to left
        daysOfWeek.forEachIndexed { index, day ->
            canvas.drawRect(
                currentX.toFloat(), 
                0f, 
                (currentX + cellWidth).toFloat(), 
                headerHeight.toFloat(), 
                headerBackgroundPaint
            )
            
            // Draw day name
            canvas.drawText(
                day,
                currentX + cellWidth / 2f,
                headerHeight / 2f - 25f,
                headerTextPaint
            )
            
            // Draw date - use direct index for proper weekStart calculation
            val date = getDateForWeek(weekStart, index)
            canvas.drawText(
                date,
                currentX + cellWidth / 2f,
                headerHeight / 2f + 35f,
                timeTextPaint // Use smaller paint for dates
            )
            
            canvas.drawRect(
                currentX.toFloat(), 
                0f, 
                (currentX + cellWidth).toFloat(), 
                headerHeight.toFloat(), 
                borderPaint
            )
            
            currentX -= cellWidth // Move LEFT for next day
        }
        
        // Draw shift rows - FULLY DYNAMIC from template
        var currentY = headerHeight
        
        if (templateData != null) {
            // Dynamic: use template shift rows
            templateData.shiftRows.forEach { shiftRow ->
                drawShiftRow(
                    canvas, shiftRow.shiftName, shiftRow.shiftHours,
                    daysOfWeek, schedule, currentY, totalWidth,
                    cellWidth, cellHeight, shiftColumnWidth,
                    shiftColumnBackgroundPaint, cellBackgroundPaint,
                    shiftTextPaint, timeTextPaint, bodyTextPaint, borderPaint
                )
                currentY += cellHeight
            }
        } else {
            // Legacy hardcoded shifts for backward compatibility
            val legacyShifts = listOf(
                Pair("בוקר", "06:45-15:00"),
                Pair("בוקר ארוך\n(שישי עד 13:00)", "06:45-18:45"), 
                Pair("צהריים", "14:45-23:00"),
                Pair("לילה", "22:30-07:00")
            )
            
            legacyShifts.forEach { (shiftName, timeRange) ->
                drawLegacyShiftRow(
                    canvas, shiftName, timeRange,
                    daysOfWeek, schedule, currentY, totalWidth,
                    cellWidth, cellHeight, shiftColumnWidth,
                    shiftColumnBackgroundPaint, cellBackgroundPaint,
                    shiftTextPaint, timeTextPaint, bodyTextPaint, borderPaint
                )
                currentY += cellHeight
            }
        }
        
        return bitmap
    }
    
    /**
     * Draw a dynamic shift row (uses shift name directly from template)
     */
    private fun drawShiftRow(
        canvas: Canvas,
        shiftName: String,
        shiftHours: String,
        daysOfWeek: List<String>,
        schedule: Map<String, List<String>>,
        currentY: Int,
        totalWidth: Int,
        cellWidth: Int,
        cellHeight: Int,
        shiftColumnWidth: Int,
        shiftColumnBackgroundPaint: Paint,
        cellBackgroundPaint: Paint,
        shiftTextPaint: Paint,
        timeTextPaint: Paint,
        bodyTextPaint: Paint,
        borderPaint: Paint
    ) {
        var currentX = totalWidth - shiftColumnWidth
        
        // Shift column (rightmost in RTL)
        canvas.drawRect(
            currentX.toFloat(), 
            currentY.toFloat(), 
            (currentX + shiftColumnWidth).toFloat(), 
            (currentY + cellHeight).toFloat(), 
            shiftColumnBackgroundPaint
        )
        
        // Draw shift name and hours
        canvas.drawText(
            shiftName,
            currentX + shiftColumnWidth / 2f,
            currentY + cellHeight / 2f - 20f,
            shiftTextPaint
        )
        canvas.drawText(
            shiftHours,
            currentX + shiftColumnWidth / 2f,
            currentY + cellHeight / 2f + 30f,
            timeTextPaint
        )
        
        canvas.drawRect(
            currentX.toFloat(), 
            currentY.toFloat(), 
            (currentX + shiftColumnWidth).toFloat(), 
            (currentY + cellHeight).toFloat(), 
            borderPaint
        )
        
        // Move LEFT for day cells
        currentX -= cellWidth
        
        // Day cells - RTL order
        daysOfWeek.forEach { day ->
            canvas.drawRect(
                currentX.toFloat(), 
                currentY.toFloat(), 
                (currentX + cellWidth).toFloat(), 
                (currentY + cellHeight).toFloat(), 
                cellBackgroundPaint
            )
            
            // Key format: "יום-שם משמרת" - EXACTLY as saved in schedule
            val key = "$day-$shiftName"
            val employees = schedule[key] ?: emptyList()
            
            if (employees.isNotEmpty()) {
                val uniqueEmployees = employees.distinct()
                uniqueEmployees.forEachIndexed { index, employee ->
                    canvas.drawText(
                        employee,
                        currentX + cellWidth / 2f,
                        currentY + cellHeight / 2f + (index - uniqueEmployees.size / 2f + 0.5f) * 40f,
                        bodyTextPaint
                    )
                }
            } else {
                canvas.drawText(
                    "-----",
                    currentX + cellWidth / 2f,
                    currentY + cellHeight / 2f,
                    bodyTextPaint
                )
            }
            
            canvas.drawRect(
                currentX.toFloat(), 
                currentY.toFloat(), 
                (currentX + cellWidth).toFloat(), 
                (currentY + cellHeight).toFloat(), 
                borderPaint
            )
            
            currentX -= cellWidth
        }
    }
    
    /**
     * Draw legacy hardcoded shift row (for backward compatibility)
     */
    private fun drawLegacyShiftRow(
        canvas: Canvas,
        shiftName: String,
        timeRange: String,
        daysOfWeek: List<String>,
        schedule: Map<String, List<String>>,
        currentY: Int,
        totalWidth: Int,
        cellWidth: Int,
        cellHeight: Int,
        shiftColumnWidth: Int,
        shiftColumnBackgroundPaint: Paint,
        cellBackgroundPaint: Paint,
        shiftTextPaint: Paint,
        timeTextPaint: Paint,
        bodyTextPaint: Paint,
        borderPaint: Paint
    ) {
        var currentX = totalWidth - shiftColumnWidth
        
        // Shift column (rightmost in RTL)
        canvas.drawRect(
            currentX.toFloat(), 
            currentY.toFloat(), 
            (currentX + shiftColumnWidth).toFloat(), 
            (currentY + cellHeight).toFloat(), 
            shiftColumnBackgroundPaint
        )
        
        // Draw shift name and time with proper spacing
        if (shiftName.contains("\n")) {
            val lines = shiftName.split("\n")
            canvas.drawText(
                lines[0],
                currentX + shiftColumnWidth / 2f,
                currentY + cellHeight / 2f - 40f,
                shiftTextPaint
            )
            canvas.drawText(
                lines[1],
                currentX + shiftColumnWidth / 2f,
                currentY + cellHeight / 2f + 5f,
                timeTextPaint
            )
            canvas.drawText(
                timeRange,
                currentX + shiftColumnWidth / 2f,
                currentY + cellHeight / 2f + 45f,
                timeTextPaint
            )
        } else {
            canvas.drawText(
                shiftName,
                currentX + shiftColumnWidth / 2f,
                currentY + cellHeight / 2f - 20f,
                shiftTextPaint
            )
            canvas.drawText(
                timeRange,
                currentX + shiftColumnWidth / 2f,
                currentY + cellHeight / 2f + 30f,
                timeTextPaint
            )
        }
        
        canvas.drawRect(
            currentX.toFloat(), 
            currentY.toFloat(), 
            (currentX + shiftColumnWidth).toFloat(), 
            (currentY + cellHeight).toFloat(), 
            borderPaint
        )
        
        currentX -= cellWidth
        
        // Day cells - RTL order
        daysOfWeek.forEach { day ->
            canvas.drawRect(
                currentX.toFloat(), 
                currentY.toFloat(), 
                (currentX + cellWidth).toFloat(), 
                (currentY + cellHeight).toFloat(), 
                cellBackgroundPaint
            )
            
            val shiftId = getShiftIdFromName(shiftName)
            
            val actualKey = if (day == "שישי" && shiftId == "בוקר-ארוך") {
                "$day-בוקר-קצר"
            } else {
                "$day-$shiftId"
            }
            
            val employees = if (day == "שבת" && shiftId == "בוקר") {
                (schedule["$day-בוקר"] ?: emptyList()) + 
                (schedule["$day-בוקר-ארוך"] ?: emptyList())
            } else if (day == "שבת" && shiftId == "בוקר-ארוך") {
                emptyList()
            } else {
                schedule[actualKey] ?: emptyList()
            }
            
            if (employees.isNotEmpty()) {
                val uniqueEmployees = employees.distinct()
                uniqueEmployees.forEachIndexed { index, employee ->
                    canvas.drawText(
                        employee,
                        currentX + cellWidth / 2f,
                        currentY + cellHeight / 2f + (index - uniqueEmployees.size / 2f + 0.5f) * 40f,
                        bodyTextPaint
                    )
                }
            } else {
                canvas.drawText(
                    "-----",
                    currentX + cellWidth / 2f,
                    currentY + cellHeight / 2f,
                    bodyTextPaint
                )
            }
            
            canvas.drawRect(
                currentX.toFloat(), 
                currentY.toFloat(), 
                (currentX + cellWidth).toFloat(), 
                (currentY + cellHeight).toFloat(), 
                borderPaint
            )
            
            currentX -= cellWidth
        }
    }
    
    /**
     * Share generated schedule image via WhatsApp
     */
    fun shareScheduleImage(context: Context, bitmap: Bitmap) {
        try {
            // Save bitmap to cache directory with timestamp
            val fileName = "schedule_${System.currentTimeMillis()}.png"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            // Try WhatsApp first
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            
            try {
                context.startActivity(whatsappIntent)
            } catch (e: Exception) {
                // WhatsApp not installed, use general sharing
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(generalIntent, "שתף סידור"))
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Save schedule image to device gallery
     */
    fun saveScheduleImage(context: Context, bitmap: Bitmap): String? {
        return try {
            val fileName = "schedule_${getCurrentDateString()}.png"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Save schedule image to device gallery using MediaStore
     */
    fun saveScheduleImageToGallery(context: Context, bitmap: Bitmap) {
        try {
            val fileName = "סידור_עבודה_${getCurrentDateString()}.png"
            
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            
            val contentResolver = context.contentResolver
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(context, "סידור נשמר בגלריה", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(context, "שגיאה בשמירת הקובץ", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "שגיאה בשמירת הקובץ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getShiftIdFromName(shiftName: String): String {
        return when {
            shiftName.contains("בוקר ארוך") -> "בוקר-ארוך"
            shiftName.contains("בוקר") -> "בוקר"
            shiftName.contains("צהריים") -> "צהריים"
            shiftName.contains("לילה") -> "לילה"
            else -> "בוקר" // fallback
        }
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
    
    private fun getCurrentDateString(dayOffset: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        val formatter = SimpleDateFormat("dd/MM", Locale("he", "IL"))
        return formatter.format(calendar.time)
    }
    
    private fun getDateForWeek(weekStart: String, dayOffset: Int): String {
        return try {
            // Parse weekStart as yyyy-MM-dd format
            val startDate = java.time.LocalDate.parse(weekStart)
            val targetDate = startDate.plusDays(dayOffset.toLong())
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
            targetDate.format(formatter)
        } catch (e: Exception) {
            // Fallback to current date calculation
            getCurrentDateString(dayOffset)
        }
    }
}

