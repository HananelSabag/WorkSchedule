package com.hananel.workschedule.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hananel.workschedule.ui.theme.*

@Composable
fun SettingsScreen(
    employeeCount: Int = 0,
    appVersion: String = "2.1",
    onNavigateToEmployees: () -> Unit,
    onNavigateToTemplate: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val context = LocalContext.current

    // ── Notification permission state ────────────────────────────────────────
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }
    val hasExactAlarmPermission by remember {
        mutableStateOf(checkExactAlarmPermission(context))
    }

    // Launcher for POST_NOTIFICATIONS runtime request (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    // Re-check permission every time this composable becomes active
    // (user may have changed it in system settings)
    LaunchedEffect(Unit) {
        hasNotificationPermission = checkNotificationPermission(context)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text(
                text = "הגדרות",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            // ── Section: Schedule Setup ──────────────────────────────────────
            SettingsSectionHeader("הגדרת סידור")

            SettingsRow(
                icon = Icons.Default.TableChart,
                title = "מבנה הטבלה",
                subtitle = "משמרות, ימים וסדר שלהם",
                onClick = onNavigateToTemplate
            )

            SettingsRow(
                icon = Icons.Default.People,
                title = "ניהול עובדים",
                subtitle = if (employeeCount > 0) "$employeeCount עובדים רשומים" else "אין עובדים עדיין",
                onClick = onNavigateToEmployees
            )

            Spacer(Modifier.height(4.dp))

            // ── Section: Notifications ──────────────────────────────────────
            SettingsSectionHeader("התראות")

            // Permission status card
            NotificationPermissionCard(
                hasNotificationPermission = hasNotificationPermission,
                hasExactAlarmPermission = hasExactAlarmPermission,
                onRequestNotifPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenNotifSettings = {
                    // Direct to app notification settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onOpenAlarmSettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )

            SettingsRow(
                icon = Icons.Default.NotificationsNone,
                title = "תזכורות שבועיות",
                subtitle = "הגדר מתי לקבל תזכורת לסידור",
                onClick = onNavigateToReminders
            )

            Spacer(Modifier.height(4.dp))

            // ── Section: About ──────────────────────────────────────────────
            SettingsSectionHeader("אודות")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.15f))
            ) {
                Column {
                    SettingsStaticRow(
                        icon = Icons.Default.Info,
                        title = "גרסה",
                        value = "v$appVersion"
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsStaticRow(
                        icon = Icons.Default.Person,
                        title = "פותח ע\"י",
                        value = "חננאל סבג"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Notification permission card ─────────────────────────────────────────────

@Composable
private fun NotificationPermissionCard(
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onRequestNotifPermission: () -> Unit,
    onOpenNotifSettings: () -> Unit,
    onOpenAlarmSettings: () -> Unit
) {
    val allGranted = hasNotificationPermission && hasExactAlarmPermission

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (allGranted)
            Color(0xFF2E7D32).copy(alpha = 0.08f)
        else
            Orange.copy(alpha = 0.10f),
        border = BorderStroke(
            1.dp,
            if (allGranted) Color(0xFF2E7D32).copy(alpha = 0.3f)
            else Orange.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (allGranted) Color(0xFF2E7D32).copy(alpha = 0.15f) else Orange.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (allGranted) Color(0xFF2E7D32) else Orange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "הרשאת התראות",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (allGranted) "הכל מאושר — התראות פעילות" else "נדרשת פעולה",
                        fontSize = 12.sp,
                        color = if (allGranted) Color(0xFF2E7D32) else Orange
                    )
                }
            }

            // Individual permission rows
            PermissionStatusRow(
                label = "הצגת התראות",
                isGranted = hasNotificationPermission,
                onFix = if (!hasNotificationPermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        onRequestNotifPermission
                    else
                        onOpenNotifSettings
                } else null
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionStatusRow(
                    label = "התראות מדויקות בזמן",
                    isGranted = hasExactAlarmPermission,
                    onFix = if (!hasExactAlarmPermission) onOpenAlarmSettings else null
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    isGranted: Boolean,
    onFix: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF2E7D32) else Orange,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (!isGranted && onFix != null) {
            TextButton(
                onClick = onFix,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "אשר",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )
            }
        } else if (isGranted) {
            Text(
                text = "מאושר",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Permission helpers ────────────────────────────────────────────────────────

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Pre-Android 13: granted via manifest
    }
}

private fun checkExactAlarmPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    } else {
        true // Pre-Android 12: always granted
    }
}

// ── Reusable row composables ─────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AccentIndigo,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AccentIndigo.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsStaticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
