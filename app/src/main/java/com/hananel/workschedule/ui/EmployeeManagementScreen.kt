package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(
    employees: List<Employee>,
    onAddEmployee: (String, Boolean, Boolean) -> Unit,
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newEmployeeName by remember { mutableStateOf("") }
    var newEmployeeShabbatObserver by remember { mutableStateOf(false) }
    var newEmployeeIsMitgaber by remember { mutableStateOf(false) }

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "חזור", tint = AccentIndigo)
                        }
                        Text(
                            text = "ניהול עובדים",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        if (employees.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AccentIndigo.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "${employees.size}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentIndigo
                                )
                            }
                        } else {
                            Spacer(Modifier.size(48.dp))
                        }
                    }
                }
            },
            modifier = modifier
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                // ─── Add Employee Card ────────────────────────────────────────
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.PersonAdd, null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                                Text("הוסף עובד חדש", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AccentIndigo)
                            }

                            OutlinedTextField(
                                value = newEmployeeName,
                                onValueChange = { newEmployeeName = it },
                                label = { Text("שם עובד") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentIndigo,
                                    focusedLabelColor = AccentIndigo
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Shabbat toggle
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (newEmployeeShabbatObserver) BlockedRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (newEmployeeShabbatObserver) BlockedRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { newEmployeeShabbatObserver = !newEmployeeShabbatObserver }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("שומר שבת", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                            color = if (newEmployeeShabbatObserver) BlockedRed else MaterialTheme.colorScheme.onSurface)
                                        Switch(
                                            checked = newEmployeeShabbatObserver,
                                            onCheckedChange = { newEmployeeShabbatObserver = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = BlockedRed, checkedTrackColor = BlockedRed.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                                // Mitgaber toggle
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (newEmployeeIsMitgaber) Orange.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (newEmployeeIsMitgaber) Orange.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { newEmployeeIsMitgaber = !newEmployeeIsMitgaber }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("מתגבר", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                            color = if (newEmployeeIsMitgaber) Orange else MaterialTheme.colorScheme.onSurface)
                                        Switch(
                                            checked = newEmployeeIsMitgaber,
                                            onCheckedChange = { newEmployeeIsMitgaber = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Orange, checkedTrackColor = Orange.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (newEmployeeName.trim().isNotEmpty())
                                                listOf(AccentIndigo, AccentIndigoDark)
                                            else listOf(DisabledGray, DisabledGrayDark)
                                        )
                                    )
                                    .clickable(enabled = newEmployeeName.trim().isNotEmpty()) {
                                        onAddEmployee(newEmployeeName.trim(), newEmployeeShabbatObserver, newEmployeeIsMitgaber)
                                        newEmployeeName = ""
                                        newEmployeeShabbatObserver = false
                                        newEmployeeIsMitgaber = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text("הוסף עובד", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // ─── Compact info chip ────────────────────────────────────────
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentIndigo.copy(alpha = 0.07f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = AccentIndigo, modifier = Modifier.size(16.dp))
                            Text(
                                "שומר שבת: חסום שישי-שבת אוטומטית  •  מתגבר: גמיש בשיבוץ",
                                fontSize = 12.sp,
                                color = AccentIndigo,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ─── Employee List ────────────────────────────────────────────
                if (employees.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(modifier = Modifier.size(72.dp), shape = CircleShape, color = AccentIndigo.copy(alpha = 0.08f)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.People, null, tint = AccentIndigo.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                                }
                            }
                            Text("אין עובדים עדיין", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("הוסף עובדים מהטופס למעלה", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(employees, key = { it.id }) { employee ->
                        EmployeeCard(
                            employee = employee,
                            onToggleShabbatObserver = { onUpdateEmployee(it) },
                            onDelete = { onDeleteEmployee(employee) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeCard(
    employee: Employee,
    onToggleShabbatObserver: (Employee) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                employee.shabbatObserver -> BlockedRed.copy(alpha = 0.25f)
                employee.isMitgaber -> Orange.copy(alpha = 0.25f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = AccentIndigo.copy(alpha = 0.1f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = AccentIndigo, modifier = Modifier.size(24.dp))
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(employee.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (employee.shabbatObserver) {
                        Surface(color = BlockedRed.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                            Text("שומר שבת", fontSize = 11.sp, color = BlockedRed, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                    if (employee.isMitgaber) {
                        Surface(color = Orange.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                            Text("מתגבר", fontSize = 11.sp, color = Orange, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                    if (!employee.shabbatObserver && !employee.isMitgaber) {
                        Text("עובד רגיל", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Toggle buttons — labeled surfaces for clarity
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shabbat toggle
                Surface(
                    modifier = Modifier
                        .width(76.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onToggleShabbatObserver(employee.copy(shabbatObserver = !employee.shabbatObserver))
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (employee.shabbatObserver) BlockedRed.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (employee.shabbatObserver) BlockedRed.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Block, null,
                            tint = if (employee.shabbatObserver) BlockedRed
                                   else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "שבת", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = if (employee.shabbatObserver) BlockedRed
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Mitgaber toggle
                Surface(
                    modifier = Modifier
                        .width(76.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onToggleShabbatObserver(employee.copy(isMitgaber = !employee.isMitgaber))
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (employee.isMitgaber) Orange.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (employee.isMitgaber) Orange.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Star, null,
                            tint = if (employee.isMitgaber) Orange
                                   else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "גמיש", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = if (employee.isMitgaber) Orange
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delete
            IconButton(
                onClick = { showDeleteSheet = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Delete, "מחק", tint = BlockedRed.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }

    // ─── Delete Confirmation Sheet ────────────────────────────────────────────
    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
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
                    Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = BlockedRed.copy(alpha = 0.1f)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, null, tint = BlockedRed, modifier = Modifier.size(28.dp))
                        }
                    }
                    Text("מחק עובד?", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(
                        "האם למחוק את ${employee.name}?\nהפעולה אינה ניתנת לביטול.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteSheet = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("ביטול", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = { onDelete(); showDeleteSheet = false },
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
