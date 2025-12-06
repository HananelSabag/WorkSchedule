package com.hananel.workschedule.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.theme.PrimaryTeal
import com.hananel.workschedule.ui.theme.PrimaryGreen
import com.hananel.workschedule.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    
    // Logo animations
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )
    
    // Text animations with delays
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 300),
        label = "titleAlpha"
    )
    
    val titleOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(durationMillis = 600, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "titleOffset"
    )
    
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 600),
        label = "subtitleAlpha"
    )
    
    val creditAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 900),
        label = "creditAlpha"
    )
    
    // Floating orbs animation
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb1"
    )
    
    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb2"
    )
    
    // Pulse animation for logo glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Extended for beautiful animations
        onTimeout()
    }
    
    // RTL Layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A3A38), // Deep teal
                            Color(0xFF0D2D2A), // Darker teal
                            Color(0xFF0A1F1E)  // Almost black teal
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animated background orbs (decorative floating circles)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f)
            ) {
                // Orb 1 - Top right
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .offset(
                            x = (150 + kotlin.math.sin(Math.toRadians(orbOffset1.toDouble())).toFloat() * 30).dp,
                            y = (-50 + kotlin.math.cos(Math.toRadians(orbOffset1.toDouble())).toFloat() * 20).dp
                        )
                        .blur(60.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                // Orb 2 - Bottom left
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .offset(
                            x = (-100 + kotlin.math.cos(Math.toRadians(orbOffset2.toDouble())).toFloat() * 25).dp,
                            y = (500 + kotlin.math.sin(Math.toRadians(orbOffset2.toDouble())).toFloat() * 30).dp
                        )
                        .blur(50.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryGreen.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                // Orb 3 - Center accent
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                        .offset(y = (-100).dp)
                        .blur(80.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
            }
            
            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Logo with glow effect
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Glow behind logo
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .alpha(0.3f * logoAlpha)
                            .blur(30.dp)
                            .background(PrimaryTeal, CircleShape)
                    )
                    
                    // Logo container with gradient border
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(logoScale)
                            .alpha(logoAlpha)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryTeal,
                                        PrimaryGreen.copy(alpha = 0.8f),
                                        PrimaryTeal
                                    )
                                ),
                                CircleShape
                            )
                            .padding(3.dp)
                            .background(Color(0xFF0F2826), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Work Schedule Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // App title with shadow
                Text(
                    text = "סידור עבודה",
                    style = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        shadow = Shadow(
                            color = PrimaryTeal.copy(alpha = 0.6f),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(titleAlpha)
                        .graphicsLayer { translationY = titleOffset }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // English subtitle with gradient effect
                Text(
                    text = "Work Schedule Manager",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryTeal.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(subtitleAlpha)
                )
                
                Spacer(modifier = Modifier.height(80.dp))
                
                // Developer credit - elegant and minimal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(creditAlpha)
                ) {
                    // Decorative line
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PrimaryTeal.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                RoundedCornerShape(1.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Developed by",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Hananel Sabag",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
