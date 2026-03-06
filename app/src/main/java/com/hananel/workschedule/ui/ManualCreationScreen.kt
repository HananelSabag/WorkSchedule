package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCreationScreen(
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
    onAddFreeTextToCell: (String, String) -> Unit,
    onGenerateManualSchedule: () -> Unit,
    onReturnToBlocking: () -> Unit,
    onClearManualSchedule: () -> Unit,
    onBackClick: () -> Unit,
    onEnterLandscape: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBlockingWarning by remember { mutableStateOf(false) }
    var pendingAssignment by remember { mutableStateOf<Triple<Employee, String, String>?>(null) }

    var showFreeTextSheet by remember { mutableStateOf(false) }
    var freeTextCellKey by remember { mutableStateOf("") }
    var freeTextValue by remember { mutableStateOf("") }

    var showResetSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ─── Header ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp),
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
                    IconButton(onClick = onReturnToBlocking) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "חזור לחסימות",
                            tint = PrimaryTeal
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "יצירת סידור ידני",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryTeal,
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = onEnterLandscape,
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryTeal.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ScreenRotation, null, tint = PrimaryTeal, modifier = Modifier.size(13.dp))
                                Text("תצוגה אופקית", fontSize = 10.sp, color = PrimaryTeal, fontWeight = FontWeight.Medium)
                            }
                        }
                        IconButton(
                            onClick = { showResetSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "איפוס",
                                tint = BlockedRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ─── Schedule Table ─────────────────────────────────────────────
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
                            showBlockingWarning = true
                        } else {
                            onToggleEmployeeInShift(emp, day, shift)
                        }
                    }
                },
                onLongPress = { day, shift ->
                    freeTextCellKey = "$day-$shift"
                    freeTextValue = schedule[freeTextCellKey]?.joinToString(", ") ?: ""
                    showFreeTextSheet = true
                },
                onDayHeaderClick = null,
                isBlockingMode = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // ─── Employee Selection Panel ────────────────────────────────────
            EmployeeSelectionPanel(
                employees = employees,
                selectedEmployee = selectedEmployee,
                onSelectEmployee = onSelectEmployee
            )

            // ─── Legend ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot("חסום", BlockedRed)
                    LegendDot("יכול", CanOnlyBlue)
                    LegendDot("משובץ", MaterialTheme.colorScheme.onSurface)
                    Text("לחץ ארוך לטקסט", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ─── Save Button ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(PrimaryTeal, PrimaryTealDark)))
                    .clickable(onClick = onGenerateManualSchedule),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Text(
                        text = "שמירה והצגה סופית",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // ─── Blocking Warning Sheet ──────────────────────────────────────────
        if (showBlockingWarning) {
            val isCanOnlyRestriction = pendingAssignment?.let { (employee, day, shift) ->
                val hasCanOnly = canOnlyBlocks.any { (key, value) ->
                    value && key.startsWith("${employee.name}-")
                }
                hasCanOnly && canOnlyBlocks["${employee.name}-$day-$shift"] != true
            } ?: false

            ModalBottomSheet(
                onDismissRequest = { showBlockingWarning = false; pendingAssignment = null },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = (if (isCanOnlyRestriction) CanOnlyBlue else BlockedRed).copy(alpha = 0.1f)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Warning,
                                    null,
                                    tint = if (isCanOnlyRestriction) CanOnlyBlue else BlockedRed,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        pendingAssignment?.let { (employee, day, shift) ->
                            Text(
                                text = if (isCanOnlyRestriction) "תא מחוץ לאיזורים מותרים" else "תא חסום",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isCanOnlyRestriction)
                                    "${employee.name} סומן 'יכול רק' בתאים ספציפיים. $shift ביום $day לא ברשימה."
                                else
                                    "${employee.name} חסום ב-$shift ביום $day.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showBlockingWarning = false; pendingAssignment = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("ביטול", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = {
                                    pendingAssignment?.let { (emp, day, shift) ->
                                        onToggleEmployeeInShift(emp, day, shift)
                                    }
                                    showBlockingWarning = false
                                    pendingAssignment = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlockedRed)
                            ) { Text("שבץ בכל זאת", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        // ─── Free Text Sheet ─────────────────────────────────────────────────
        if (showFreeTextSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFreeTextSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = PrimaryTeal.copy(alpha = 0.12f)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                                }
                            }
                            Text("עריכת תא", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = freeTextValue,
                            onValueChange = { freeTextValue = it },
                            label = { Text("טקסט חופשי") },
                            placeholder = { Text("שם עובד, הערה...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                focusedLabelColor = PrimaryTeal
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showFreeTextSheet = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("ביטול") }
                            Button(
                                onClick = {
                                    onAddFreeTextToCell(freeTextCellKey, freeTextValue)
                                    showFreeTextSheet = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                            ) { Text("שמור", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        // ─── Reset Confirmation Sheet ────────────────────────────────────────
        if (showResetSheet) {
            ModalBottomSheet(
                onDismissRequest = { showResetSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Orange.copy(alpha = 0.1f)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Refresh, null, tint = Orange, modifier = Modifier.size(32.dp))
                            }
                        }
                        Text("איפוס שיבוצים ידניים", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(
                            "כל השיבוצים הידניים יימחקו. החסימות לא יושפעו.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showResetSheet = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("ביטול", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = {
                                    onClearManualSchedule()
                                    showResetSheet = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Orange)
                            ) { Text("אפס", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(10.dp), color = color, shape = RoundedCornerShape(3.dp)) {}
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EmployeeSelectionPanel(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onSelectEmployee: (Employee?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedEmployee != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryTeal.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(color = PrimaryTeal.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(28.dp).padding(4.dp),
                                tint = PrimaryTeal
                            )
                        }
                        Text(selectedEmployee.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal, modifier = Modifier.weight(1f))
                        Text("לחץ על תא לשיבוץ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("בחר עובד:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("${employees.size} עובדים", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(employees) { employee ->
                    val isSelected = selectedEmployee?.id == employee.id
                    FilterChip(
                        onClick = { if (isSelected) onSelectEmployee(null) else onSelectEmployee(employee) },
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
