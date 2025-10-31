package com.hananel.workschedule.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                TopAppBar(
                    title = {
                        Text(
                            text = if (hasExistingTemplate) {
                                "עריכת טבלת סידור עבודה"
                            } else {
                                "יצירת טבלה - פעם ראשונה"
                            },
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onSaveAndExit) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "חזור",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryTeal
                    )
                )
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
                // Instructions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD) // Light blue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "כיצד להגדיר את הטבלה?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                        
                        Text(
                            text = "• הוסף משמרות (שורות) עם שם ושעות פעילות\n" +
                                    "• סמן/בטל ימים (עמודות) לפי צורך\n" +
                                    "• לפחות 2 משמרות ו-4 ימים נדרשים\n" +
                                    "• לחץ על משמרת לעריכה",
                            fontSize = 14.sp,
                            color = Color(0xFF1565C0),
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // Shift Rows Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow!
                    shape = RoundedCornerShape(12.dp)
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
                                    containerColor = if (shiftRows.size < 8) PrimaryGreen else Color.Gray,
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
                                color = Color(0xFFFF9800),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Day Columns Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow!
                    shape = RoundedCornerShape(12.dp)
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
                                color = Color(0xFFFF9800),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Done Button (auto-save on every action)
                val validRows = shiftRows.filter { it.shiftName.isNotBlank() && it.shiftHours.isNotBlank() }
                val enabledDays = dayColumns.count { it.isEnabled }
                val canFinish = validRows.size >= 2 && enabledDays >= 4
                
                Button(
                    onClick = {
                        if (canFinish) {
                            onSaveAndExit() // Save and exit to home
                        }
                    },
                    enabled = canFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "סיום עריכה",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Validation messages
                if (validRows.size < 2) {
                    Text(
                        text = "נדרשות לפחות 2 משמרות תקינות (עם שם ושעות)",
                        fontSize = 13.sp,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                
                if (shiftRows.size > validRows.size) {
                    Text(
                        text = "⚠️ שורות ריקות לא יישמרו",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800),
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
    
    // Delete confirmation dialog
    if (showDeleteConfirm && deletingRowIndex >= 0) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = {
                Text(
                    text = "מחיקת משמרת",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "האם למחוק את המשמרת \"${shiftRows.getOrNull(deletingRowIndex)?.shiftName}\"?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteShiftRow(deletingRowIndex)
                        onAutoSave() // Auto-save after deletion
                        showDeleteConfirm = false
                        deletingRowIndex = -1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("מחק")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ביטול")
                }
            }
        )
    }
}

// Shift input dialog with structured time fields
@Composable
private fun ShiftInputDialog(
    title: String,
    initialName: String,
    initialHours: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var shiftName by remember { mutableStateOf(initialName) }
    
    // Parse existing hours or use empty strings for new shifts
    val hoursParts = if (initialHours.isNotBlank()) initialHours.split("-") else emptyList()
    val startParts = if (hoursParts.isNotEmpty()) hoursParts[0].split(":") else emptyList()
    val endParts = if (hoursParts.size > 1) hoursParts[1].split(":") else emptyList()
    
    var startHour by remember { mutableStateOf(startParts.getOrNull(0)?.trim() ?: "") }
    var startMinute by remember { mutableStateOf(startParts.getOrNull(1)?.trim() ?: "") }
    var endHour by remember { mutableStateOf(endParts.getOrNull(0)?.trim() ?: "") }
    var endMinute by remember { mutableStateOf(endParts.getOrNull(1)?.trim() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = PrimaryTeal
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shift name field
                OutlinedTextField(
                    value = shiftName,
                    onValueChange = { shiftName = it },
                    label = { Text("שם המשמרת") },
                    placeholder = { Text("למשל: בוקר, צהריים, לילה") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        focusedLabelColor = PrimaryTeal
                    )
                )
                
                // Time fields section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "שעות פעילות",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    
                    // Start time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("התחלה:", fontSize = 14.sp, modifier = Modifier.width(60.dp))
                        OutlinedTextField(
                            value = startHour,
                            onValueChange = { 
                                if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..23)) {
                                    startHour = it
                                }
                            },
                            label = { Text("שעה") },
                            placeholder = { Text("07", color = Color.Gray) },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal
                            )
                        )
                        Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = startMinute,
                            onValueChange = { 
                                if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..59)) {
                                    startMinute = it
                                }
                            },
                            label = { Text("דקות") },
                            placeholder = { Text("00", color = Color.Gray) },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal
                            )
                        )
                    }
                    
                    // End time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("סיום:", fontSize = 14.sp, modifier = Modifier.width(60.dp))
                        OutlinedTextField(
                            value = endHour,
                            onValueChange = { 
                                if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..23)) {
                                    endHour = it
                                }
                            },
                            label = { Text("שעה") },
                            placeholder = { Text("15", color = Color.Gray) },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal
                            )
                        )
                        Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = endMinute,
                            onValueChange = { 
                                if (it.isEmpty() || (it.length <= 2 && (it.toIntOrNull() ?: -1) in 0..59)) {
                                    endMinute = it
                                }
                            },
                            label = { Text("דקות") },
                            placeholder = { Text("00", color = Color.Gray) },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            val isValid = shiftName.isNotBlank() && 
                          startHour.isNotBlank() && startMinute.isNotBlank() &&
                          endHour.isNotBlank() && endMinute.isNotBlank()
            Button(
                onClick = {
                    val formattedHours = "${startHour.padStart(2, '0')}:${startMinute.padStart(2, '0')}-${endHour.padStart(2, '0')}:${endMinute.padStart(2, '0')}"
                    onConfirm(shiftName, formattedHours)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                enabled = isValid
            ) {
                Text("אישור")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
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
                isDragging -> Color(0xFFB2DFDB) // Darker teal when dragging
                isTarget && !isDragged -> Color(0xFFFFF9C4) // Yellow for target position
                else -> Color(0xFFF5F5F5)
            }
        ),
        shape = RoundedCornerShape(8.dp),
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
                        tint = if (canDelete) Color.Red else Color.Gray,
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
            containerColor = if (dayColumn.isEnabled) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp),
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
                color = if (dayColumn.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
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
                    uncheckedTrackColor = Color.Gray,
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = Color(0xFFB0BEC5)
                )
            )
        }
    }
}

