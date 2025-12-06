package com.hananel.workschedule.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.theme.PrimaryBlue
import com.hananel.workschedule.ui.theme.PrimaryGreen
import com.hananel.workschedule.ui.theme.PrimaryTeal

@Composable
fun HomeScreen(
    scheduleCount: Int = 5,
    onRecentSchedulesClick: () -> Unit,
    onNewScheduleClick: () -> Unit,
    onContinueTempDraftClick: () -> Unit,
    onEmployeeManagementClick: () -> Unit,
    onTemplateSetupClick: () -> Unit,
    onGoToTemplateSetup: () -> Unit = {},
    hasTempDraft: Boolean = false,
    hasTemplate: Boolean = true,
    employeeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    
    // Subtle breathing animation for decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "decor")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Background with subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                PrimaryTeal.copy(alpha = 0.03f)
                            )
                        )
                    )
            )
            
            // Decorative background orb
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = 200.dp, y = (-100).dp)
                    .scale(breatheScale)
                    .blur(100.dp)
                    .alpha(0.08f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryTeal, Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                // Header with Logo and App Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo with subtle glow
                    Box(contentAlignment = Alignment.Center) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(breatheScale)
                                .alpha(0.2f)
                                .blur(15.dp)
                                .background(PrimaryTeal, CircleShape)
                        )
                        
                        // Logo container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.2f),
                                            PrimaryGreen.copy(alpha = 0.1f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryTeal, PrimaryGreen.copy(alpha = 0.6f))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    // App Title
                    Text(
                        text = "סידור עבודה",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            shadow = Shadow(
                                color = PrimaryTeal.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subtitle
                Text(
                    text = "ניהול משמרות בקלות",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.weight(0.3f))
                
                // Warning Cards
                if (employeeCount == 0) {
                    WarningCard(
                        icon = Icons.Default.PersonAdd,
                        title = "ברוך הבא! 👋",
                        message = "להתחלה, הוסף את העובדים שלך",
                        buttonText = "הוסף עובדים",
                        buttonColor = PrimaryTeal,
                        onClick = onEmployeeManagementClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (employeeCount > 0 && !hasTemplate) {
                    WarningCard(
                        icon = Icons.Default.TableChart,
                        title = "צעד אחרון לפני שמתחילים!",
                        message = "הגדר את מבנה הטבלה - משמרות וימים",
                        buttonText = "הגדר טבלה",
                        buttonColor = PrimaryTeal,
                        onClick = onGoToTemplateSetup
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Main Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Draft Button (if exists)
                    if (hasTempDraft) {
                        PremiumButton(
                            text = "המשך טיוטה",
                            subtitle = "יש לך עבודה שלא הושלמה",
                            icon = Icons.Default.Restore,
                            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                            onClick = onContinueTempDraftClick
                        )
                    }
                    
                    // New Schedule Button
                    PremiumButton(
                        text = "סידור חדש",
                        subtitle = if (employeeCount == 0) "הוסף עובדים קודם" else if (!hasTemplate) "הגדר טבלה קודם" else "צור סידור עבודה חדש",
                        icon = Icons.Default.Add,
                        gradientColors = if (employeeCount > 0 && hasTemplate) 
                            listOf(PrimaryGreen, Color(0xFF2E7D32)) 
                        else 
                            listOf(Color.Gray, Color.DarkGray),
                        enabled = employeeCount > 0 && hasTemplate,
                        onClick = {
                            if (hasTempDraft) {
                                showDraftConfirmDialog = true
                            } else {
                                onNewScheduleClick()
                            }
                        }
                    )
                    
                    // History Button
                    PremiumButton(
                        text = "היסטוריה",
                        subtitle = "$scheduleCount סידורים שמורים",
                        icon = Icons.Default.History,
                        gradientColors = if (employeeCount > 0) 
                            listOf(PrimaryBlue, Color(0xFF1565C0)) 
                        else 
                            listOf(Color.Gray, Color.DarkGray),
                        enabled = employeeCount > 0,
                        onClick = onRecentSchedulesClick
                    )
                    
                    // Employee Management Button
                    PremiumButton(
                        text = "ניהול עובדים",
                        subtitle = if (employeeCount > 0) "$employeeCount עובדים במערכת" else "הוסף עובדים חדשים",
                        icon = Icons.Default.People,
                        gradientColors = listOf(Color(0xFF607D8B), Color(0xFF455A64)),
                        onClick = onEmployeeManagementClick
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Template Settings Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryTeal.copy(alpha = 0.06f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    onClick = onTemplateSetupClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        PrimaryTeal.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "הגדרות טבלה",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryTeal
                                )
                                Text(
                                    text = "עריכת משמרות וימים",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "עריכה",
                            tint = PrimaryTeal.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Developer Credit - Minimal and elegant
                Text(
                    text = "פותח ע\"י חננאל סבג",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    
    // Draft Confirmation Dialog
    if (showDraftConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDraftConfirmDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color(0xFFFF9800).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "נמצאה טיוטה",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "יש לך סידור שלא הושלם.\nמה תרצה לעשות?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNewScheduleClick()
                        showDraftConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("התחל מחדש", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onContinueTempDraftClick()
                        showDraftConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("המשך טיוטה", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun PremiumButton(
    text: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 4.dp else 0.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = if (enabled) gradientColors else listOf(Color.Gray, Color.DarkGray)
                    ),
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with circular background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningCard(
    icon: ImageVector,
    title: String,
    message: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(36.dp)
            )
            
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF795548),
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
