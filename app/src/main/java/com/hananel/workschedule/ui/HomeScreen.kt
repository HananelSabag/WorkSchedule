package com.hananel.workschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    hasTempDraft: Boolean = false, // New parameter
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
            
            // Main Buttons - Centered vertically
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeButton(
                    text = "סידורים אחרונים",
                    subtitle = "($scheduleCount)",
                    icon = Icons.Default.History,
                    backgroundColor = PrimaryBlue,
                    onClick = onRecentSchedulesClick
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
                    backgroundColor = PrimaryGreen,
                    onClick = {
                        // If there's a draft, show confirmation dialog
                        if (hasTempDraft) {
                            showDraftConfirmDialog = true
                        } else {
                            // No draft, proceed directly
                            onNewScheduleClick()
                        }
                    }
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
            
            // Developer Credit with Copyright - professionally styled at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = "פותח על ידי חננאל סבג (Hananel Sabag)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
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
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

