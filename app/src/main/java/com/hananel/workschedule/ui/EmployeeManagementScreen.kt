package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun EmployeeManagementScreen(
    employees: List<Employee>,
    onAddEmployee: (String, Boolean, Boolean) -> Unit, // הוספת פרמטר למתגבר
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newEmployeeName by remember { mutableStateOf("") }
    var newEmployeeShabbatObserver by remember { mutableStateOf(false) }
    var newEmployeeIsMitgaber by remember { mutableStateOf(false) }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Add space from status bar
            
            // Title with Logo and Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "חזור",
                        tint = PrimaryTeal
                    )
                }
                
                Text(
                    text = "ניהול עובדים",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal, // Use logo color
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Add Employee Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GrayLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "הוסף עובד חדש",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = newEmployeeName,
                        onValueChange = { newEmployeeName = it },
                        label = { Text("שם עובד חדש") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    
                    // שומר שבת
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "שומר שבת",
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = newEmployeeShabbatObserver,
                            onCheckedChange = { newEmployeeShabbatObserver = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryGreen,
                                checkedTrackColor = PrimaryGreen.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    // מתגבר
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "מתגבר",
                                fontSize = 16.sp
                            )
                            Text(
                                text = "גמיש יותר בשיבוץ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = newEmployeeIsMitgaber,
                            onCheckedChange = { newEmployeeIsMitgaber = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Orange,
                                checkedTrackColor = Orange.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (newEmployeeName.trim().isNotEmpty()) {
                                onAddEmployee(newEmployeeName.trim(), newEmployeeShabbatObserver, newEmployeeIsMitgaber)
                                newEmployeeName = ""
                                newEmployeeShabbatObserver = false
                                newEmployeeIsMitgaber = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "הוסף עובד",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "הוסף עובד",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Info Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "הסבר:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• סמן \"שומר שבת\" לעובדים שלא יכולים לעבוד:\n" +
                                "  - שישי צהריים, שישי לילה\n" +
                                "  - שבת בוקר, שבת צהריים\n" +
                                "• סמן \"מתגבר\" לעובדים גמישים שיכולים למלא חורים\n" +
                                "• החסימות מתעדכנות אוטומטית בכל סידור חדש",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Employee List - כל העובדים ברשימה רגילה
            employees.forEach { employee ->
                EmployeeCard(
                    employee = employee,
                    onToggleShabbatObserver = { updatedEmployee ->
                        onUpdateEmployee(updatedEmployee)
                    },
                    onDelete = { onDeleteEmployee(employee) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Back Button
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = GrayMedium),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "חזור למסך הבית",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    onToggleShabbatObserver: (Employee) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow!
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Employee Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = employee.shabbatObserver,
                        onCheckedChange = { isChecked ->
                            onToggleShabbatObserver(employee.copy(shabbatObserver = isChecked))
                        }
                    )
                    Text(
                        text = "שומר שבת",
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Checkbox(
                        checked = employee.isMitgaber,
                        onCheckedChange = { isChecked ->
                            onToggleShabbatObserver(employee.copy(isMitgaber = isChecked))
                        }
                    )
                    Text(
                        text = "מתגבר",
                        fontSize = 14.sp,
                        color = if (employee.isMitgaber) Orange else Color.Unspecified
                    )
                }
                
                // Employee status text
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (employee.shabbatObserver) {
                        Text(
                            text = "חסום אוטומטית בשישי-שבת",
                            fontSize = 12.sp,
                            color = BlockedRed
                        )
                    }
                    if (employee.shabbatObserver && employee.isMitgaber) {
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    if (employee.isMitgaber) {
                        Text(
                            text = "גמיש בשיבוץ",
                            fontSize = 12.sp,
                            color = Orange
                        )
                    }
                }
            }
            
            // Delete Button
            IconButton(
                onClick = { showDeleteConfirm = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "מחק עובד",
                    tint = BlockedRed
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "מחק עובד",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "האם אתה בטוח שברצונך למחוק את ${employee.name}?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("מחק", color = BlockedRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text("ביטול")
                }
            }
        )
    }
}

