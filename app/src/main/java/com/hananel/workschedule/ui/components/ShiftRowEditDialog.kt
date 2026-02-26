package com.hananel.workschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.data.ShiftRow
import com.hananel.workschedule.ui.theme.PrimaryTeal
import com.hananel.workschedule.ui.theme.PrimaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftRowEditDialog(
    shiftRow: ShiftRow?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var shiftName by remember { mutableStateOf(shiftRow?.shiftName ?: "") }
    var shiftHours by remember { mutableStateOf(shiftRow?.shiftHours ?: "") }
    var showError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isNew = shiftRow == null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                        color = PrimaryTeal.copy(alpha = 0.12f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isNew) Icons.Default.AccessTime else Icons.Default.Edit,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isNew) "הוסף משמרת" else "ערוך משמרת",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Shift name
                OutlinedTextField(
                    value = shiftName,
                    onValueChange = { shiftName = it; showError = false },
                    label = { Text("שם המשמרת") },
                    placeholder = { Text("בוקר, צהריים, לילה...") },
                    singleLine = true,
                    isError = showError && shiftName.isBlank(),
                    supportingText = if (showError && shiftName.isBlank()) {
                        { Text("שדה חובה", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        focusedLabelColor = PrimaryTeal
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Shift hours
                OutlinedTextField(
                    value = shiftHours,
                    onValueChange = { shiftHours = it; showError = false },
                    label = { Text("שעות (HH:MM-HH:MM)") },
                    placeholder = { Text("06:45-15:00") },
                    singleLine = true,
                    isError = showError && shiftHours.isBlank(),
                    supportingText = if (showError && shiftHours.isBlank()) {
                        { Text("שדה חובה", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        focusedLabelColor = PrimaryTeal
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Live preview chip
                if (shiftName.isNotBlank() && shiftHours.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryTeal.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                            Text(
                                "$shiftName · $shiftHours",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryTeal
                            )
                        }
                    }
                }

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ביטול") }

                    Button(
                        onClick = {
                            if (shiftName.isNotBlank() && shiftHours.isNotBlank()) {
                                onSave(shiftName.trim(), shiftHours.trim())
                                onDismiss()
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text(
                            text = if (isNew) "הוסף" else "שמור",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
