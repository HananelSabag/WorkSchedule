package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ScreenRotation
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
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    errorMessage: String,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    templateData: TemplateData? = null, // Dynamic template
    onUpdateCell: (String, String) -> Unit,
    onSaveSchedule: () -> Unit, // Deprecated - smart save system handles this automatically
    onShareSchedule: (ShareType) -> Unit,
    onBackClick: () -> Unit,
    onReturnToBlocking: () -> Unit, // New callback for returning to blocking
    onDismissError: () -> Unit, // New callback for dismissing error popup
    onEnterLandscape: () -> Unit = {}, // New: open full-screen landscape table view
    isEditingExistingSchedule: Boolean = false, // Smart save system - indicates if editing existing schedule
    modifier: Modifier = Modifier
) {
    var editableSchedule by remember {
        mutableStateOf(schedule.mapValues { it.value.joinToString(", ") })
    }

    // Statistics bottom sheet
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showStatisticsSheet by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }

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
            // Status bar spacer
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
            }

            // ── Themed Header ────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.12f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "חזור",
                                tint = PrimaryTeal
                            )
                        }

                        // Title + badge
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (isEditingExistingSchedule) "צפייה בסידור" else "סידור עבודה",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryTeal,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Landscape + Logo row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = onEnterLandscape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "מצב אופקי",
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp, PrimaryTeal.copy(alpha = 0.45f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_app_logo_new),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Info Box - Compact explanation about editing
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f))
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
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditingExistingSchedule) {
                                "עריכה: שינויים נשמרים אוטומטית • לחיצה ארוכה = עריכת טקסט חופשי"
                            } else {
                                "הסידור נשמר בהיסטוריה ✓ • לחיצה ארוכה על תא = עריכת טקסט חופשי"
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
                    templateData = templateData, // Dynamic template support
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
            
            // Actions button — opens ActionsBottomSheet
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryTeal, Color(0xFF00796B))
                            )
                        )
                        .clickable {
                            showActionsSheet = true
                            scope.launch { actionsSheetState.show() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "פעולות ושיתוף",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "גלריה • ווצאפ • סטטיסטיקה",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            // Return to Blocking Button - Always show (not just when editing)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryTeal, Color(0xFF00796B))
                            )
                        )
                        .clickable(onClick = onReturnToBlocking),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
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
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "עריכת חסימות",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "חזור למסך החסימות",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Transparent Error Popup - appears over content
        if (errorMessage.isNotEmpty()) {
            ErrorPopup(
                message = errorMessage,
                onDismiss = onDismissError
            )
        }
        
        // Actions BottomSheet (share + stats)
        if (showActionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActionsSheet = false },
                sheetState = actionsSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Handle
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
                        Text(
                            "פעולות ושיתוף",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryTeal
                        )
                        Spacer(Modifier.height(16.dp))

                        // Download
                        ActionSheetItem(
                            icon = Icons.Default.Download,
                            color = PrimaryTeal,
                            title = "הורד לגלריה",
                            subtitle = "שמור תמונה של הסידור בטלפון",
                            onClick = {
                                showActionsSheet = false
                                onShareSchedule(ShareType.DOWNLOAD_IMAGE)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // WhatsApp
                        ActionSheetItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            color = Color(0xFF25D366),
                            title = "שתף בווצאפ",
                            subtitle = "שלח לקבוצה או לאיש קשר",
                            onClick = {
                                showActionsSheet = false
                                onShareSchedule(ShareType.WHATSAPP_IMAGE)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Statistics
                        ActionSheetItem(
                            icon = Icons.Default.BarChart,
                            color = PrimaryTeal,
                            title = "סטטיסטיקה שבועית",
                            subtitle = "משמרות ושעות לכל עובד",
                            onClick = {
                                showActionsSheet = false
                                scope.launch { actionsSheetState.hide() }
                                showStatisticsSheet = true
                                scope.launch { statsSheetState.show() }
                            }
                        )
                    }
                }
            }
        }

        // Statistics - BottomSheet
        if (showStatisticsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatisticsSheet = false },
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
                        // Handle
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.BarChart, null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
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
internal fun EmployeeStatistics(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    savingMode: Map<String, Boolean> = emptyMap(),
    templateData: TemplateData? = null
) {
    // Helper function to extract employee name from text (first word before space)
    fun extractEmployeeName(text: String): String {
        return text.trim().split(" ").firstOrNull() ?: text
    }
    
    // Parse shift hours from time range string (e.g., "07:00-15:00" → 8.0 hours)
    // MUST be defined BEFORE calculateHours since it's called from there
    fun parseShiftHours(hoursText: String): Double {
        return try {
            // Format: "HH:MM-HH:MM" or "H:MM-H:MM"
            val parts = hoursText.split("-")
            if (parts.size != 2) return 8.0
            
            val startParts = parts[0].trim().split(":")
            val endParts = parts[1].trim().split(":")
            
            if (startParts.size < 2 || endParts.size < 2) return 8.0
            
            val startHour = startParts[0].toIntOrNull() ?: 0
            val startMin = startParts[1].toIntOrNull() ?: 0
            val endHour = endParts[0].toIntOrNull() ?: 0
            val endMin = endParts[1].toIntOrNull() ?: 0
            
            val startTotalMin = startHour * 60 + startMin
            var endTotalMin = endHour * 60 + endMin
            
            // Handle overnight shifts (e.g., 23:00-07:00)
            if (endTotalMin <= startTotalMin) {
                endTotalMin += 24 * 60
            }
            
            val totalMin = endTotalMin - startTotalMin
            val hours = totalMin / 60.0
            
            // Return reasonable value (between 0 and 24 hours)
            if (hours > 0 && hours <= 24) hours else 8.0
            
        } catch (e: Exception) {
            8.0 // Default fallback
        }
    }
    
    // SIMPLE: Calculate hours from shift hours definition in template
    fun calculateHours(cellKey: String): Double {
        val parts = cellKey.split("-")
        if (parts.size < 2) return 8.0
        
        val shiftName = parts.drop(1).joinToString("-")
        
        // Try to get hours from dynamic template first
        if (templateData != null) {
            val shiftRow = templateData.shiftRows.find { it.shiftName == shiftName }
            if (shiftRow != null) {
                // Parse hours from shiftHours string (e.g., "07:00-15:00")
                return parseShiftHours(shiftRow.shiftHours)
            }
        }
        
        // Fallback: hardcoded (for old schedules without template)
        val day = parts[0]
        val isSaving = savingMode[day] ?: false
        val shifts = ShiftDefinitions.getShiftsForDay(day, isSaving)
        val shift = shifts.find { it.id == shiftName }
        return shift?.hours ?: 8.0
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header - modern design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(28.dp).padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "📊 סטטיסטיקה שבועית",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal
                )
            }
            
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "שם עובד",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "משמרות",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(70.dp)
                )
                Text(
                    text = "שעות",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(70.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Table Rows - Calculate stats dynamically (will recompose when schedule changes)
            employees.forEach { employee ->
                // Count shifts - extract first word from text to match employee name
                var shiftCount = 0
                var totalHours = 0.0
                
                schedule.forEach { (cellKey, employeeList) ->
                    employeeList.forEach { text ->
                        val extractedName = extractEmployeeName(text)
                        if (extractedName.equals(employee.name, ignoreCase = true)) {
                            shiftCount++
                            // Calculate hours from shift definition in template
                            totalHours += calculateHours(cellKey)
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
                        color = MaterialTheme.colorScheme.onSurface, // Theme-aware text color
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Shift count badge
                    Surface(
                        color = if (shiftCount > 0) PrimaryTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.width(70.dp)
                    ) {
                        Text(
                            text = shiftCount.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (shiftCount > 0) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Weekly hours badge
                    Surface(
                        color = if (totalHours > 0) PrimaryTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
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
                            color = if (totalHours > 0) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Divider between rows (except last)
                if (employee != employees.last()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow!
    ) {
        Column {
            // Header Row - Days with dates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryTeal)
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
                            .background(PrimaryTeal)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow!
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
                tint = PrimaryTeal
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
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow!
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
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

@Composable
private fun ActionSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
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
