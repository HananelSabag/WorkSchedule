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
 * User-facing export configuration. All color values are ARGB Android Color ints.
 */
data class PrintSettings(
    val headerColor: Int          = Color.parseColor("#2D3561"), // deep indigo (default)
    val cellBgColor: Int          = Color.parseColor("#EEF0F8"), // pale indigo (default)
    val cellTextColor: Int        = Color.parseColor("#1A1E3A"), // dark navy (default)
    val fontSizeScale: Float      = 1.0f,  // 0.85 = small, 1.0 = normal, 1.2 = large
    val showNotes: Boolean        = true,
    val tableTitle: String        = "סידור עבודה"
) {
    companion object {
        val DEFAULT = PrintSettings()
    }
}

/**
 * Utility for generating schedule images for WhatsApp sharing.
 * Supports dynamic templates and user-configured PrintSettings.
 */
object ImageSharer {

    /**
     * Generate a high-resolution schedule image.
     * @param templateData Dynamic template (null = use hardcoded ShiftDefinitions)
     * @param settings      User export preferences (colors, font scale, title)
     */
    fun generateScheduleImage(
        context: Context,
        schedule: Map<String, List<String>>,
        savingMode: Map<String, Boolean>,
        weekStart: String = getCurrentDateString(),
        templateData: TemplateData? = null,
        settings: PrintSettings = PrintSettings.DEFAULT
    ): Bitmap {

        // Base dimensions — scaled 3× for high quality output
        val scale = 3
        val cellWidth        = 140 * scale
        val cellHeight       = 70  * scale
        val headerHeight     = 70  * scale
        val shiftColumnWidth = 180 * scale

        val daysOfWeek = templateData?.dayColumns?.map { it.dayNameHebrew } ?: ShiftDefinitions.daysOfWeek
        val shiftRows  = templateData?.shiftRows ?: emptyList()
        val numShifts  = if (templateData != null) shiftRows.size else 4

        val totalWidth  = shiftColumnWidth + daysOfWeek.size * cellWidth
        val totalHeight = headerHeight     + numShifts * cellHeight

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Derive corner accent from header color (slightly lighter shade)
        val cornerColor = blendColors(settings.headerColor, Color.WHITE, 0.12f)

        val fs = settings.fontSizeScale

        val headerBackgroundPaint = Paint().apply { isAntiAlias = true; color = settings.headerColor }
        val shiftColumnBackgroundPaint = Paint().apply { isAntiAlias = true; color = settings.headerColor }
        val cellBackgroundPaint = Paint().apply { isAntiAlias = true; color = settings.cellBgColor }
        val blackCornerPaint    = Paint().apply { isAntiAlias = true; color = cornerColor }

        val headerTextPaint = Paint().apply {
            isAntiAlias = true; color = Color.WHITE
            textAlign = Paint.Align.CENTER; textSize = 60f * fs; isFakeBoldText = true
        }
        val shiftTextPaint = Paint().apply {
            isAntiAlias = true; color = Color.WHITE
            textAlign = Paint.Align.CENTER; textSize = 55f * fs; isFakeBoldText = true
        }
        val bodyTextPaint = Paint().apply {
            isAntiAlias = true; color = settings.cellTextColor
            textAlign = Paint.Align.CENTER; textSize = 50f * fs
        }
        val whiteTextPaint = Paint().apply {
            isAntiAlias = true; color = Color.WHITE
            textAlign = Paint.Align.CENTER; textSize = 55f * fs; isFakeBoldText = true
        }
        val timeTextPaint = Paint().apply {
            isAntiAlias = true; color = Color.WHITE
            textAlign = Paint.Align.CENTER; textSize = 40f * fs
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#1A1E3A")
            style = Paint.Style.STROKE; strokeWidth = 2f
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
        val titleLines = settings.tableTitle.split(" ")
        val line1 = titleLines.firstOrNull() ?: "סידור"
        val line2 = if (titleLines.size > 1) titleLines.drop(1).joinToString(" ") else "עבודה"
        canvas.drawText(line1, currentX + shiftColumnWidth / 2f, headerHeight / 2f - 25f, whiteTextPaint)
        canvas.drawText(line2, currentX + shiftColumnWidth / 2f, headerHeight / 2f + 25f, whiteTextPaint)
        canvas.drawRect(
            currentX.toFloat(), 
            0f, 
            (currentX + shiftColumnWidth).toFloat(), 
            headerHeight.toFloat(), 
            borderPaint
        )
        
        // Move LEFT for day headers
        currentX -= cellWidth
        
        // Note paint — small amber text drawn below day name
        val notePaint = Paint().apply {
            isAntiAlias = true; color = Color.parseColor("#FF8C00")
            textAlign = Paint.Align.CENTER; textSize = 28f * fs
        }

        // Day headers — RTL order (ראשון → שבת) going right to left
        daysOfWeek.forEachIndexed { index, day ->
            canvas.drawRect(currentX.toFloat(), 0f,
                (currentX + cellWidth).toFloat(), headerHeight.toFloat(), headerBackgroundPaint)

            canvas.drawText(day, currentX + cellWidth / 2f, headerHeight / 2f - 25f, headerTextPaint)

            val date = getDateForWeek(weekStart, index)
            canvas.drawText(date, currentX + cellWidth / 2f, headerHeight / 2f + 35f, timeTextPaint)

            // Schedule-specific day note
            if (settings.showNotes) {
                val dayNote = schedule["__DAY_NOTE__$day"]?.firstOrNull().orEmpty()
                if (dayNote.isNotBlank()) {
                    canvas.drawText(dayNote, currentX + cellWidth / 2f, headerHeight - 10f, notePaint)
                }
            }

            canvas.drawRect(currentX.toFloat(), 0f,
                (currentX + cellWidth).toFloat(), headerHeight.toFloat(), borderPaint)
            currentX -= cellWidth
        }

        // Draw shift rows
        var currentY = headerHeight

        if (templateData != null) {
            templateData.shiftRows.forEach { shiftRow ->
                // Schedule-specific shift note
                val shiftNote = if (settings.showNotes)
                    schedule["__SHIFT_NOTE__${shiftRow.shiftName}"]?.firstOrNull().orEmpty()
                else ""

                drawShiftRow(
                    canvas, shiftRow.shiftName, shiftRow.shiftHours, shiftNote,
                    daysOfWeek, schedule, currentY, totalWidth,
                    cellWidth, cellHeight, shiftColumnWidth,
                    shiftColumnBackgroundPaint, cellBackgroundPaint,
                    shiftTextPaint, timeTextPaint, bodyTextPaint, borderPaint, notePaint
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
    
    private fun drawShiftRow(
        canvas: Canvas,
        shiftName: String,
        shiftHours: String,
        shiftNote: String,
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
        borderPaint: Paint,
        notePaint: Paint
    ) {
        var currentX = totalWidth - shiftColumnWidth

        canvas.drawRect(currentX.toFloat(), currentY.toFloat(),
            (currentX + shiftColumnWidth).toFloat(), (currentY + cellHeight).toFloat(),
            shiftColumnBackgroundPaint)

        // Shift name + hours; shift note below them if present
        val hasNote = shiftNote.isNotBlank()
        val nameY  = if (hasNote) currentY + cellHeight / 2f - 35f else currentY + cellHeight / 2f - 20f
        val hoursY = if (hasNote) currentY + cellHeight / 2f + 10f else currentY + cellHeight / 2f + 30f
        canvas.drawText(shiftName,  currentX + shiftColumnWidth / 2f, nameY,  shiftTextPaint)
        canvas.drawText(shiftHours, currentX + shiftColumnWidth / 2f, hoursY, timeTextPaint)
        if (hasNote) {
            canvas.drawText(shiftNote, currentX + shiftColumnWidth / 2f,
                currentY + cellHeight - 18f, notePaint)
        }
        
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
            val startDate = java.time.LocalDate.parse(weekStart)
            val targetDate = startDate.plusDays(dayOffset.toLong())
            targetDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
        } catch (e: Exception) {
            getCurrentDateString(dayOffset)
        }
    }

    /** Linear blend between two ARGB colors. ratio=0 → a, ratio=1 → b */
    private fun blendColors(a: Int, b: Int, ratio: Float): Int {
        val inv = 1f - ratio
        val r = (Color.red(a)   * inv + Color.red(b)   * ratio).toInt()
        val g = (Color.green(a) * inv + Color.green(b) * ratio).toInt()
        val bl= (Color.blue(a)  * inv + Color.blue(b)  * ratio).toInt()
        return Color.rgb(r, g, bl)
    }
}

