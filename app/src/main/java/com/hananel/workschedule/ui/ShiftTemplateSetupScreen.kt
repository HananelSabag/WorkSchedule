package com.hananel.workschedule.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hananel.workschedule.data.DayColumn
import com.hananel.workschedule.data.ShiftRow
import com.hananel.workschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftTemplateSetupScreen(
    shiftRows: List<ShiftRow>,
    dayColumns: List<DayColumn>,
    hasExistingTemplate: Boolean = false, // New: for dynamic title
    onAddShiftRow: (String, String) -> Unit, // Changed: now requires name and hours
    onEditShiftRow: (Int, String, String) -> Unit,
    onDeleteShiftRow: (Int) -> Unit,
    onMoveShiftRow: (Int, Int) -> Unit, // New: move row from index to new index
    onToggleDayColumn: (Int) -> Unit,
    onAutoSave: () -> Unit, // New: just save, don't navigate
    onSaveAndExit: () -> Unit, // New: save and navigate to home
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) } // New: dialog for adding
    var showEditDialog by remember { mutableStateOf(false) }
    var editingRowIndex by remember { mutableStateOf(-1) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingRowIndex by remember { mutableStateOf(-1) }
    
    // Handle system back button - save before leaving
    BackHandler {
        onSaveAndExit()
    }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSaveAndExit) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "חזור", tint = PrimaryTeal)
                        }
                        Text(
                            text = if (hasExistingTemplate) "עריכת תבנית סידור" else "הגדרת תבנית - ראשונה",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        if (shiftRows.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryTeal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "${shiftRows.size}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                        } else {
                            Spacer(Modifier.size(48.dp))
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Compact info chip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryTeal.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                        Text(
                            text = "הוסף משמרות + סמן ימים. מינימום: 2 משמרות, 4 ימים",
                            fontSize = 12.sp,
                            color = PrimaryTeal
                        )
                    }
                }
                
                // Shift Rows Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "משמרות (${shiftRows.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            
                            // Add button - opens dialog
                            IconButton(
                                onClick = { showAddDialog = true },
                                enabled = shiftRows.size < 8,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (shiftRows.size < 8) PrimaryTeal else Color.Gray,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "הוסף משמרת"
                                )
                            }
                        }
                        
                        // Shift rows list with drag & drop
                        if (shiftRows.isEmpty()) {
                            Text(
                                text = "לחץ על + להוספת משמרת ראשונה",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            DraggableShiftRowsList(
                                shiftRows = shiftRows,
                onEdit = { index ->
                    editingRowIndex = index
                    showEditDialog = true
                },
                onDelete = { index ->
                    deletingRowIndex = index
                    showDeleteConfirm = true
                },
                onMove = { fromIndex, toIndex ->
                    onMoveShiftRow(fromIndex, toIndex)
                    onAutoSave() // Auto-save after reordering
                },
                                canDelete = shiftRows.size > 2
                            )
                        }
                        
                        if (shiftRows.size >= 8) {
                            Text(
                                text = "הגעת למקסימום משמרות (8)",
                                fontSize = 12.sp,
                                color = Orange,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Day Columns Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ימים (${dayColumns.count { it.isEnabled }}/${dayColumns.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                        
                        // Day columns compact grid (2 columns for space efficiency)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // First column: days 0, 2, 4, 6
                                dayColumns.filterIndexed { idx, _ -> idx % 2 == 0 }.forEachIndexed { _, column ->
                                    val index = dayColumns.indexOf(column)
                                    DayColumnItemCompact(
                                        dayColumn = column,
                                        onToggle = { 
                                            onToggleDayColumn(index)
                                            onAutoSave() // Auto-save after toggle
                                        },
                                        canDisable = dayColumns.count { it.isEnabled } > 4
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Second column: days 1, 3, 5
                                dayColumns.filterIndexed { idx, _ -> idx % 2 == 1 }.forEachIndexed { _, column ->
                                    val index = dayColumns.indexOf(column)
                                    DayColumnItemCompact(
                                        dayColumn = column,
                                        onToggle = { 
                                            onToggleDayColumn(index)
                                            onAutoSave() // Auto-save after toggle
                                        },
                                        canDisable = dayColumns.count { it.isEnabled } > 4
                                    )
                                }
                            }
                        }
                        
                        val enabledCount = dayColumns.count { it.isEnabled }
                        if (enabledCount <= 4) {
                            Text(
                                text = "דרושים לפחות 4 ימים",
                                fontSize = 12.sp,
                                color = Orange,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Table Preview Section
                val enabledDaysForPreview = dayColumns.filter { it.isEnabled }
                if (shiftRows.isNotEmpty() && enabledDaysForPreview.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Orange.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Orange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "תצוגה מקדימה של הטבלה",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Orange
                                )
                            }
                            
                            // Table preview
                            TablePreview(
                                shiftRows = shiftRows,
                                dayColumns = enabledDaysForPreview
                            )
                        }
                    }
                }
                
                // Done Button (auto-save on every action)
                val validRows = shiftRows.filter { it.shiftName.isNotBlank() && it.shiftHours.isNotBlank() }
                val enabledDays = dayColumns.count { it.isEnabled }
                val canFinish = validRows.size >= 2 && enabledDays >= 4
                
                // Premium style Finish Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (canFinish) 
                                    listOf(PrimaryTeal, PrimaryTealDark)
                                else 
                                    listOf(Color.Gray, Color.DarkGray)
                            )
                        )
                        .clickable(enabled = canFinish) {
                            if (canFinish) {
                                onSaveAndExit()
                            }
                        },
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
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "סיום עריכה",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "שמירה וחזרה למסך הבית",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Validation messages
                if (validRows.size < 2) {
                    Text(
                        text = "נדרשות לפחות 2 משמרות תקינות (עם שם ושעות)",
                        fontSize = 13.sp,
                        color = BlockedRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                
                if (shiftRows.size > validRows.size) {
                    Text(
                        text = "⚠️ שורות ריקות לא יישמרו",
                        fontSize = 12.sp,
                        color = Orange,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Add shift dialog - opens BEFORE creating row
    if (showAddDialog) {
        ShiftInputDialog(
            title = "הוספת משמרת חדשה",
            initialName = "",
            initialHours = "",
                onConfirm = { name, hours ->
                    onAddShiftRow(name, hours)
                    onAutoSave() // Auto-save after adding
                    showAddDialog = false
                },
            onDismiss = { showAddDialog = false }
        )
    }
    
    // Edit shift dialog
    if (showEditDialog && editingRowIndex >= 0) {
        val existingRow = shiftRows.getOrNull(editingRowIndex)
        ShiftInputDialog(
            title = "עריכת משמרת",
            initialName = existingRow?.shiftName ?: "",
            initialHours = existingRow?.shiftHours ?: "",
            onConfirm = { name, hours ->
                onEditShiftRow(editingRowIndex, name, hours)
                onAutoSave() // Auto-save after editing
                showEditDialog = false
                editingRowIndex = -1
            },
            onDismiss = { 
                showEditDialog = false
                editingRowIndex = -1
            }
        )
    }
    
    // Delete confirmation sheet
    if (showDeleteConfirm && deletingRowIndex >= 0) {
        val shiftName = shiftRows.getOrNull(deletingRowIndex)?.shiftName ?: ""
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirm = false },
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
                            .background(MaterialTheme.colorScheme.outlineVariant, androidx.compose.foundation.shape.CircleShape)
                    )
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = BlockedRed.copy(alpha = 0.1f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, null, tint = BlockedRed, modifier = Modifier.size(28.dp))
                        }
                    }
                    Text("מחיקת משמרת", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(
                        "האם למחוק את \"$shiftName\"?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("ביטול", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = {
                                onDeleteShiftRow(deletingRowIndex)
                                onAutoSave()
                                showDeleteConfirm = false
                                deletingRowIndex = -1
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlockedRed)
                        ) { Text("מחק", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// Shift input bottom sheet with structured time fields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftInputDialog(
    title: String,
    initialName: String,
    initialHours: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var shiftName by remember { mutableStateOf(initialName) }

    val hoursParts = if (initialHours.isNotBlank()) initialHours.split("-") else emptyList()
    val startParts = if (hoursParts.isNotEmpty()) hoursParts[0].split(":") else emptyList()
    val endParts = if (hoursParts.size > 1) hoursParts[1].split(":") else emptyList()

    var startHour by remember { mutableStateOf(startParts.getOrNull(0)?.trim() ?: "") }
    var startMinute by remember { mutableStateOf(startParts.getOrNull(1)?.trim() ?: "") }
    var endHour by remember { mutableStateOf(endParts.getOrNull(0)?.trim() ?: "") }
    var endMinute by remember { mutableStateOf(endParts.getOrNull(1)?.trim() ?: "") }

    val isValid = shiftName.isNotBlank() &&
            startHour.isNotBlank() && startMinute.isNotBlank() &&
            endHour.isNotBlank() && endMinute.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        .background(MaterialTheme.colorScheme.outlineVariant, androidx.compose.foundation.shape.CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = PrimaryTeal.copy(alpha = 0.12f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                        }
                    }
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Shift name
                OutlinedTextField(
                    value = shiftName,
                    onValueChange = { shiftName = it },
                    label = { Text("שם המשמרת") },
                    placeholder = { Text("בוקר, צהריים, לילה...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        focusedLabelColor = PrimaryTeal
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Time section
                Text("שעות פעילות", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryTeal)

                // Start + End time rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("התחלה:", fontSize = 13.sp, modifier = Modifier.width(52.dp))
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..23)) startHour = it },
                        label = { Text("שע") },
                        placeholder = { Text("07") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = startMinute,
                        onValueChange = { if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..59)) startMinute = it },
                        label = { Text("דק") },
                        placeholder = { Text("00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("סיום:", fontSize = 13.sp, modifier = Modifier.width(52.dp))
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..23)) endHour = it },
                        label = { Text("שע") },
                        placeholder = { Text("15") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = endMinute,
                        onValueChange = { if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..59)) endMinute = it },
                        label = { Text("דק") },
                        placeholder = { Text("00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ביטול") }
                    Button(
                        onClick = {
                            val formattedHours = "${startHour.padStart(2, '0')}:${startMinute.padStart(2, '0')}-${endHour.padStart(2, '0')}:${endMinute.padStart(2, '0')}"
                            onConfirm(shiftName, formattedHours)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        enabled = isValid
                    ) { Text("שמור", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// Draggable shift rows list with reorder capability
@Composable
private fun DraggableShiftRowsList(
    shiftRows: List<ShiftRow>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    canDelete: Boolean
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var itemHeight by remember { mutableStateOf(0f) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        shiftRows.forEachIndexed { index, row ->
            val isDragged = draggedIndex == index
            val isTarget = targetIndex == index
            
            ShiftRowItemDraggable(
                shiftRow = row,
                index = index,
                totalItems = shiftRows.size,
                isDragged = isDragged,
                isTarget = isTarget,
                onEdit = { onEdit(index) },
                onDelete = { onDelete(index) },
                onDragStart = { 
                    draggedIndex = index
                    targetIndex = index
                },
                onDragEnd = {
                    if (draggedIndex != null && targetIndex != null && draggedIndex != targetIndex) {
                        onMove(draggedIndex!!, targetIndex!!)
                    }
                    draggedIndex = null
                    targetIndex = null
                },
                onDragOver = { newTarget -> 
                    if (newTarget in shiftRows.indices) {
                        targetIndex = newTarget
                    }
                },
                onHeightMeasured = { height -> itemHeight = height },
                itemHeight = itemHeight,
                canDelete = canDelete
            )
        }
    }
}

// Component for each draggable shift row item
@Composable
private fun ShiftRowItemDraggable(
    shiftRow: ShiftRow,
    index: Int,
    totalItems: Int,
    isDragged: Boolean,
    isTarget: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragOver: (Int) -> Unit,
    onHeightMeasured: (Float) -> Unit,
    itemHeight: Float,
    canDelete: Boolean
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                if (itemHeight == 0f) {
                    onHeightMeasured(coordinates.size.height.toFloat())
                }
            }
            .graphicsLayer {
                alpha = if (isDragging) 0.7f else 1f
                scaleX = if (isDragging) 1.03f else 1f
                scaleY = if (isDragging) 1.03f else 1f
                translationY = if (isDragging) dragOffset else 0f
            }
            .zIndex(if (isDragging) 10f else 0f)
            // Long press on card (not hamburger) to drag
            .pointerInput(index, totalItems, itemHeight) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        dragOffset = 0f
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = 0f
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = 0f
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y
                        
                        // Calculate target index based on cumulative drag distance
                        if (itemHeight > 0) {
                            val spacingPx = 8.dp.toPx() // spacing between items
                            val effectiveItemHeight = itemHeight + spacingPx
                            val positionShift = (dragOffset / effectiveItemHeight).toInt()
                            val newTargetIndex = (index + positionShift).coerceIn(0, totalItems - 1)
                            onDragOver(newTargetIndex)
                        }
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> PrimaryTeal.copy(alpha = 0.25f) // Teal when dragging
                isTarget && !isDragged -> Orange.copy(alpha = 0.15f) // Orange for target position
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        border = when {
            isDragging -> BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.6f))
            isTarget && !isDragged -> BorderStroke(1.dp, Orange.copy(alpha = 0.5f))
            else -> BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.12f))
        },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 12.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle (hamburger icon) - INSTANT drag on touch (no long press)
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "גרור לשינוי סדר",
                tint = PrimaryTeal,
                modifier = Modifier
                    .size(24.dp)
                    .pointerInput(index, totalItems, itemHeight) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffset = 0f
                                onDragStart()
                            },
                            onDragEnd = {
                                isDragging = false
                                dragOffset = 0f
                                onDragEnd()
                            },
                            onDragCancel = {
                                isDragging = false
                                dragOffset = 0f
                                onDragEnd()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                                
                                // Calculate target index based on cumulative drag distance
                                if (itemHeight > 0) {
                                    val spacingPx = 8.dp.toPx() // spacing between items
                                    val effectiveItemHeight = itemHeight + spacingPx
                                    val positionShift = (dragOffset / effectiveItemHeight).toInt()
                                    val newTargetIndex = (index + positionShift).coerceIn(0, totalItems - 1)
                                    onDragOver(newTargetIndex)
                                }
                            }
                        )
                    }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content - clickable to edit (not draggable here to avoid conflict)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = shiftRow.shiftName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = shiftRow.shiftHours,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ערוך",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "מחק",
                        tint = if (canDelete) BlockedRed else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Compact component for day columns - saves space with 2-column grid
@Composable
private fun DayColumnItemCompact(
    dayColumn: DayColumn,
    onToggle: () -> Unit,
    canDisable: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !dayColumn.isEnabled || canDisable,
                onClick = onToggle
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (dayColumn.isEnabled)
                PrimaryGreen.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (dayColumn.isEnabled) PrimaryGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayColumn.dayNameHebrew,
                fontSize = 14.sp,
                fontWeight = if (dayColumn.isEnabled) FontWeight.Bold else FontWeight.Normal,
                color = if (dayColumn.isEnabled) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = dayColumn.isEnabled,
                onCheckedChange = { onToggle() },
                enabled = !dayColumn.isEnabled || canDisable,
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = PrimaryGreen.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// Table preview component - Beautiful modern design
@Composable
private fun TablePreview(
    shiftRows: List<ShiftRow>,
    dayColumns: List<DayColumn>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(2.dp)
        ) {
            // Header row with days
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryTeal,
                                Color(0xFF3D8B85)
                            )
                        )
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Corner cell with logo placeholder
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "סידור",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Day headers
                dayColumns.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayNameHebrew,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Shift rows with alternating colors
            shiftRows.forEachIndexed { index, shift ->
                val isEven = index % 2 == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shift name cell
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = 0.9f),
                                        PrimaryTeal.copy(alpha = 0.7f)
                                    )
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = shift.shiftName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = shift.shiftHours,
                                fontSize = 7.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                    
                    // Empty cells for days
                    dayColumns.forEach { _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                                .background(
                                    PrimaryTeal.copy(alpha = 0.04f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = PrimaryTeal.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "—",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Bottom rounded corners padding
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
    
    // Info text below the card
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "💡",
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "כך תיראה הטבלה שלך",
            fontSize = 11.sp,
            color = Orange,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

