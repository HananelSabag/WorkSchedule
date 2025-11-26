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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp)
                    )
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
            
            // Instructions with color legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("🔴 חסום", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("🔵 יכול", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("⚫ משובץ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("👆 לחץ ארוך", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Complete Manual Schedule Button - הוסר הכפתור הצהוב, רק הכפתור הראשי
                Button(
                    onClick = onGenerateManualSchedule,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                    text = "שמירה והצגה סופית של הסידור",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
        colors = CardDefaults.cardColors(containerColor = GrayLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Selected employee indicator - Enhanced (like BlockingScreen)
            if (selectedEmployee != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.2f)),
                    border = CardDefaults.outlinedCardBorder(enabled = true)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            modifier = Modifier.size(20.dp),
                            tint = PrimaryTeal
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "👤 עובד נבחר:",
                                fontSize = 12.sp,
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
                            text = "לחץ על תא בטבלה",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
            
            // Employee selection buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "עובדים:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface // Use theme color - works in dark mode
                    )
                    Text(
                        text = "לחץ על אחד השמות כדי להכניס לטבלה",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Subtle but visible in dark mode
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp) // Match BlockingScreen spacing
                ) {
                    items(employees) { employee ->
                        val isSelected = selectedEmployee?.id == employee.id
                        Button(
                            onClick = { 
                                if (isSelected) onSelectEmployee(null) else onSelectEmployee(employee)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryTeal else Color.Gray
                            ),
                            modifier = Modifier.height(32.dp), // Match BlockingScreen height
                            shape = RoundedCornerShape(16.dp), // Match BlockingScreen shape
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp) // Match BlockingScreen padding
                        ) {
                            Text(
                                text = employee.name,
                                fontSize = 11.sp, // Match BlockingScreen font size
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}