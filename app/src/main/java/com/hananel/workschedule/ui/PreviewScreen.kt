package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.ui.theme.*

@Composable
fun PreviewScreen(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    errorMessage: String,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    onUpdateCell: (String, String) -> Unit,
    onSaveSchedule: () -> Unit, // Deprecated - smart save system handles this automatically
    onShareSchedule: (ShareType) -> Unit,
    onBackClick: () -> Unit,
    onReturnToBlocking: () -> Unit, // New callback for returning to blocking
    onDismissError: () -> Unit, // New callback for dismissing error popup
    isEditingExistingSchedule: Boolean = false, // Smart save system - indicates if editing existing schedule
    modifier: Modifier = Modifier
) {
    var editableSchedule by remember { 
        mutableStateOf(schedule.mapValues { it.value.joinToString(", ") })
    }
    
    // Update editable schedule when schedule changes
    LaunchedEffect(schedule) {
        editableSchedule = schedule.mapValues { it.value.joinToString(", ") }
    }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add space from status bar
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Title with Logo and Back Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "חזור",
                            tint = PrimaryTeal
                        )
                    }
                    
                    Text(
                        text = "תצוגה מקדימה - סידור עבודה",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal, // Use logo color
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            
            
            // Return to Blocking Button - At the top as requested, red color, less wide
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = onReturnToBlocking,
                        colors = ButtonDefaults.buttonColors(containerColor = BlockedRed),
                        modifier = Modifier.width(160.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "חזור לחסימות",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Info Box - Compact explanation about editing
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditingExistingSchedule) {
                                "שינויים נשמרים אוטומטית • לחיצה ארוכה על תא = עריכת טקסט חופשי"
                            } else {
                            "הסידור שמור בהיסטוריה • לחיצה ארוכה על תא = עריכת טקסט חופשי"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                        )
                    }
                }
            }
            
            // Simple and Stable Schedule Table
            item {
                SimpleScheduleTable(
                    employees = employees,
                    selectedEmployee = null,
                    blocks = emptyMap(),
                    canOnlyBlocks = emptyMap(),
                    savingMode = savingMode,
                    schedule = schedule,
                    isEditMode = true, // Always enable long-press editing
                    weekStartDate = weekStartDate,
                    isBlockingMode = false, // Professional colors like export - no red border
                    onCellEdit = { key, value ->
                        editableSchedule = editableSchedule.toMutableMap().apply { 
                            put(key, value) 
                        }
                        onUpdateCell(key, value)
                    },
                    modifier = Modifier.height(500.dp) // Fixed height for the table
                )
            }
            
            // Share Buttons - Two separate buttons as requested
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download to Gallery Button
                    Button(
                        onClick = { onShareSchedule(ShareType.DOWNLOAD_IMAGE) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "הורד לגלריה",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Share on WhatsApp Button  
                    Button(
                        onClick = { onShareSchedule(ShareType.WHATSAPP_IMAGE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp green
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "שתף בווצאפ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Statistics - moved to bottom as requested
            item {
                EmployeeStatistics(
                    employees = employees, 
                    schedule = schedule,
                    savingMode = savingMode
                )
            }
        }
        
        // Transparent Error Popup - appears over content
        if (errorMessage.isNotEmpty()) {
            ErrorPopup(
                message = errorMessage,
                onDismiss = onDismissError
            )
        }
    }
}

@Composable
private fun EmployeeStatistics(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    savingMode: Map<String, Boolean> = emptyMap()
) {
    // Helper function to extract employee name from text (first word before space)
    fun extractEmployeeName(text: String): String {
        return text.trim().split(" ").firstOrNull() ?: text
    }
    
    // SMART: Calculate hours with free text support
    fun calculateHours(cellKey: String, employeeText: String): Double {
        val parts = cellKey.split("-")
        if (parts.size < 2) return 8.0
        
        val day = parts[0]
        val shiftId = parts.drop(1).joinToString("-")
        val isSaving = savingMode[day] ?: false
        val shifts = ShiftDefinitions.getShiftsForDay(day, isSaving)
        val shift = shifts.find { it.id == shiftId }
        
        // Step 1: Check if there's free text (space + character after employee name)
        val hasFreeText = employeeText.trim().contains(" ") && 
                          employeeText.trim().split(" ").size > 1
        
        if (!hasFreeText) {
            // Step 2: No free text - use default shift hours
            return shift?.hours ?: 8.0
        }
        
        // Step 3: Has free text - analyze and calculate
        try {
            // Extract all numbers (single or double digits)
            val numbers = Regex("""\d+""").findAll(employeeText).map { it.value.toInt() }.toList()
            
            if (numbers.isEmpty()) {
                // No numbers found - use default
                return shift?.hours ?: 8.0
            }
            
            val startHour: Int
            val endHour: Int
            
            if (numbers.size == 1) {
                // Case A: Only end time changed
                startHour = shift?.startHour ?: 7
                endHour = numbers[0]
            } else {
                // Case B: Both start and end changed
                // First number in string = start, second = end
                startHour = numbers[0]
                endHour = numbers[1]
            }
            
            // Calculate hours difference
            var hours = if (endHour > startHour) {
                (endHour - startHour).toDouble()
            } else {
                // Overnight shift
                (24 - startHour + endHour).toDouble()
            }
            
            // Add break time (0.25 = 15 minutes)
            hours += 0.25
            
            // Step 4: Protections
            if (hours > 12.25 || hours <= 0) {
                // Invalid - return default 8 hours
                return 8.0
            }
            
            return hours
            
        } catch (e: Exception) {
            // Any error - return default
            return 8.0
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 סטטיסטיקה",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal
                )
                
                // Refresh button (statistics auto-update, but this is for manual refresh)
                Text(
                    text = "🔄",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .alpha(0.6f),
                    color = PrimaryTeal
                )
            }
            
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "שם עובד",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "משמרות",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(70.dp)
                )
                Text(
                    text = "שעות",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(70.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Table Rows - IMPROVED: Smart employee name detection + hours calculation
            employees.forEach { employee ->
                // Count shifts - extract first word from text to match employee name
                var shiftCount = 0
                var totalHours = 0.0
                
                schedule.forEach { (cellKey, employeeList) ->
                    employeeList.forEach { text ->
                        val extractedName = extractEmployeeName(text)
                        if (extractedName.equals(employee.name, ignoreCase = true)) {
                            shiftCount++
                            // SMART: Calculate hours based on the full text (supports free text)
                            totalHours += calculateHours(cellKey, text)
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = employee.name,
                        fontSize = 14.sp,
                        color = Color.Black, // Force black text in both light and dark mode
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Shift count badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (shiftCount > 0) PrimaryGreen.copy(alpha = 0.2f) else GrayLight
                        ),
                        modifier = Modifier.width(70.dp)
                    ) {
                        Text(
                            text = shiftCount.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (shiftCount > 0) PrimaryGreen else GrayMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Weekly hours badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (totalHours > 0) PrimaryTeal.copy(alpha = 0.2f) else GrayLight
                        ),
                        modifier = Modifier.width(70.dp)
                    ) {
                        Text(
                            text = if (totalHours > 0) {
                                String.format("%.1f", totalHours)
                            } else {
                                "0"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalHours > 0) PrimaryTeal else GrayMedium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Divider between rows (except last)
                if (employee != employees.last()) {
                    HorizontalDivider(
                        color = GrayLight,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SchedulePreviewTable(
    schedule: Map<String, String>,
    savingMode: Map<String, Boolean>,
    isEditMode: Boolean,
    onCellEdit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header Row - Days with dates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
            ) {
                // Empty cell for shift names column
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp)
                        .border(1.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Empty header cell
                }
                
                // Day headers with dates
                ShiftDefinitions.daysOfWeek.forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(60.dp)
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = getCurrentDateForDay(index), // Will implement this
                                color = Color.White,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Define exact shifts as shown in real schedule images
            val realScheduleShifts = listOf(
                "בוקר 6:45-15:00",
                "בוקר ארוך 06:45-18:45",
                "שישי עד 6:45-13:00", 
                "צהריים 23:00-14:45",
                "לילה 7:00-22:30"
            )
            
            // Create shift rows exactly like real schedule
            realScheduleShifts.forEach { shiftDisplay ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shift header - exactly like real schedule
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp)
                            .background(PrimaryGreen)
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shiftDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                    
                    // Day cells for this shift
                    ShiftDefinitions.daysOfWeek.forEach { day ->
                        val cellKey = "$day-${getShiftIdFromDisplay(shiftDisplay)}"
                        val cellValue = schedule[cellKey] ?: ""
                        
                        RealScheduleCell(
                            key = cellKey,
                            value = cellValue,
                            isEditMode = isEditMode,
                            onEdit = onCellEdit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealScheduleCell(
    key: String,
    value: String,
    isEditMode: Boolean,
    onEdit: (String, String) -> Unit
) {
    var cellText by remember(key, value) { mutableStateOf(value) }
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(50.dp)
            .background(Color.White)
            .border(
                width = if (isEditMode && isFocused) 2.dp else 1.dp,
                color = if (isEditMode && isFocused) Orange else Color.Black
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isEditMode) {
            // FULL EXCEL-LIKE EDITING - Free text field
            OutlinedTextField(
                value = cellText,
                onValueChange = { newValue ->
                    cellText = newValue
                    onEdit(key, newValue) // Auto-save changes
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                textStyle = TextStyle(
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    textDirection = androidx.compose.ui.text.style.TextDirection.ContentOrRtl // RTL Hebrew support
                ),
                singleLine = false, // Allow multiple lines like Excel
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Orange,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                placeholder = {
                    Text(
                        text = "הקלד כאן...",
                        fontSize = 7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            )
        } else {
            // VIEW MODE - Display only
            if (value.isEmpty()) {
                Text(
                    text = "-----",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = value,
                    fontSize = 9.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp,
                    modifier = Modifier.padding(1.dp),
                    maxLines = 3, // Allow multiple lines in display
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShareMenu(
    onShareType: (ShareType) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "בחר סוג שיתוף:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            ShareMenuItem(
                text = "שתף בווצאפ (תמונה)",
                icon = Icons.Default.Image,
                onClick = { onShareType(ShareType.WHATSAPP_IMAGE) }
            )
            
            ShareMenuItem(
                text = "הורד כתמונה לגלריה",
                icon = Icons.Default.Download,
                onClick = { onShareType(ShareType.DOWNLOAD_IMAGE) }
            )
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ביטול")
            }
        }
    }
}

@Composable
private fun ShareMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

enum class ShareType {
    WHATSAPP_IMAGE, DOWNLOAD_IMAGE
}

@Composable
private fun ErrorPopup(
    message: String,
    onDismiss: () -> Unit
) {
    // Full screen overlay with transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)), // Semi-transparent overlay
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning Icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Orange,
                    modifier = Modifier.size(48.dp)
                )
                
                // Title
                Text(
                    text = "אזהרה מהגנרטור האוטמטי",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange,
                    textAlign = TextAlign.Center
                )
                
                // Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                // OK Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "הבנתי",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper functions for real schedule format
private fun getCurrentDateForDay(dayIndex: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, dayIndex)
    val formatter = java.text.SimpleDateFormat("dd/MM", java.util.Locale("he", "IL"))
    return formatter.format(calendar.time)
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
