package com.hananel.workschedule.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.data.Reminder
import com.hananel.workschedule.data.ReminderTime
import com.hananel.workschedule.notifications.ReminderManager
import com.hananel.workschedule.ui.theme.AccentIndigo
import com.hananel.workschedule.ui.theme.AccentIndigoDark
import com.hananel.workschedule.ui.theme.BlockedRed
import com.hananel.workschedule.ui.theme.Orange
import java.util.Calendar
import java.util.UUID

// ─── Hebrew helpers ───────────────────────────────────────────────────────────

private val DAYS_HE = mapOf(
    Calendar.SUNDAY    to "ראשון",
    Calendar.MONDAY    to "שני",
    Calendar.TUESDAY   to "שלישי",
    Calendar.WEDNESDAY to "רביעי",
    Calendar.THURSDAY  to "חמישי",
    Calendar.FRIDAY    to "שישי",
    Calendar.SATURDAY  to "שבת"
)
private val DAY_ORDER = listOf(
    Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
    Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
)

private fun formatTime(h: Int, m: Int) = "%02d:%02d".format(h, m)

// ─── Main screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var reminders by remember {
        mutableStateOf(ReminderManager.getReminders(context))
    }

    // Sheet state
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var isNewReminder by remember { mutableStateOf(false) }

    fun persist(updated: List<Reminder>) {
        reminders = updated
        ReminderManager.saveReminders(context, updated)
    }

    fun toggleEnable(reminder: Reminder) {
        val updated = reminders.map {
            if (it.id == reminder.id) {
                val toggled = it.copy(isEnabled = !it.isEnabled)
                if (toggled.isEnabled) ReminderManager.scheduleReminder(context, toggled)
                else ReminderManager.cancelReminder(context, it)
                toggled
            } else it
        }
        persist(updated)
    }

    fun deleteReminder(reminder: Reminder) {
        ReminderManager.cancelReminder(context, reminder)
        persist(reminders.filter { it.id != reminder.id })
    }

    fun saveReminder(reminder: Reminder) {
        val updated = if (reminders.any { it.id == reminder.id }) {
            // Cancel old alarms first, then schedule new ones
            reminders.find { it.id == reminder.id }?.let { ReminderManager.cancelReminder(context, it) }
            reminders.map { if (it.id == reminder.id) reminder else it }
        } else {
            reminders + reminder
        }
        persist(updated)
        if (reminder.isEnabled) ReminderManager.scheduleReminder(context, reminder)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "חזור",
                                tint = AccentIndigo
                            )
                        }
                        Text(
                            "תזכורות",
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingReminder = null
                        isNewReminder = true
                        showEditSheet = true
                    },
                    containerColor = AccentIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "הוסף תזכורת")
                }
            }
        ) { paddingValues ->
            if (reminders.isEmpty()) {
                EmptyRemindersState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggleEnable = { toggleEnable(reminder) },
                            onEdit = {
                                editingReminder = reminder
                                isNewReminder = false
                                showEditSheet = true
                            },
                            onDelete = { deleteReminder(reminder) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }
    }

    // ─── Edit / Add sheet ──────────────────────────────────────────────────
    if (showEditSheet) {
        ReminderEditSheet(
            initial = if (isNewReminder) null else editingReminder,
            onDismiss = { showEditSheet = false },
            onSave = { reminder ->
                saveReminder(reminder)
                showEditSheet = false
            }
        )
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyRemindersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = AccentIndigo.copy(alpha = 0.1f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = AccentIndigo,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "אין תזכורות",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "לחץ + כדי להוסיף תזכורת שבועית\nלעריכת סידור העבודה",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Reminder card ───────────────────────────────────────────────────────────

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onToggleEnable: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha = if (reminder.isEnabled) 1f else 0.5f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (reminder.isEnabled) AccentIndigo.copy(0.3f)
            else MaterialTheme.colorScheme.outline.copy(0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(alpha)
        ) {
            // ── Header row: day + enable switch ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentIndigo.copy(alpha = if (reminder.isEnabled) 0.15f else 0.06f)
                ) {
                    Text(
                        "יום ${DAYS_HE[reminder.dayOfWeek] ?: ""}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reminder.isEnabled) AccentIndigo
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Recurring badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (reminder.isRecurring) Orange.copy(0.12f) else Color.Transparent,
                    border = if (!reminder.isRecurring) BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(0.3f)
                    ) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (reminder.isRecurring) Icons.Default.Repeat else Icons.Default.LooksOne,
                            null,
                            tint = if (reminder.isRecurring) Orange
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            if (reminder.isRecurring) "חוזר" else "חד פעמי",
                            fontSize = 12.sp,
                            color = if (reminder.isRecurring) Orange
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggleEnable() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentIndigo)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Time chips ───────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(reminder.times) { time ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, AccentIndigo.copy(0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                null,
                                tint = AccentIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                formatTime(time.hour, time.minute),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Action row ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentIndigo.copy(0.5f))
                ) {
                    Icon(Icons.Default.Edit, null, tint = AccentIndigo, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ערוך", color = AccentIndigo, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BlockedRed.copy(0.4f))
                ) {
                    Icon(Icons.Default.Delete, null, tint = BlockedRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("מחק", color = BlockedRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Edit / Add sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderEditSheet(
    initial: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val isNew = initial == null

    var selectedDay by remember { mutableStateOf(initial?.dayOfWeek ?: Calendar.SUNDAY) }
    var times by remember { mutableStateOf(initial?.times ?: emptyList()) }
    var isRecurring by remember { mutableStateOf(initial?.isRecurring ?: true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = AccentIndigo.copy(alpha = 0.12f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            if (isNew) Icons.Default.NotificationAdd else Icons.Default.Edit,
                            null,
                            tint = AccentIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    if (isNew) "תזכורת חדשה" else "ערוך תזכורת",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Day selector ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "יום בשבוע",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true        // RTL: Sunday first visually on right
                ) {
                    items(DAY_ORDER) { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = {
                                Text(
                                    DAYS_HE[day] ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentIndigo,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ── Times ─────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "שעות התזכורת",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { showTimePicker = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = AccentIndigo, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("הוסף שעה", color = AccentIndigo, fontSize = 13.sp)
                    }
                }

                AnimatedVisibility(visible = showError && times.isEmpty()) {
                    Text(
                        "יש להוסיף לפחות שעה אחת",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                if (times.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "לא נוספו שעות",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(times.indices.toList()) { idx ->
                            val time = times[idx]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, AccentIndigo.copy(0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        formatTime(time.hour, time.minute),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { times = times.toMutableList().also { it.removeAt(idx) } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Recurring toggle ──────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        null,
                        tint = if (isRecurring) Orange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "תזכורת חוזרת",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (isRecurring) "מתרחשת כל שבוע" else "מתרחשת פעם אחת בלבד",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Orange
                        )
                    )
                }
            }

            // ── Info note ─────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AccentIndigo.copy(alpha = 0.07f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = AccentIndigo,
                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                    Text(
                        "התזכורת תישלח רק אם אין סידור עבודה שמור לשבוע הבא",
                        fontSize = 12.sp,
                        color = AccentIndigo,
                        lineHeight = 17.sp
                    )
                }
            }

            // ── Buttons ───────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("ביטול") }

                Button(
                    onClick = {
                        if (times.isEmpty()) {
                            showError = true
                        } else {
                            onSave(
                                Reminder(
                                    id = initial?.id ?: UUID.randomUUID().toString(),
                                    dayOfWeek = selectedDay,
                                    times = times,
                                    isRecurring = isRecurring,
                                    isEnabled = initial?.isEnabled ?: true
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                ) {
                    Text(
                        if (isNew) "הוסף" else "שמור",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ─── Time picker dialog ───────────────────────────────────────────────
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                val newTime = ReminderTime(hour, minute)
                if (!times.contains(newTime)) {
                    times = (times + newTime).sortedWith(compareBy({ it.hour }, { it.minute }))
                }
                showTimePicker = false
            }
        )
    }
}

// ─── Time picker dialog ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(is24Hour = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "בחר שעה",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectorColor = AccentIndigo,
                        timeSelectorSelectedContainerColor = AccentIndigo,
                        timeSelectorSelectedContentColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
            ) { Text("אישור", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ביטול") }
        }
    )
}
