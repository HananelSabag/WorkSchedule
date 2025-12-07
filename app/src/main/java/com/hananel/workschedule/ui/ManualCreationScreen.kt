package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.theme.*
import com.hananel.workschedule.viewmodel.ScheduleViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCreationScreen(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    schedule: Map<String, List<String>>,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>, 
    weekStartDate: java.time.LocalDate, // Keep for display only, no editing
    templateData: TemplateData? = null, // Dynamic template
    onSelectEmployee: (Employee?) -> Unit,
    onToggleEmployeeInShift: (Employee, String, String) -> Unit,
    onAddFreeTextToCell: (String, String) -> Unit, // New callback for free text
    onGenerateManualSchedule: () -> Unit,
    onReturnToBlocking: () -> Unit, // New callback for returning to blocking
    onClearManualSchedule: () -> Unit, // New callback for reset manual assignments
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for blocking warning dialog
    var showBlockingWarning by remember { mutableStateOf(false) }
    var pendingAssignment by remember { mutableStateOf<Triple<Employee, String, String>?>(null) }
    
    // State for free text editing
    var showFreeTextDialog by remember { mutableStateOf(false) }
    var freeTextCellKey by remember { mutableStateOf("") }
    var freeTextValue by remember { mutableStateOf("") }
    
    // State for reset confirmation
    var showResetConfirmation by remember { mutableStateOf(false) }
    
    // הוסר: State for reset previous schedule dialog - זה אמור להיות רק במסך הבית!
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status bar padding
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title with Logo and Back Button
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(onClick = onReturnToBlocking) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "חזור לחסימות",
                        tint = PrimaryTeal
                    )
                }
                Text(
                    text = "יצירת סידור ידני",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Reset button + App Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Small reset button for manual assignments only - GREEN color (visible in both themes)
                    IconButton(
                        onClick = { showResetConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "איפוס שיבוצים ידניים",
                            tint = PrimaryGreen, // Green like table cells - visible in both light/dark modes
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // App Logo with white background for visibility
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                        )
                    }
                }
            }
            
            // השתמש בטבלה הסטנדרטית - אותו עיצוב בדיוק כמו חסימות
            SimpleScheduleTable(
                employees = employees,
                selectedEmployee = selectedEmployee,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                schedule = schedule, // הצגת הסידור הנוכחי
                templateData = templateData, // Dynamic template support
                isEditMode = false, // לא במצב עריכת טקסט
                weekStartDate = weekStartDate,
                onSetWeekStartDate = null, // ללא עריכת תאריך
                onCellClick = { _, day, shift ->
                    // השתמש בעובד הנבחר (לא בעובד מהתא)
                    selectedEmployee?.let { emp ->
                        val blockKey = "${emp.name}-$day-$shift"
                        val isBlocked = blocks[blockKey] == true
                        val isCanOnly = canOnlyBlocks[blockKey] == true
                        
                        // Check if employee has ANY can-only restrictions
                        val hasCanOnlyRestrictions = canOnlyBlocks.any { (key, value) ->
                            value && key.startsWith("${emp.name}-")
                        }
                        
                        // Check if this is a restricted cell
                        val isRestricted = if (hasCanOnlyRestrictions) {
                            // Employee has can-only cells - this cell is restricted if NOT in can-only list
                            !isCanOnly
                        } else {
                            // Employee has no can-only cells - check regular blocks
                            isBlocked
                        }
                        
                        if (isRestricted) {
                            // Show warning
                            pendingAssignment = Triple(emp, day, shift)
                            showBlockingWarning = true
                        } else {
                            // No restriction - continue with assignment
                            onToggleEmployeeInShift(emp, day, shift)
                        }
                    }
                },
                onLongPress = { day, shift ->
                    // לחיצה ארוכה לעריכת טקסט חופשי
                    freeTextCellKey = "$day-$shift"
                    freeTextValue = schedule[freeTextCellKey]?.joinToString(", ") ?: ""
                    showFreeTextDialog = true
                },
                onDayHeaderClick = null, // ללא פונקציונליות חסימת יום שלם
                isBlockingMode = false, // גבולות שחורים (לא אדומים כמו חסימות)
                modifier = Modifier.fillMaxWidth().height(450.dp)
            )
            
            // Employee Selection Panel (moved to BOTTOM so expanding won't cut table)
            EmployeeSelectionPanel(
                employees = employees,
                selectedEmployee = selectedEmployee,
                onSelectEmployee = onSelectEmployee
            )
            
            // Instructions with color legend - Modern design
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = BlockedRed,
                            shape = RoundedCornerShape(3.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("חסום", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlockedRed)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = CanOnlyBlue,
                            shape = RoundedCornerShape(3.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("יכול", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CanOnlyBlue)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = Color.Black,
                            shape = RoundedCornerShape(3.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("משובץ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("👆 לחץ ארוך", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            // Complete Manual Schedule Button - Premium style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryTeal, Color(0xFF00796B))
                        )
                    )
                    .clickable(onClick = onGenerateManualSchedule),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "שמירה והצגה סופית",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "סיום יצירת הסידור",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Blocking Warning Dialog
        if (showBlockingWarning) {
            // Check if it's a can-only restriction or regular block
            val isCanOnlyRestriction = pendingAssignment?.let { (employee, day, shift) ->
                val blockKey = "${employee.name}-$day-$shift"
                val hasCanOnly = canOnlyBlocks.any { (key, value) ->
                    value && key.startsWith("${employee.name}-")
                }
                hasCanOnly && canOnlyBlocks[blockKey] != true
            } ?: false
            
            AlertDialog(
                onDismissRequest = { 
                    showBlockingWarning = false
                    pendingAssignment = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isCanOnlyRestriction) CanOnlyBlue else BlockedRed
                    )
                },
                title = {
                    Text(
                        text = if (isCanOnlyRestriction) "תא מחוץ לאיזורים מותרים" else "תא חסום",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    pendingAssignment?.let { (employee, day, shift) ->
                        val message = if (isCanOnlyRestriction) {
                            "העובד ${employee.name} סומן כ'יכול רק' בתאים ספציפיים.\n\nהתא הזה ($shift ביום $day) לא נמצא ברשימת התאים המותרים.\n\nהאם אתה בטוח שברצונך לשבץ אותו בכל זאת?"
                        } else {
                            "העובד ${employee.name} חסום במשמרת $shift ביום $day.\n\nהאם אתה בטוח שברצונך לשבץ אותו בכל זאת?"
                        }
                        Text(
                            text = message,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingAssignment?.let { (employee, day, shift) ->
                                onToggleEmployeeInShift(employee, day, shift)
                            }
                            showBlockingWarning = false
                            pendingAssignment = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlockedRed)
                    ) {
                        Text("אשר", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showBlockingWarning = false
                            pendingAssignment = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrayMedium)
                    ) {
                        Text("ביטול", color = Color.White)
                    }
                }
            )
        }
        
        // Free Text Dialog
        if (showFreeTextDialog) {
            AlertDialog(
                onDismissRequest = { showFreeTextDialog = false },
                title = { Text("עריכת טקסט חופשי", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("הזן טקסט מותאם אישית עבור התא:", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = freeTextValue,
                            onValueChange = { freeTextValue = it },
                            placeholder = { Text("הזן טקסט...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddFreeTextToCell(freeTextCellKey, freeTextValue)
                            showFreeTextDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("שמור", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showFreeTextDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GrayMedium)
                    ) {
                        Text("ביטול", color = Color.White)
                    }
                }
            )
        }
        
        // Reset Confirmation Dialog
        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Orange
                    )
                },
                title = {
                    Text(
                        text = "איפוס שיבוצים ידניים",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "האם אתה בטוח שברצונך לאפס את כל השיבוצים הידניים?\n\nפעולה זו תמחק רק את השיבוצים שביצעת, לא את החסימות.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearManualSchedule()
                            showResetConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Text("אפס", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetConfirmation = false }
                    ) {
                        Text("ביטול")
                    }
                }
            )
        }
    }
    
    // הוסר: Reset Previous Schedule Dialog - זה צריך להיות רק במסך הבית!
}

// הקוד הישן של ManualCreationTable הוסר - השתמש ב-SimpleScheduleTable במקום

// הקוד הישן של ManualCreationTableContent הוסר - השתמש ב-SimpleScheduleTable במקום

// הקוד הישן הוסר - SimpleScheduleTable מטפל בכל הפונקציונליות

@Composable
private fun EmployeeSelectionPanel(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onSelectEmployee: (Employee?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Selected employee indicator - Modern design (matches BlockingScreen)
            if (selectedEmployee != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryTeal.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Person icon with background
                        Surface(
                            color = PrimaryTeal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null, 
                                modifier = Modifier.size(32.dp).padding(4.dp),
                                tint = PrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "עובד נבחר",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedEmployee.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "👆 לחץ על תא",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Employee selection buttons - Modern chips style (matches BlockingScreen)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👥 בחר עובד:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${employees.size} עובדים",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(employees) { employee ->
                        val isSelected = selectedEmployee?.id == employee.id
                        FilterChip(
                            onClick = { 
                                if (isSelected) onSelectEmployee(null) else onSelectEmployee(employee)
                            },
                            label = {
                                Text(
                                    text = employee.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isSelected,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryTeal,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = PrimaryTeal.copy(alpha = 0.3f),
                                selectedBorderColor = PrimaryTeal,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }
        }
    }
}