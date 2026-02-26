package com.hananel.workschedule.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.ui.theme.*
import com.hananel.workschedule.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════
// LANDSCAPE BLOCKING SCREEN
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun LandscapeBlockingScreen(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blockingMode: ScheduleViewModel.BlockingMode,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    templateData: TemplateData? = null,
    onSelectEmployee: (Employee?) -> Unit,
    onSetBlockingMode: (ScheduleViewModel.BlockingMode) -> Unit,
    onToggleBlock: (Employee, String, String) -> Unit,
    onBlockAllShiftsForDay: (Employee, String) -> Unit,
    onSetWeekStartDate: (java.time.LocalDate) -> Unit,
    onClose: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var showToolbar by remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            LandscapeTopBar(title = "חסימות — תצוגה מורחבת", onClose = onClose)

            Box(modifier = Modifier.weight(1f)) {
                SimpleScheduleTable(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    blocks = blocks,
                    canOnlyBlocks = canOnlyBlocks,
                    savingMode = savingMode,
                    templateData = templateData,
                    weekStartDate = weekStartDate,
                    onSetWeekStartDate = onSetWeekStartDate,
                    onDayHeaderClick = { day ->
                        selectedEmployee?.let { emp -> onBlockAllShiftsForDay(emp, day) }
                    },
                    isBlockingMode = true,
                    onCellClick = { employee, day, shift -> onToggleBlock(employee, day, shift) },
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = !showToolbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    ShowToolbarPill(onClick = { showToolbar = true })
                }
            }

            AnimatedVisibility(
                visible = showToolbar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                LandscapeBlockingToolbar(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    blockingMode = blockingMode,
                    onSelectEmployee = onSelectEmployee,
                    onSetBlockingMode = onSetBlockingMode,
                    onHide = { showToolbar = false }
                )
            }
        }
    }
}

@Composable
private fun LandscapeBlockingToolbar(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    blockingMode: ScheduleViewModel.BlockingMode,
    onSelectEmployee: (Employee?) -> Unit,
    onSetBlockingMode: (ScheduleViewModel.BlockingMode) -> Unit,
    onHide: () -> Unit,
) {
    val isCannotActive = blockingMode == ScheduleViewModel.BlockingMode.CANNOT
    val isCanOnlyActive = blockingMode == ScheduleViewModel.BlockingMode.CAN_ONLY

    LandscapeToolbarSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeChip(
                label = "לא יכול",
                active = isCannotActive,
                activeColor = BlockedRed,
                onClick = { onSetBlockingMode(ScheduleViewModel.BlockingMode.CANNOT) }
            )
            ModeChip(
                label = "יכול רק",
                active = isCanOnlyActive,
                activeColor = CanOnlyBlue,
                onClick = { onSetBlockingMode(ScheduleViewModel.BlockingMode.CAN_ONLY) }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(employees) { employee ->
                    val isSelected = selectedEmployee?.id == employee.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) onSelectEmployee(null) else onSelectEmployee(employee)
                        },
                        label = { Text(employee.name, fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                        )
                    )
                }
            }

            IconButton(onClick = onHide, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowDown, "הסתר",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LANDSCAPE MANUAL SCREEN
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeManualScreen(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    schedule: Map<String, List<String>>,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    templateData: TemplateData? = null,
    onSelectEmployee: (Employee?) -> Unit,
    onToggleEmployeeInShift: (Employee, String, String) -> Unit,
    onClose: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var showToolbar by remember { mutableStateOf(true) }
    var showBlockingWarning by remember { mutableStateOf(false) }
    var pendingAssignment by remember { mutableStateOf<Triple<Employee, String, String>?>(null) }
    var pendingIsBlocked by remember { mutableStateOf(false) }
    val warningSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            LandscapeTopBar(title = "סידור ידני — תצוגה מורחבת", onClose = onClose)

            Box(modifier = Modifier.weight(1f)) {
                SimpleScheduleTable(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    blocks = blocks,
                    canOnlyBlocks = canOnlyBlocks,
                    savingMode = savingMode,
                    schedule = schedule,
                    templateData = templateData,
                    isEditMode = false,
                    weekStartDate = weekStartDate,
                    onSetWeekStartDate = null,
                    onCellClick = { _, day, shift ->
                        selectedEmployee?.let { emp ->
                            val blockKey = "${emp.name}-$day-$shift"
                            val isBlocked = blocks[blockKey] == true
                            val isCanOnly = canOnlyBlocks[blockKey] == true
                            val hasCanOnlyRestrictions = canOnlyBlocks.any { (key, value) ->
                                value && key.startsWith("${emp.name}-")
                            }
                            val isRestricted = if (hasCanOnlyRestrictions) !isCanOnly else isBlocked
                            if (isRestricted) {
                                pendingAssignment = Triple(emp, day, shift)
                                pendingIsBlocked = isBlocked
                                showBlockingWarning = true
                            } else {
                                onToggleEmployeeInShift(emp, day, shift)
                            }
                        }
                    },
                    onDayHeaderClick = null,
                    isBlockingMode = false,
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = !showToolbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    ShowToolbarPill(onClick = { showToolbar = true })
                }
            }

            AnimatedVisibility(
                visible = showToolbar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                LandscapeManualToolbar(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    onSelectEmployee = onSelectEmployee,
                    onHide = { showToolbar = false }
                )
            }
        }

        // Blocking warning sheet
        if (showBlockingWarning) {
            ModalBottomSheet(
                onDismissRequest = { showBlockingWarning = false; pendingAssignment = null },
                sheetState = warningSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = BlockedRed,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            "שיבוץ מוגבל",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlockedRed
                        )
                        Text(
                            text = if (pendingIsBlocked)
                                "התא חסום לעובד זה. לשבץ בכל זאת?"
                            else
                                "העובד לא יכול לעבוד בתא זה. לשבץ בכל זאת?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showBlockingWarning = false; pendingAssignment = null },
                                modifier = Modifier.weight(1f)
                            ) { Text("ביטול") }
                            Button(
                                onClick = {
                                    pendingAssignment?.let { (emp, day, shift) ->
                                        onToggleEmployeeInShift(emp, day, shift)
                                    }
                                    showBlockingWarning = false
                                    pendingAssignment = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlockedRed),
                                modifier = Modifier.weight(1f)
                            ) { Text("שבץ בכל זאת", color = Color.White) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeManualToolbar(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onSelectEmployee: (Employee?) -> Unit,
    onHide: () -> Unit,
) {
    LandscapeToolbarSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(BlockedRed, "חסום")
                LegendDot(CanOnlyBlue, "יכול")
                LegendDot(PrimaryTeal, "משובץ")
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(employees) { employee ->
                    val isSelected = selectedEmployee?.id == employee.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) onSelectEmployee(null) else onSelectEmployee(employee)
                        },
                        label = { Text(employee.name, fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                        )
                    )
                }
            }

            IconButton(onClick = onHide, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowDown, "הסתר",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LANDSCAPE PREVIEW SCREEN
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapePreviewScreen(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    templateData: TemplateData? = null,
    onUpdateCell: (String, String) -> Unit,
    onShareSchedule: (ShareType) -> Unit,
    onReturnToBlocking: () -> Unit,
    onClose: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var showToolbar by remember { mutableStateOf(true) }
    var showStatsSheet by remember { mutableStateOf(false) }
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var editableSchedule by remember {
        mutableStateOf(schedule.mapValues { it.value.joinToString(", ") })
    }
    LaunchedEffect(schedule) {
        editableSchedule = schedule.mapValues { it.value.joinToString(", ") }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            LandscapeTopBar(title = "תצוגה מקדימה — מורחבת", onClose = onClose)

            Box(modifier = Modifier.weight(1f)) {
                SimpleScheduleTable(
                    employees = employees,
                    selectedEmployee = null,
                    blocks = emptyMap(),
                    canOnlyBlocks = emptyMap(),
                    savingMode = savingMode,
                    schedule = schedule,
                    templateData = templateData,
                    isEditMode = true,
                    weekStartDate = weekStartDate,
                    isBlockingMode = false,
                    onCellEdit = { key, value ->
                        editableSchedule = editableSchedule.toMutableMap().apply { put(key, value) }
                        onUpdateCell(key, value)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = !showToolbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    ShowToolbarPill(onClick = { showToolbar = true })
                }
            }

            AnimatedVisibility(
                visible = showToolbar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                LandscapePreviewToolbar(
                    onDownload = { onShareSchedule(ShareType.DOWNLOAD_IMAGE) },
                    onWhatsApp = { onShareSchedule(ShareType.WHATSAPP_IMAGE) },
                    onStats = {
                        showStatsSheet = true
                        scope.launch { statsSheetState.show() }
                    },
                    onReturnToBlocking = onReturnToBlocking,
                    onHide = { showToolbar = false }
                )
            }
        }

        if (showStatsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatsSheet = false },
                sheetState = statsSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(2.dp)
                                )
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.BarChart, null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "סטטיסטיקה שבועית",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        val scheduleKey = remember(schedule) { schedule.hashCode() }
                        key(scheduleKey) {
                            EmployeeStatistics(
                                employees = employees,
                                schedule = schedule,
                                savingMode = savingMode,
                                templateData = templateData
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapePreviewToolbar(
    onDownload: () -> Unit,
    onWhatsApp: () -> Unit,
    onStats: () -> Unit,
    onReturnToBlocking: () -> Unit,
    onHide: () -> Unit,
) {
    LandscapeToolbarSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LandscapeActionButton(Icons.Default.Download, "גלריה", PrimaryTeal, onDownload)
            LandscapeActionButton(
                Icons.AutoMirrored.Filled.Chat, "ווצאפ",
                Color(0xFF25D366), onWhatsApp
            )
            LandscapeActionButton(Icons.Default.BarChart, "סטטיסטיקה", PrimaryTeal, onStats)
            LandscapeActionButton(Icons.Default.Edit, "חסימות", PrimaryTeal, onReturnToBlocking)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onHide, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowDown, "הסתר",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun LandscapeTopBar(title: String, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.12f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // In RTL: first = right side (where close button should be)
            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Close, "סגור",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(36.dp)) // balance
        }
    }
}

@Composable
private fun LandscapeToolbarSurface(content: @Composable RowScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.2f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun ShowToolbarPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp, null,
                tint = PrimaryTeal,
                modifier = Modifier.size(16.dp)
            )
            Text("הצג כלים", fontSize = 11.sp, color = PrimaryTeal)
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (active) activeColor else MaterialTheme.colorScheme.surfaceVariant,
        border = if (!active) BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else activeColor
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LandscapeActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
