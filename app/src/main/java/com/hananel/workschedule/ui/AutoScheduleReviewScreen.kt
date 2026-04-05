package com.hananel.workschedule.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.R
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.TemplateData
import com.hananel.workschedule.ui.components.SimpleScheduleTable
import com.hananel.workschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoScheduleReviewScreen(
    employees: List<Employee>,
    schedule: Map<String, List<String>>,
    blocks: Map<String, Boolean>,
    canOnlyBlocks: Map<String, Boolean>,
    savingMode: Map<String, Boolean>,
    weekStartDate: java.time.LocalDate,
    templateData: TemplateData?,
    impossibleShifts: List<String>,
    totalShifts: Int,
    onConfirm: () -> Unit,
    onBackToBlocking: () -> Unit,
    onEnterLandscape: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filledShifts = totalShifts - impossibleShifts.size
    val allGood = impossibleShifts.isEmpty()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ─── Header ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.12f)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToBlocking) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "חזור לחסימות",
                            tint = AccentIndigo
                        )
                    }

                    // Title
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "סקירת סידור אוטומטי",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentIndigo,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }

                    // Landscape + Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = onEnterLandscape,
                            shape = RoundedCornerShape(8.dp),
                            color = AccentIndigo.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ScreenRotation, null, tint = AccentIndigo, modifier = Modifier.size(13.dp))
                                Text("תצוגה אופקית", fontSize = 10.sp, color = AccentIndigo, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ─── Summary Card ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (allGood) AccentIndigo.copy(alpha = 0.4f) else Orange.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = (if (allGood) AccentIndigo else Orange).copy(alpha = 0.15f)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (allGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (allGood) AccentIndigo else Orange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (allGood) "הסידור הושלם בהצלחה!" else "הסידור נוצר עם חורים",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allGood) AccentIndigo else Orange
                            )
                        }

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip(
                                label = "מולאו",
                                value = "$filledShifts",
                                color = AccentIndigo,
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = "פתוחות",
                                value = "${impossibleShifts.size}",
                                color = if (impossibleShifts.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else BlockedRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = "סה״כ",
                                value = "$totalShifts",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Open shifts list (if any)
                        if (impossibleShifts.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "משמרות שלא ניתן למלא:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BlockedRed
                            )
                            impossibleShifts.forEach { key ->
                                val parts = key.split("-")
                                val day = parts.firstOrNull() ?: key
                                val shift = parts.drop(1).joinToString("-")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        null,
                                        tint = BlockedRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$day · $shift",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "ניתן לערוך ידנית לאחר השמירה",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // ─── Schedule Table (read-only preview) ─────────────────────────
            SimpleScheduleTable(
                employees = employees,
                selectedEmployee = null,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                schedule = schedule,
                templateData = templateData,
                isEditMode = false,
                weekStartDate = weekStartDate,
                onSetWeekStartDate = null,
                onCellClick = null,
                onLongPress = null,
                onDayHeaderClick = null,
                isBlockingMode = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // ─── Action Buttons ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToBlocking,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("חסימות", fontWeight = FontWeight.SemiBold)
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(AccentIndigo, AccentIndigoDark))
                        )
                        .clickable(
                            onClick = onConfirm,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(
                            "שמירה והצגה",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
