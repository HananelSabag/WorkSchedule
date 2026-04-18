package com.hananel.workschedule.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.ui.theme.AccentIndigo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0) }

    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0.4f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha"
    )
    val textSlide by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 22f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "textSlide"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(400),
        label = "bottomAlpha"
    )

    val infinite = rememberInfiniteTransition(label = "splash")
    val linePulse by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "linePulse"
    )

    LaunchedEffect(Unit) {
        delay(50);  phase = 1
        delay(300); phase = 2
        delay(400); phase = 3
        delay(1250); onTimeout()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            // ── Center content ─────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                // Logo — shown as a clean app-icon card (rounded square, drop shadow)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = AccentIndigo.copy(alpha = 0.20f),
                            spotColor = AccentIndigo.copy(alpha = 0.30f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_in_app_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(44.dp))

                // Hebrew brand name — dark, bold
                Text(
                    text = "סידור עבודה",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .offset(y = textSlide.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Work Schedule Manager",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentIndigo.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 3.sp,
                    modifier = Modifier.alpha(textAlpha)
                )
            }

            // ── Bottom: pulsing teal line + version ────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .alpha(bottomAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(2.dp)
                        .alpha(linePulse)
                        .background(AccentIndigo, RoundedCornerShape(1.dp))
                )
                Text(
                    text = "v1.0",
                    fontSize = 11.sp,
                    color = AccentIndigo.copy(alpha = 0.35f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
