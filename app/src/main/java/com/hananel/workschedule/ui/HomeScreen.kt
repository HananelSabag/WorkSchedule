package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.theme.PrimaryBlue
import com.hananel.workschedule.ui.theme.PrimaryGreen
import com.hananel.workschedule.ui.theme.PrimaryTeal
import java.time.LocalDate

@Composable
fun HomeScreen(
    scheduleCount: Int = 5, // Will be connected to real data later
    onRecentSchedulesClick: () -> Unit,
    onNewScheduleClick: () -> Unit,
    onContinueTempDraftClick: () -> Unit, // New callback for temp draft
    onEmployeeManagementClick: () -> Unit,
    onTemplateSetupClick: () -> Unit, // New: template setup
    onGoToTemplateSetup: () -> Unit = {}, // Direct navigation to template setup
    hasTempDraft: Boolean = false, // New parameter
    hasTemplate: Boolean = true, // New: does a template exist?
    employeeCount: Int = 0, // New: number of employees in system
    modifier: Modifier = Modifier
) {
    // State for draft confirmation dialog
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title with Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "סידור עבודה",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal, // Use logo color
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                
                // Actual Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Empty State Warning - Show when no employees exist
            if (employeeCount == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD) // Light yellow background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = "ברוך הבא לאפליקציה!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "לפני שתוכל ליצור סידור עבודה,\nעליך להוסיף עובדים למערכת.",
                            fontSize = 16.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Text(
                            text = "👇 לחץ על 'ניהול עובדים' למטה",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Template Warning - Show when employees exist but no template
            if (employeeCount > 0 && !hasTemplate) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD) // Light blue background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = "צריך להגדיר טבלה!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "לפני יצירת סידור ראשון,\nעליך להגדיר את מבנה הטבלה:\nמשמרות וימים.",
                            fontSize = 16.sp,
                            color = PrimaryTeal,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Button(
                            onClick = onGoToTemplateSetup,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "הגדר טבלה עכשיו",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Main Buttons - Centered vertically
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeButton(
                    text = "סידורים אחרונים",
                    subtitle = "($scheduleCount)",
                    icon = Icons.Default.History,
                    backgroundColor = if (employeeCount == 0) Color.Gray else PrimaryBlue,
                    onClick = if (employeeCount == 0) { {} } else onRecentSchedulesClick,
                    enabled = employeeCount > 0
                )
                
                // Show temp draft button only if there's a temp draft
                if (hasTempDraft) {
                    HomeButton(
                        text = "המשך תהליך קיים",
                        subtitle = "טיוטה זמנית נמצאה",
                        icon = Icons.Default.Restore,
                        backgroundColor = Color(0xFFFF9800), // Orange color
                        onClick = onContinueTempDraftClick
                    )
                }
                
                HomeButton(
                    text = "יצירת סידור חדש",
                    subtitle = "",
                    icon = Icons.Default.Add,
                    backgroundColor = when {
                        employeeCount == 0 -> Color.Gray
                        !hasTemplate -> Color.Gray
                        else -> PrimaryGreen
                    },
                    onClick = if (employeeCount == 0 || !hasTemplate) { 
                        {}
                    } else {
                        {
                            // If there's a draft, show confirmation dialog
                            if (hasTempDraft) {
                                showDraftConfirmDialog = true
                            } else {
                                // No draft, proceed directly
                                onNewScheduleClick()
                            }
                        }
                    },
                    enabled = employeeCount > 0 && hasTemplate
                )
                
                HomeButton(
                    text = "ניהול עובדים",
                    subtitle = "",
                    icon = Icons.Default.People,
                    backgroundColor = Color.Gray,
                    onClick = onEmployeeManagementClick
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Template edit button - Visual card style
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryTeal.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Card(
                            modifier = Modifier.size(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryTeal.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = "עריכת מבנה טבלה",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryTeal
                            )
                            Text(
                                text = "התאמת משמרות וימים",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onTemplateSetupClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "עריכה",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Developer Credit with Copyright - professionally styled at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "פותח על ידי חננאל סבג (Hananel Sabag)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryTeal.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "v2.0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                
                Text(
                    text = "© ${LocalDate.now().year} כל הזכויות שמורות",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // Draft Confirmation Dialog
    if (showDraftConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDraftConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800) // Orange
                )
            },
            title = {
                Text(
                    text = "טיוטה קיימת",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "נמצאה טיוטה של סידור שלא הושלם.\n\nמה תרצה לעשות?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNewScheduleClick() // Start new (clear draft)
                        showDraftConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("התחל מחדש", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onContinueTempDraftClick() // Continue draft
                        showDraftConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("המשך טיוטה", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun HomeButton(
    text: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.Gray.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp) // No shadow!
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = if (enabled) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

