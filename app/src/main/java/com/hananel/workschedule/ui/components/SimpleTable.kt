package com.hananel.workschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun SimpleScheduleTable(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    schedule: Map<String, List<String>>? = null, // For preview mode
    templateData: TemplateData? = null, // Dynamic template (null = use hardcoded)
    isEditMode: Boolean = false, // For preview mode  
    onCellClick: ((Employee, String, String) -> Unit)? = null, // For blocking mode
    onCellEdit: ((String, String) -> Unit)? = null, // For preview edit mode
    onLongPress: ((String, String) -> Unit)? = null, // For manual creation free text
    weekStartDate: java.time.LocalDate? = null, // For dynamic dates
    onSetWeekStartDate: ((java.time.LocalDate) -> Unit)? = null, // For date picker
    onDayHeaderClick: ((String) -> Unit)? = null, // For blocking all shifts in a day
    isBlockingMode: Boolean = false, // For red border in blocking screen
    modifier: Modifier = Modifier
) {
    // Get days list (dynamic or hardcoded)
    val daysOfWeek = remember(templateData) {
        templateData?.dayColumns?.map { it.dayNameHebrew } ?: ShiftDefinitions.daysOfWeek
    }
    
    var scale by remember { mutableFloatStateOf(1f) }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Simple Control Panel - Only Zoom
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GrayLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                // תיקון בעיית הזום - צמצום ב-10% מדויק
                                val currentPercent = (scale * 100).toInt()
                                val newPercent = maxOf(50, currentPercent - 10)
                                scale = newPercent / 100f
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "הקטן",
                                tint = PrimaryTeal
                            )
                        }

                        Text(
                            text = "${(scale * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )

                        IconButton(
                            onClick = { 
                                // תיקון בעיית הזום - הגדלה ב-10% מדויק
                                val currentPercent = (scale * 100).toInt()
                                val newPercent = minOf(200, currentPercent + 10)
                                scale = newPercent / 100f
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "הגדל",
                                tint = PrimaryTeal
                            )
                        }
                    }

                    // Reset View
                    TextButton(
                        onClick = { scale = 1f }
                    ) {
                        Text(
                            text = "איפוס זום",
                            fontSize = 14.sp,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Table - Both Horizontal and Vertical  
            // Scrollable container for both horizontal AND vertical scrolling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                ) {
                    ScheduleTableContent(
                        employees = employees,
                        selectedEmployee = selectedEmployee,
                        blocks = blocks,
                        canOnlyBlocks = canOnlyBlocks,
                        savingMode = savingMode,
                        schedule = schedule,
                        templateData = templateData,
                        daysOfWeek = daysOfWeek,
                        isEditMode = isEditMode,
                        onCellClick = onCellClick,
                        onCellEdit = onCellEdit,
                        onLongPress = onLongPress,
                        weekStartDate = weekStartDate,
                        onSetWeekStartDate = onSetWeekStartDate,
                        onDayHeaderClick = onDayHeaderClick,
                        isBlockingMode = isBlockingMode
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTableContent(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    schedule: Map<String, List<String>>?,
    templateData: TemplateData? = null,
    daysOfWeek: List<String>,
    isEditMode: Boolean,
    onCellClick: ((Employee, String, String) -> Unit)?,
    onCellEdit: ((String, String) -> Unit)?,
    onLongPress: ((String, String) -> Unit)? = null,
    weekStartDate: java.time.LocalDate? = null,
    onSetWeekStartDate: ((java.time.LocalDate) -> Unit)? = null,
    onDayHeaderClick: ((String) -> Unit)? = null,
    isBlockingMode: Boolean = false
) {
    val cellWidth = 140.dp
    val cellHeight = 70.dp
    val shiftColumnWidth = 180.dp

    // Colors according to new specification - exactly matching export
    val headerBackgroundColor = Color(0xFF3E7C3A) // NEW dark green for headers (#3E7C3A)
    val cellBackgroundColor = if (isBlockingMode) {
        Color(0xFFFFD6D6) // Light red for blocking mode (#FFD6D6)
    } else {
        Color(0xFFB6D7A8) // NEW light green for normal cells (#B6D7A8)
    }
    // תיקון: כל הגבולות באדום במסך חסימות
    val borderColor = if (isBlockingMode) Color(0xFFE53935) else Color.Black
    val cellBorderColor = if (isBlockingMode) Color(0xFFE53935) else Color.Black

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow!
        border = if (isBlockingMode) BorderStroke(3.dp, borderColor) else null
    ) {
        Column {
            // Header Row
            Row {
                // Shifts column header with date picker
                if (onSetWeekStartDate != null) {
                    ShiftHeaderWithDatePicker(
                        shiftColumnWidth = shiftColumnWidth,
                        cellHeight = cellHeight,
                        weekStartDate = weekStartDate,
                        onSetWeekStartDate = onSetWeekStartDate,
                        headerBackgroundColor = headerBackgroundColor,
                        cellBorderColor = cellBorderColor
                    )
                } else {
                    // Regular shifts header without date picker
                    Box(
                        modifier = Modifier
                            .width(shiftColumnWidth)
                            .height(cellHeight)
                            .background(headerBackgroundColor)
                            .border(1.dp, cellBorderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "משמרות",
                            color = Color.Black, // Black text (#000000)
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp // Larger font
                        )
                    }
                }
                
                // Day headers - Full names always, all clickable for blocking
                daysOfWeek.forEach { day ->
                    // Regular day header - clickable for blocking all shifts
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .height(cellHeight)
                            .background(headerBackgroundColor)
                            .border(1.dp, cellBorderColor)
                            .clickable {
                                if (selectedEmployee != null && onDayHeaderClick != null) {
                                    onDayHeaderClick(day)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day, // Full day name
                                color = Color.Black, // Black text (#000000)
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp, // Larger headers
                                textAlign = TextAlign.Center
                            )
                            
                            // Date
                            Text(
                                text = getCurrentDateForDay(daysOfWeek.indexOf(day), weekStartDate),
                                color = Color.Black, // Black text (#000000)
                                fontSize = 12.sp, // Larger date
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Get shifts for table
            val shiftsToShow = if (templateData != null) {
                // Dynamic template: convert ShiftRows to ShiftDisplayInfo
                templateData.shiftRows.map { shiftRow ->
                    ShiftDisplayInfo(
                        shiftId = shiftRow.shiftName,
                        displayName = shiftRow.shiftName,
                        timeRange = shiftRow.shiftHours,
                        availableDays = daysOfWeek // All days
                    )
                }
            } else {
                // Legacy: use hardcoded shifts
                getShiftsForTable(savingMode)
            }
            
            // Shifts rows
            shiftsToShow.forEach { shiftInfo ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min) // Make all cells in row have same height
                ) {
                    // Shift name column
                    Box(
                        modifier = Modifier
                            .width(shiftColumnWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                            .background(headerBackgroundColor)
                            .border(1.dp, cellBorderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = shiftInfo.displayName,
                                color = Color.Black, // Black text (#000000)
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, // Larger shift names
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = shiftInfo.timeRange,
                                color = Color.Black, // Black text (#000000)
                                fontSize = 13.sp, // Larger time text
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Day cells
                    daysOfWeek.forEach { day ->
                        // Special handling for Saturday - combine morning shifts
                        if (day == "שבת" && (shiftInfo.shiftId == "בוקר" || shiftInfo.shiftId == "בוקר-ארוך")) {
                            // Only render for "בוקר" shift, skip "בוקר-ארוך" for Saturday
                            if (shiftInfo.shiftId == "בוקר") {
                                // Combined Saturday morning cell
                                val cellKey = "$day-בוקר"
                                
                                if (schedule != null) {
                                    // Preview mode - combined cell
                                    SchedulePreviewCell(
                                        cellKey = cellKey,
                                        employees = (schedule["$day-בוקר"] ?: emptyList()) + 
                                                  (schedule["$day-בוקר-ארוך"] ?: emptyList()),
                                        isEditMode = isEditMode,
                                        onEdit = { key, value -> onCellEdit?.invoke("$day-בוקר", value) },
                                        onLongPress = onLongPress,
                                        // העבר פרמטרי חסימות למסך יצירה ידני - Saturday morning
                                        onCellClick = if (!isEditMode) onCellClick else null,
                                        selectedEmployee = if (!isEditMode) selectedEmployee else null,
                                        allEmployees = if (!isEditMode) employees else null,
                                        blocks = if (!isEditMode) blocks else null,
                                        canOnlyBlocks = if (!isEditMode) canOnlyBlocks else null,
                                        day = if (!isEditMode) day else null,
                                        shift = if (!isEditMode) "בוקר" else null, // שבת משתמש בבוקר
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                    )
                                } else {
                                    // Blocking mode - combined cell
                                    SaturdayMorningCell(
                                        employees = employees,
                                        selectedEmployee = selectedEmployee,
                                        day = day,
                                        blocks = blocks,
                                        canOnlyBlocks = canOnlyBlocks,
                                        onCellClick = onCellClick,
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                    )
                                }
                            }
                            // Skip rendering for "בוקר-ארוך" on Saturday
                        } else if (day != "שבת" || (shiftInfo.shiftId != "בוקר" && shiftInfo.shiftId != "בוקר-ארוך")) {
                            // Normal cell logic for all other cases
                            
                            // Special handling for Friday: בוקר-ארוך row shows בוקר-קצר for Friday
                            val actualShiftId = if (day == "שישי" && shiftInfo.shiftId == "בוקר-ארוך") {
                                "בוקר-קצר" // Friday uses בוקר-קצר instead of בוקר-ארוך
                            } else {
                                shiftInfo.shiftId
                            }
                            
                            val cellKey = "$day-$actualShiftId"
                            
                            if (shiftInfo.availableDays.contains(day)) {
                                if (schedule != null) {
                                    // Preview mode
                                    SchedulePreviewCell(
                                        cellKey = cellKey,
                                        employees = schedule[cellKey] ?: emptyList(),
                                        isEditMode = isEditMode,
                                        onEdit = onCellEdit,
                                        onLongPress = onLongPress,
                                        // העבר פרמטרי חסימות אם זה לא במצב עריכה (יצירה ידני)
                                        onCellClick = if (!isEditMode) onCellClick else null,
                                        selectedEmployee = if (!isEditMode) selectedEmployee else null,
                                        allEmployees = if (!isEditMode) employees else null,
                                        blocks = if (!isEditMode) blocks else null,
                                        canOnlyBlocks = if (!isEditMode) canOnlyBlocks else null,
                                        day = if (!isEditMode) day else null,
                                        shift = if (!isEditMode) actualShiftId else null,
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                    )
                                } else {
                                    // Blocking mode
                                    ScheduleBlockingCell(
                                        employees = employees,
                                        selectedEmployee = selectedEmployee,
                                        day = day,
                                        shift = actualShiftId,
                                        blocks = blocks,
                                        canOnlyBlocks = canOnlyBlocks,
                                        onCellClick = onCellClick,
                                        onLongPress = onLongPress,
                                        cellBorderColor = cellBorderColor,
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                    )
                                }
                            } else {
                                // Empty cell - but still editable in edit mode!
                                if (schedule != null) {
                                    // Preview mode - make all cells editable
                                    SchedulePreviewCell(
                                        cellKey = cellKey,
                                        employees = schedule[cellKey] ?: emptyList(),
                                        isEditMode = isEditMode,
                                        onEdit = onCellEdit,
                                        onLongPress = onLongPress,
                                        // העבר פרמטרי חסימות אם זה לא במצב עריכה
                                        onCellClick = if (!isEditMode) onCellClick else null,
                                        selectedEmployee = if (!isEditMode) selectedEmployee else null,
                                        allEmployees = if (!isEditMode) employees else null,
                                        blocks = if (!isEditMode) blocks else null,
                                        canOnlyBlocks = if (!isEditMode) canOnlyBlocks else null,
                                        day = if (!isEditMode) day else null,
                                        shift = if (!isEditMode) actualShiftId else null,
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                    )
                                } else {
                                    // Blocking mode - empty cell
                                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                            .background(Color.White)
                                            .border(1.dp, cellBorderColor)
                                    )
                                }
                            }
                        } else {
                            // Empty placeholder - but still editable in edit mode!
                            val cellKey = "$day-${shiftInfo.shiftId}"
                            if (schedule != null) {
                                // Preview mode - make all cells editable even placeholders
                                SchedulePreviewCell(
                                    cellKey = cellKey,
                                    employees = schedule[cellKey] ?: emptyList(),
                                    isEditMode = isEditMode,
                                    onEdit = onCellEdit,
                                    onLongPress = onLongPress,
                                    // העבר פרמטרי חסימות אם זה לא במצב עריכה
                                    onCellClick = if (!isEditMode) onCellClick else null,
                                    selectedEmployee = if (!isEditMode) selectedEmployee else null,
                                    allEmployees = if (!isEditMode) employees else null,
                                    blocks = if (!isEditMode) blocks else null,
                                    canOnlyBlocks = if (!isEditMode) canOnlyBlocks else null,
                                    day = if (!isEditMode) day else null,
                                    shift = if (!isEditMode) shiftInfo.shiftId else null,
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                )
                            } else {
                                // Blocking mode - empty placeholder
                                Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = cellHeight)
                                        .background(Color.White)
                                        .border(1.dp, cellBorderColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class for shift info
private data class ShiftDisplayInfo(
    val shiftId: String,
    val displayName: String,
    val timeRange: String,
    val availableDays: List<String>
)

// Get shifts to display in table
private fun getShiftsForTable(savingMode: Map<String, Boolean>): List<ShiftDisplayInfo> {
    return listOf(
        ShiftDisplayInfo(
            shiftId = "בוקר",
            displayName = "בוקר",
            timeRange = "06:45-15:00",
            availableDays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
        ),
        ShiftDisplayInfo(
            shiftId = "בוקר-ארוך",
            displayName = "בוקר ארוך\n(שישי עד 13:00)",
            timeRange = "06:45-19:00",
            availableDays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי")
        ),
        ShiftDisplayInfo(
            shiftId = "צהריים",
            displayName = "צהריים",
            timeRange = "14:45-23:00",
            availableDays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
        ),
        ShiftDisplayInfo(
            shiftId = "לילה",
            displayName = "לילה",
            timeRange = "22:30-07:00",
            availableDays = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SchedulePreviewCell(
    cellKey: String,
    employees: List<String>,
    isEditMode: Boolean,
    onEdit: ((String, String) -> Unit)?,
    onLongPress: ((String, String) -> Unit)? = null,
    // פרמטרים להצגת חסימות במסך יצירה ידני
    onCellClick: ((Employee, String, String) -> Unit)? = null, // הוספת תמיכה בלחיצה
    selectedEmployee: Employee? = null, // הוספת העובד הנבחר
    allEmployees: List<Employee>? = null,
    blocks: Map<String, Boolean>? = null,
    canOnlyBlocks: Map<String, Boolean>? = null,
    day: String? = null,
    shift: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black) // הוחזר לצבע שחור עבור preview cells
            .combinedClickable(
                onClick = {
                    // תמיכה בלחיצה למסך יצירה ידני
                    if (onCellClick != null && selectedEmployee != null && day != null && shift != null) {
                        onCellClick.invoke(selectedEmployee, day, shift)
                    }
                },
                onLongClick = {
                    // השתמש ב-day ו-shift הנכונים במקום לפרק את cellKey
                    if (onLongPress != null && day != null && shift != null) {
                        onLongPress.invoke(day, shift)
                    } else {
                        // fallback - פירוק cellKey
                        onLongPress?.invoke(cellKey.split("-")[0], cellKey.split("-")[1])
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isEditMode && onEdit != null) {
            // Edit mode - TextField
            var text by remember { mutableStateOf(employees.joinToString(", ")) }
            
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    onEdit(cellKey, it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    textDirection = androidx.compose.ui.text.style.TextDirection.ContentOrRtl,
                    color = Color.Black // Force black text in both light and dark mode
                ),
                singleLine = false,
                placeholder = {
                    Text("הקלד כאן...", fontSize = 9.sp, color = Color.Gray.copy(alpha = 0.6f))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Orange
                )
            )
        } else {
            // View mode - יציג גם חסימות (במסך יצירה ידני) וגם שיבוצים
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                // הצגת חסימות אם זה מסך יצירה ידני
                if (allEmployees != null && blocks != null && canOnlyBlocks != null && day != null && shift != null) {
                    // עובדים חסומים באדום
                    val blockedEmployees = allEmployees.filter { employee ->
                        val blockKey = "${employee.name}-$day-$shift"
                        blocks[blockKey] == true
                    }
                    blockedEmployees.forEach { employee ->
                        Text(
                            text = employee.name,
                            fontSize = 10.sp,
                            color = BlockedRed,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // עובדים עם "יכול" בכחול
                    val canOnlyEmployees = allEmployees.filter { employee ->
                        val blockKey = "${employee.name}-$day-$shift"
                        canOnlyBlocks[blockKey] == true
                    }
                    canOnlyEmployees.forEach { employee ->
                        Text(
                            text = employee.name,
                            fontSize = 10.sp,
                            color = CanOnlyBlue,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // הצגת עובדים משובצים בשחור
                if (employees.isNotEmpty()) {
                    employees.forEach { employeeName ->
                        Text(
                            text = employeeName,
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                } else if (allEmployees == null) {
                    // רק בתצוגה מקדימה רגילה (לא יצירה ידני)
                    Text(
                        text = "-----",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SaturdayMorningCell(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    day: String,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    onCellClick: ((Employee, String, String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Get blocked and can-only employees for both morning shifts on Saturday
    val blockedEmployees = employees.filter { 
        blocks["${it.name}-$day-בוקר"] == true || blocks["${it.name}-$day-בוקר-ארוך"] == true
    }
    val canOnlyEmployees = employees.filter { 
        canOnlyBlocks["${it.name}-$day-בוקר"] == true || canOnlyBlocks["${it.name}-$day-בוקר-ארוך"] == true
    }
    
    var isClicked by remember { mutableStateOf(false) }
    
    // Flash animation
    LaunchedEffect(isClicked) {
        if (isClicked) {
            kotlinx.coroutines.delay(200)
            isClicked = false
        }
    }
    
    Box(
        modifier = modifier
            .background(
                if (isClicked) Yellow.copy(alpha = 0.5f) else Color.White
            )
            .border(1.dp, Color.Black)
            .clickable {
                selectedEmployee?.let { employee ->
                    // Click affects both morning shifts for Saturday
                    onCellClick?.invoke(employee, day, "בוקר")
                    isClicked = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Show blocked employees (RED)
            blockedEmployees.forEach { employee ->
                Text(
                    text = employee.name,
                    fontSize = 10.sp,
                    color = BlockedRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // Show can-only employees (BLUE)
            canOnlyEmployees.forEach { employee ->
                Text(
                    text = employee.name,
                    fontSize = 10.sp,
                    color = CanOnlyBlue,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // Show clear hint if employee selected and cell empty
            if (blockedEmployees.isEmpty() && canOnlyEmployees.isEmpty() && selectedEmployee != null) {
                Text(
                    text = "הקלק להוספת שם",
                    fontSize = 8.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScheduleBlockingCell(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    day: String,
    shift: String,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    onCellClick: ((Employee, String, String) -> Unit)?,
    onLongPress: ((String, String) -> Unit)? = null,
    cellBorderColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    modifier: Modifier = Modifier
) {
    // Get blocked and can-only employees for this cell
    val blockedEmployees = employees.filter { 
        blocks["${it.name}-$day-$shift"] == true 
    }
    val canOnlyEmployees = employees.filter { 
        canOnlyBlocks["${it.name}-$day-$shift"] == true 
    }
    
    var isClicked by remember { mutableStateOf(false) }
    
    // Flash animation
    LaunchedEffect(isClicked) {
        if (isClicked) {
            kotlinx.coroutines.delay(200)
            isClicked = false
        }
    }
    
    Box(
        modifier = modifier
            .background(
                if (isClicked) Yellow.copy(alpha = 0.5f) else Color.White
            )
                            .border(1.dp, cellBorderColor)
            .combinedClickable(
                onClick = {
                    selectedEmployee?.let { employee ->
                        onCellClick?.invoke(employee, day, shift)
                        isClicked = true
                    }
                },
                onLongClick = {
                    onLongPress?.invoke(day, shift)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Show blocked employees (RED)
            blockedEmployees.forEach { employee ->
                Text(
                    text = employee.name,
                    fontSize = 10.sp,
                    color = BlockedRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // Show can-only employees (BLUE)
            canOnlyEmployees.forEach { employee ->
                Text(
                    text = employee.name,
                    fontSize = 10.sp,
                    color = CanOnlyBlue,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // Show clear hint if employee selected and cell empty
            if (blockedEmployees.isEmpty() && canOnlyEmployees.isEmpty() && selectedEmployee != null) {
                Text(
                    text = "הקלק להוספת שם",
                    fontSize = 8.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftHeaderWithDatePicker(
    shiftColumnWidth: Dp,
    cellHeight: Dp,
    weekStartDate: java.time.LocalDate?,
    onSetWeekStartDate: (java.time.LocalDate) -> Unit,
    headerBackgroundColor: Color,
    cellBorderColor: Color
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .width(shiftColumnWidth)
            .height(cellHeight)
            .background(headerBackgroundColor)
            .border(1.dp, cellBorderColor)
            .clickable { showDatePicker = true },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "משמרות",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = weekStartDate?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) ?: "בחר תאריך",
                color = Color.Black,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Date Picker Dialog - Only Sundays selectable
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = weekStartDate?.toEpochDay()?.times(24 * 60 * 60 * 1000L),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = java.time.LocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000L))
                    return date.dayOfWeek == java.time.DayOfWeek.SUNDAY
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000L))
                            onSetWeekStartDate(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("אישור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("ביטול")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun getCurrentDateForDay(dayOffset: Int, weekStartDate: java.time.LocalDate? = null): String {
    val baseDate = weekStartDate ?: java.time.LocalDate.now()
    val targetDate = baseDate.plusDays(dayOffset.toLong())
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
    return targetDate.format(formatter)
}
