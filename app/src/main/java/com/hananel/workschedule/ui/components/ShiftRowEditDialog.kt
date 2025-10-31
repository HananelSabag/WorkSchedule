package com.hananel.workschedule.ui.components

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.data.ShiftRow
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
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (shiftRow == null) Icons.Default.AccessTime else Icons.Default.Edit,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (shiftRow == null) "הוספת משמרת חדשה" else "עריכת משמרת",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Shift Name Input
                    OutlinedTextField(
                        value = shiftName,
                        onValueChange = { 
                            shiftName = it
                            showError = false
                        },
                        label = { Text("שם המשמרת") },
                        placeholder = { Text("למשל: בוקר, צהריים, לילה") },
                        singleLine = true,
                        isError = showError && shiftName.isBlank(),
                        supportingText = if (showError && shiftName.isBlank()) {
                            { Text("שדה חובה", color = Color.Red) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            focusedLabelColor = PrimaryTeal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    // Shift Hours Input
                    OutlinedTextField(
                        value = shiftHours,
                        onValueChange = { 
                            shiftHours = it
                            showError = false
                        },
                        label = { Text("שעות פעילות") },
                        placeholder = { Text("למשל: 06:45-15:00") },
                        singleLine = true,
                        isError = showError && shiftHours.isBlank(),
                        supportingText = if (showError && shiftHours.isBlank()) {
                            { Text("שדה חובה", color = Color.Red) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            focusedLabelColor = PrimaryTeal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    // Example hint
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0) // Light orange
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "דוגמאות:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• בוקר (06:45-15:00)\n" +
                                        "• צהריים (14:45-23:00)\n" +
                                        "• לילה (22:30-07:00)",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    // Preview
                    if (shiftName.isNotBlank() && shiftHours.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9) // Light green
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "תצוגה מקדימה:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "$shiftName ($shiftHours)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shiftName.isNotBlank() && shiftHours.isNotBlank()) {
                            onSave(shiftName.trim(), shiftHours.trim())
                            onDismiss()
                        } else {
                            showError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (shiftRow == null) "הוסף" else "שמור",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ביטול",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}


