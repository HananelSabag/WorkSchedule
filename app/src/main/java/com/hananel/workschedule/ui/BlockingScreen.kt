package com.hananel.workschedule.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.theme.*
import com.hananel.workschedule.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DisplayMode
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingScreen(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blockingMode: ScheduleViewModel.BlockingMode,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    snackbarMessage: String?,
    isEditingScheduleBlocks: Boolean, // New: are we editing blocks of existing schedule from history?
    editedScheduleName: String? = null, // Schedule name/date when editing from history
    templateData: TemplateData? = null, // Dynamic template
    onSelectEmployee: (Employee?) -> Unit,
    onSetBlockingMode: (ScheduleViewModel.BlockingMode) -> Unit,
    onToggleBlock: (Employee, String, String) -> Unit,
    onBlockAllShiftsForDay: (Employee, String) -> Unit, // New callback for blocking all day
    onToggleSavingMode: (String) -> Unit,
    onSetWeekStartDate: (java.time.LocalDate) -> Unit,
    onGenerateManualSchedule: () -> Unit,
    onGenerateAutomaticSchedule: () -> Unit, // NEW: Generate schedule automatically
    onReturnToSavedSchedule: () -> Unit, // New: return to saved schedule with updated blocks
    onOverrideAndCreateNew: () -> Unit, // New: override and create new manual schedule
    onCreateScheduleCopy: () -> Unit, // New: create a copy of current schedule
    onClearAllBlocks: () -> Unit,
    onDismissSnackbar: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show confirmation dialog for reset
    var showResetConfirmation by remember { mutableStateOf(false) }
    
    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onDismissSnackbar() // Clear message after showing
        }
    }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = modifier.fillMaxSize()) {
            // Empty state when no employees exist
            if (employees.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "אין עובדים במערכת",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "לפני יצירת סידור עבודה,\nעליך להוסיף עובדים במסך ניהול עובדים.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "חזור למסך הבית",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Normal blocking screen content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing for better UX
                ) {
                    // Add space from status bar - reduced
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title with Logo and Back Button
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
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEditingScheduleBlocks && editedScheduleName != null) {
                            "עריכת סידור $editedScheduleName"
                        } else {
                            "חסימת משמרות"
                        },
                        fontSize = if (isEditingScheduleBlocks && editedScheduleName != null) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                // Reset button + Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Small reset button
                    IconButton(
                        onClick = { showResetConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "איפוס חסימות",
                            tint = Color(0xFFE53935), // Red color
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // App Logo with white background for visibility
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
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
            
            // Simple and Stable Schedule Table (moved to TOP to avoid cutting)
            SimpleScheduleTable(
                employees = employees,
                selectedEmployee = selectedEmployee,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                templateData = templateData, // Dynamic template support
                weekStartDate = weekStartDate,
                onSetWeekStartDate = onSetWeekStartDate,
                onDayHeaderClick = { day ->
                    selectedEmployee?.let { employee ->
                        onBlockAllShiftsForDay(employee, day)
                    }
                },
                isBlockingMode = true, // Red border for blocking screen
                onCellClick = { employee, day, shift ->
                    onToggleBlock(employee, day, shift)
                },
                modifier = Modifier.fillMaxWidth().height(450.dp) // Same height as ManualCreationScreen
            )
            
            // Employee Selection Panel (moved to BOTTOM so it won't push table down)
            EmployeeSelectionPanel(
                employees = employees,
                selectedEmployee = selectedEmployee,
                onSelectEmployee = onSelectEmployee,
                blockingMode = blockingMode,
                onSetBlockingMode = onSetBlockingMode
            )
            
            // Legend moved to bottom - Modern compact design
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = BlockedRed,
                            shape = RoundedCornerShape(3.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("לא יכול", fontSize = 12.sp, color = BlockedRed, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = CanOnlyBlue,
                            shape = RoundedCornerShape(3.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("יכול רק", fontSize = 12.sp, color = CanOnlyBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Buttons - Different buttons based on whether we're editing existing schedule blocks
            if (isEditingScheduleBlocks) {
                // EDITING MODE: Show "Return to Schedule" and "Override" buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Return to Saved Schedule Button - PRIMARY
                    Button(
                        onClick = onReturnToSavedSchedule,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "חזור לסידור השמור",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "עם החסימות המעודכנות",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Create Copy Button - NEW OPTION
                    OutlinedButton(
                        onClick = onCreateScheduleCopy,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryGreen
                        ),
                        border = BorderStroke(2.dp, PrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "צור עותק של הסידור",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                    
                    // Override and Create New Button - SECONDARY
                    Button(
                        onClick = onOverrideAndCreateNew,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "דרוס והכן סידור חדש",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "צור סידור ידני מאפס",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // NORMAL MODE: Show Manual/Auto buttons SIDE BY SIDE - Premium style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp), // Bottom spacing
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Manual Schedule Button - Premium style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
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
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "סידור ידני",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "אתה בוחר",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    // Automatic Schedule Button - Premium style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryGreen, Color(0xFF2E7D32))
                                )
                            )
                            .clickable(onClick = onGenerateAutomaticSchedule),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "אוטומטי",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "אלגוריתם",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
                } // End of Column (else block)
            } // End of if-else
            
            // Snackbar at the bottom
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Orange,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        } // End of Box
        
        // Reset Confirmation Dialog
        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = BlockedRed
                    )
                },
                title = {
                    Text(
                        text = "איפוס טבלת חסימות",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "האם אתה בטוח שברצונך לאפס את כל טבלת החסימות?\n\nפעולה זו תמחק את כל החסימות והגבלות שהוגדרו.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllBlocks()
                            showResetConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlockedRed)
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
}

@Composable
private fun EmployeeSelectionPanel(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onSelectEmployee: (Employee?) -> Unit,
    blockingMode: ScheduleViewModel.BlockingMode,
    onSetBlockingMode: (ScheduleViewModel.BlockingMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Selected employee indicator - Modern design
            if (selectedEmployee != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryTeal.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
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
            
            // Employee selection buttons - Modern chips style
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
            
            // Mode buttons - Modern with icons, KEEPING RED AND BLUE COLORS!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "לא יכול" button - RED when active
                Button(
                    onClick = { onSetBlockingMode(ScheduleViewModel.BlockingMode.CANNOT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (blockingMode == ScheduleViewModel.BlockingMode.CANNOT) 
                            BlockedRed else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (blockingMode == ScheduleViewModel.BlockingMode.CANNOT)
                            Color.White else BlockedRed
                    ),
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = if (blockingMode != ScheduleViewModel.BlockingMode.CANNOT) 
                        BorderStroke(1.dp, BlockedRed.copy(alpha = 0.5f)) else null
                ) {
                    Text("🚫 לא יכול", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                // "יכול רק" button - BLUE when active
                Button(
                    onClick = { onSetBlockingMode(ScheduleViewModel.BlockingMode.CAN_ONLY) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (blockingMode == ScheduleViewModel.BlockingMode.CAN_ONLY) 
                            CanOnlyBlue else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (blockingMode == ScheduleViewModel.BlockingMode.CAN_ONLY)
                            Color.White else CanOnlyBlue
                    ),
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = if (blockingMode != ScheduleViewModel.BlockingMode.CAN_ONLY) 
                        BorderStroke(1.dp, CanOnlyBlue.copy(alpha = 0.5f)) else null
                ) {
                    Text("✅ יכול רק", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ShiftSavingPanel removed - not needed

@Composable
private fun MobileOptimizedTable(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    isCompact: Boolean,
    onCellClick: (Employee, String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow!
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
                        .width(if (isCompact) 70.dp else 100.dp)
                        .height(if (isCompact) 40.dp else 50.dp)
                        .border(1.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Empty header cell
                }
                
                // Day headers with dates - abbreviated for mobile
                ShiftDefinitions.daysOfWeek.forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier
                            .width(if (isCompact) 45.dp else 70.dp)  // Much smaller for mobile
                            .height(if (isCompact) 40.dp else 50.dp)
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isCompact) day.take(2) else day, // Abbreviate
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompact) 8.sp else 10.sp,
                                textAlign = TextAlign.Center
                            )
                            if (!isCompact) {
                                Text(
                                    text = getCurrentDateForBlockingDay(index),
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            // Define exact shifts as shown in real schedule images
            val realScheduleShifts = if (isCompact) {
                // Abbreviated for portrait mobile
                listOf("בוקר", "ב.ארוך", "צהריים", "לילה")
            } else {
                listOf("בוקר 6:45-15:00", "בוקר ארוך 06:45-18:45", "צהריים 23:00-14:45", "לילה 7:00-22:30")
            }
            
            // Create shift rows exactly like real schedule
            realScheduleShifts.forEach { shiftDisplay ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shift header - compact for mobile
                    Box(
                        modifier = Modifier
                            .width(if (isCompact) 70.dp else 100.dp)
                            .height(if (isCompact) 35.dp else 45.dp)
                            .background(PrimaryGreen)
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shiftDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 7.sp else 9.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = if (isCompact) 8.sp else 10.sp
                        )
                    }
                    
                    // Day cells for this shift
                    ShiftDefinitions.daysOfWeek.forEach { day ->
                        val shiftId = getShiftIdFromDisplay(shiftDisplay)
                        
                        MobileBlockingCell(
                            day = day,
                            shiftId = shiftId,
                            employees = employees,
                            selectedEmployee = selectedEmployee,
                            blocks = blocks,
                            canOnlyBlocks = canOnlyBlocks,
                            isCompact = isCompact,
                            onCellClick = onCellClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileBlockingCell(
    day: String,
    shiftId: String,
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    isCompact: Boolean,
    onCellClick: (Employee, String, String) -> Unit
) {
    val cellKey = "$day-$shiftId"
    var isClicked by remember { mutableStateOf(false) }
    
    // Get employees in this cell
    val employeesInCell = employees.filter { employee ->
        val blockKey = "${employee.name}-$cellKey"
        val isBlocked = blocks[blockKey] == true
        val isCanOnly = canOnlyBlocks[blockKey] == true
        
        // Show blocked employees in red, can-only in blue
        isBlocked || isCanOnly
    }
    
    // Check if selected employee is in this cell
    val selectedInCell = selectedEmployee?.let { emp ->
        val blockKey = "${emp.name}-$cellKey"
        blocks[blockKey] == true || canOnlyBlocks[blockKey] == true
    } ?: false
    
    // Visual feedback effect
    LaunchedEffect(isClicked) {
        if (isClicked) {
            kotlinx.coroutines.delay(150) // Brief animation
            isClicked = false
        }
    }
    
    Box(
        modifier = Modifier
            .width(if (isCompact) 45.dp else 70.dp)
            .height(if (isCompact) 35.dp else 45.dp)
            .background(
                if (isClicked) Yellow.copy(alpha = 0.5f) else Color.White
            )
            .border(
                width = if (selectedInCell) 2.dp else 1.dp,
                color = if (selectedInCell) Yellow else Color.Black
            )
            .clickable {
                selectedEmployee?.let { emp ->
                    isClicked = true // Visual feedback
                    onCellClick(emp, day, shiftId) // This will toggle add/remove
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (employeesInCell.isEmpty()) {
            // Empty cell - show selected employee's initial as hint
            selectedEmployee?.let { emp -> 
                Text(
                    text = "${emp.name.take(1)}?", // Show initial with question mark as hint
                    fontSize = if (isCompact) 7.sp else 8.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Show employee names in very compact format for mobile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(1.dp)
            ) {
                employeesInCell.take(if (isCompact) 2 else 3).forEach { employee -> // Limit names for space
                    val blockKey = "${employee.name}-$cellKey"
                    val isBlocked = blocks[blockKey] == true
                    val isCanOnly = canOnlyBlocks[blockKey] == true
                    
                    Text(
                        text = if (isCompact) employee.name.take(2) else employee.name, // Abbreviate for mobile
                        fontSize = if (isCompact) 6.sp else 7.sp,
                        color = when {
                            isBlocked -> BlockedRed
                            isCanOnly -> CanOnlyBlue
                            else -> Color.Black
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        fontWeight = if (employee == selectedEmployee) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                // Show "+N" if more employees don't fit
                if (employeesInCell.size > (if (isCompact) 2 else 3)) {
                    Text(
                        text = "+${employeesInCell.size - (if (isCompact) 2 else 3)}",
                        fontSize = if (isCompact) 5.sp else 6.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getShiftIdFromDisplay(displayName: String): String {
    return when {
        displayName.contains("ב.ארוך") || displayName.contains("בוקר ארוך") -> "בוקר-ארוך"
        displayName.contains("בוקר") -> "בוקר"
        displayName.contains("שישי עד") -> "בוקר-קצר"
        displayName.contains("צהריים") -> "צהריים"
        displayName.contains("לילה") -> "לילה"
        else -> "בוקר" // default fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)

// Removed old ScheduleTable - replaced with MobileOptimizedTable

// Helper function for date formatting
private fun getCurrentDateForBlockingDay(dayIndex: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, dayIndex)
    val formatter = java.text.SimpleDateFormat("dd/MM", java.util.Locale("he", "IL"))
    return formatter.format(calendar.time)
}
