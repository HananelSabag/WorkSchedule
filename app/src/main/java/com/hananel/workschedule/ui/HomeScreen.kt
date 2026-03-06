package com.hananel.workschedule.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
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
import com.hananel.workschedule.ui.theme.*

@Composable
fun HomeScreen(
    scheduleCount: Int = 0,
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
    var showHelp by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ps"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = modifier.fillMaxSize()) {

            // Single subtle ambient orb — clean, not distracting
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = 140.dp, y = (-100).dp)
                    .scale(pulseScale)
                    .blur(100.dp)
                    .alpha(0.09f)
                    .background(
                        Brush.radialGradient(listOf(PrimaryTeal, Color.Transparent)),
                        CircleShape
                    )
            )

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 48.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ─── Header ───────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HelpButton(onClick = { showHelp = true })

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Logo in teal circle — subtle, not bordered like a button
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .scale(pulseScale)
                                        .alpha(0.14f)
                                        .background(
                                            Brush.radialGradient(listOf(PrimaryTeal, Color.Transparent)),
                                            CircleShape
                                        )
                                )
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        Brush.linearGradient(listOf(PrimaryTeal, PrimaryTeal.copy(0.5f)))
                                    )
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_app_logo_new),
                                            contentDescription = null,
                                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "סידור עבודה",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryTeal
                                )
                                Text(
                                    "ניהול משמרות חכם",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.width(52.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    // ─── Setup / Draft banners ────────────────────────────────
                    if (employeeCount == 0 || !hasTemplate) {
                        SetupBanner(
                            needsEmployees = employeeCount == 0,
                            needsTemplate = employeeCount > 0 && !hasTemplate,
                            onAddEmployees = onEmployeeManagementClick,
                            onSetupTemplate = onGoToTemplateSetup
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                    if (hasTempDraft) {
                        DraftBanner(onClick = onContinueTempDraftClick)
                        Spacer(Modifier.height(14.dp))
                    }

                    // ─── Card grid — visual hierarchy ─────────────────────────
                    val canCreate = employeeCount > 0 && hasTemplate
                    val canHistory = employeeCount > 0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ① Hero: New Schedule — full width, primary action
                        MainActionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(162.dp),
                            icon = Icons.Default.Add,
                            label = "סידור חדש",
                            description = "צור סידור שבועי חדש",
                            gradient = if (canCreate)
                                listOf(PrimaryTeal, PrimaryTealDark)
                            else listOf(Color(0xFF9E9E9E), Color(0xFF616161)),
                            enabled = canCreate,
                            badge = if (hasTempDraft) "!" else null,
                            onClick = {
                                if (hasTempDraft) showDraftConfirmDialog = true
                                else onNewScheduleClick()
                            }
                        )

                        // ② History + Employees — side by side, different colors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(134.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MainActionCard(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                icon = Icons.Default.History,
                                label = "היסטוריה",
                                description = "סידורים שמורים",
                                gradient = if (canHistory)
                                    listOf(CardBlue, CardBlueDark)
                                else listOf(Color(0xFF9E9E9E), Color(0xFF616161)),
                                enabled = canHistory,
                                badge = if (scheduleCount > 0) scheduleCount.toString() else null,
                                onClick = onRecentSchedulesClick
                            )
                            MainActionCard(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                icon = Icons.Default.People,
                                label = "עובדים",
                                description = "הוסף ונהל עובדים",
                                gradient = listOf(CardEmerald, CardEmeraldDark),
                                badge = if (employeeCount > 0) employeeCount.toString() else null,
                                onClick = onEmployeeManagementClick
                            )
                        }

                        // ③ Template Settings — full width, compact row layout
                        MainActionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            icon = Icons.Default.TableChart,
                            label = "הגדרות טבלה",
                            description = "משמרות וימים",
                            gradient = listOf(CardViolet, CardVioletDark),
                            badge = if (!hasTemplate) "!" else null,
                            compact = true,
                            onClick = onTemplateSetupClick
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // ─── Footer ────────────────────────────────────────────────
                    Text(
                        "פותח ע\"י חננאל סבג",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // ─── Draft confirmation (intentionally AlertDialog) ────────────────────
    if (showDraftConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDraftConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Restore, null, tint = Orange, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("יש טיוטה פתוחה", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            },
            text = {
                Text("מה תרצה לעשות?", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onNewScheduleClick(); showDraftConfirmDialog = false },
                        border = BorderStroke(1.dp, BlockedRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("התחל מחדש", color = BlockedRed, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onContinueTempDraftClick(); showDraftConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("המשך טיוטה", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {},
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showHelp) {
        HelpBottomSheet(onDismiss = { showHelp = false })
    }
}

// ─── MainActionCard ─────────────────────────────────────────────────────────

@Composable
private fun MainActionCard(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badge: String? = null,
    description: String? = null,
    compact: Boolean = false          // true = horizontal layout (for flat bottom card)
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(gradient))
                .then(
                    if (enabled) Modifier.clickable(interactionSource, null, onClick = onClick)
                    else Modifier.alpha(0.48f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Subtle top-shine highlight on all cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                        )
                    )
            )

            if (compact) {
                // ── Horizontal layout (template settings) ──────────────────
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (description != null) {
                            Text(
                                description,
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronLeft,
                        null,
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // ── Vertical layout (hero + secondary cards) ────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    if (description != null) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = description,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Badge (count or "!" warning)
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(22.dp)
                    .background(
                        if (badge == "!") BlockedRed else Color.White.copy(alpha = 0.90f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge == "!") Color.White else gradient.first()
                )
            }
        }
    }
}

// ─── DraftBanner ─────────────────────────────────────────────────────────────

@Composable
private fun DraftBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(listOf(Orange, Color(0xFFFF5722)))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Restore, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text("המשך טיוטה", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("יש לך עבודה שלא הושלמה", color = Color.White.copy(0.85f), fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronLeft, null, tint = Color.White.copy(0.8f))
    }
}

// ─── SetupBanner ─────────────────────────────────────────────────────────────

@Composable
private fun SetupBanner(
    needsEmployees: Boolean,
    needsTemplate: Boolean,
    onAddEmployees: () -> Unit,
    onSetupTemplate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                Text("נדרשת הגדרה", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryTeal)
            }
            if (needsEmployees) SetupChip("הוסף עובדים", Icons.Default.PersonAdd, onAddEmployees)
            if (needsTemplate) SetupChip("הגדר מבנה טבלה", Icons.Default.TableChart, onSetupTemplate)
        }
    }
}

@Composable
private fun SetupChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PrimaryTeal.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PrimaryTeal, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronLeft, null, tint = PrimaryTeal.copy(0.6f), modifier = Modifier.size(16.dp))
    }
}
