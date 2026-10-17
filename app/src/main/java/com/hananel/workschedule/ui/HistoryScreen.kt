package com.hananel.workschedule.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.hananel.workschedule.R
import com.hananel.workschedule.data.Schedule
import com.hananel.workschedule.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    schedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit,
    onDeleteSchedule: (Schedule) -> Unit,
    onRenameSchedule: (Schedule, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var sheetMode by remember { mutableStateOf(SheetMode.OPTIONS) }
    var renameText by remember { mutableStateOf("") }

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
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "חזור", tint = PrimaryTeal)
                        }
                        Text(
                            "סידורים שמורים",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        // Count badge
                        if (schedules.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryTeal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "${schedules.size}",
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
            }
        ) { paddingValues ->

            if (schedules.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = PrimaryTeal.copy(alpha = 0.08f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                null,
                                tint = PrimaryTeal.copy(alpha = 0.5f),
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "אין סידורים שמורים",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "סידורים שתיצור יופיעו כאן",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    items(schedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onOpen = { onScheduleClick(schedule) },
                            onMoreClick = {
                                selectedSchedule = schedule
                                renameText = schedule.weekStart
                                sheetMode = SheetMode.OPTIONS
                                scope.launch { sheetState.show() }
                            }
                        )
                    }
                }
            }
        }
    }

    // ─── Bottom Sheet ──────────────────────────────────────────────────────
    selectedSchedule?.let { schedule ->
        if (sheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { sheetState.hide() } },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    when (sheetMode) {
                        SheetMode.OPTIONS -> OptionsSheet(
                            schedule = schedule,
                            onOpen = {
                                scope.launch { sheetState.hide() }
                                onScheduleClick(schedule)
                            },
                            onRename = {
                                renameText = schedule.weekStart
                                sheetMode = SheetMode.RENAME
                            },
                            onDelete = { sheetMode = SheetMode.DELETE }
                        )
                        SheetMode.RENAME -> RenameSheet(
                            value = renameText,
                            onValueChange = { renameText = it },
                            onSave = {
                                if (renameText.isNotBlank()) {
                                    onRenameSchedule(schedule, renameText.trim())
                                }
                                scope.launch { sheetState.hide() }
                            },
                            onCancel = { sheetMode = SheetMode.OPTIONS }
                        )
                        SheetMode.DELETE -> DeleteSheet(
                            onConfirm = {
                                onDeleteSchedule(schedule)
                                scope.launch { sheetState.hide() }
                            },
                            onCancel = { sheetMode = SheetMode.OPTIONS }
                        )
                    }
                }
            }
        }
    }
}

private enum class SheetMode { OPTIONS, RENAME, DELETE }

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    onOpen: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calendar icon
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryTeal.copy(alpha = 0.12f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
                }
            }

            // Info
            Column(Modifier.weight(1f)) {
                Text(
                    "שבוע ${formatDate(schedule.weekStart)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatDateTime(schedule.createdDate),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Open button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(PrimaryTeal, Color(0xFF00796B))))
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("פתח", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // More options
            IconButton(onClick = onMoreClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    "אפשרויות",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionsSheet(
    schedule: Schedule,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(12.dp))

        Text(
            "שבוע ${formatDate(schedule.weekStart)}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            formatDateTime(schedule.createdDate),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        SheetActionRow(icon = Icons.Default.FolderOpen, label = "פתח סידור", color = PrimaryTeal, onClick = onOpen)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
        SheetActionRow(icon = Icons.Default.Edit, label = "שנה שם", color = MaterialTheme.colorScheme.onSurface, onClick = onRename)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
        SheetActionRow(icon = Icons.Default.Delete, label = "מחק סידור", color = BlockedRed, onClick = onDelete)
    }
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun RenameSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
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
        Spacer(Modifier.height(4.dp))
        Text("שנה שם", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("שם הסידור") },
            textStyle = androidx.compose.ui.text.TextStyle(
                textDirection = androidx.compose.ui.text.style.TextDirection.ContentOrRtl,
                fontSize = 16.sp
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryTeal,
                focusedLabelColor = PrimaryTeal
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("ביטול") }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) { Text("שמור", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun DeleteSheet(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
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
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = BlockedRed.copy(alpha = 0.1f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Delete, null, tint = BlockedRed, modifier = Modifier.size(32.dp))
            }
        }
        Text("מחק סידור?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            "לא ניתן לשחזר סידור שנמחק",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("ביטול", fontWeight = FontWeight.SemiBold) }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlockedRed)
            ) { Text("מחק", fontWeight = FontWeight.Bold) }
        }
    }
}

private fun formatDate(dateString: String): String = try {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("he", "IL"))
    outputFormat.format(inputFormat.parse(dateString)!!)
} catch (_: Exception) { dateString }

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("he", "IL")).format(Date(timestamp))
