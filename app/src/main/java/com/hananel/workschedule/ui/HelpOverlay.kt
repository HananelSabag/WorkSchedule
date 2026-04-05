package com.hananel.workschedule.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.ui.theme.*
import kotlinx.coroutines.launch

// ─── Data model ─────────────────────────────────────────────────────────────

private data class HelpPage(
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val title: String,
    val subtitle: String,
    val steps: List<HelpStep>,
    val tip: String
)

private data class HelpStep(
    val icon: ImageVector,
    val text: String,
    val detail: String,
    val color: Color
)

private val helpPages = listOf(

    // ── Page 1: Setup ────────────────────────────────────────────────────────
    HelpPage(
        icon = Icons.Default.Settings,
        gradientColors = listOf(AccentIndigo, Color(0xFF1A1F4E)),
        title = "הגדרה ראשונית",
        subtitle = "פעם אחת — ואתה מוכן לתמיד",
        steps = listOf(
            HelpStep(
                Icons.Default.PersonAdd,
                "הוסף עובדים",
                "שם + סוג חוזה (מלא / חלקי)",
                AccentIndigo
            ),
            HelpStep(
                Icons.Default.TableChart,
                "הגדר ימים פעילים",
                "בחר אילו ימים בשבוע יש משמרות",
                PrimaryBlue
            ),
            HelpStep(
                Icons.Default.Schedule,
                "הגדר משמרות ושעות",
                "שמות כמו 'בוקר / ערב / לילה' + שעות",
                Orange
            ),
            HelpStep(
                Icons.Default.Check,
                "שמור תבנית",
                "מוכן! אפשר ליצור סידורים כבר עכשיו",
                PrimaryGreen
            )
        ),
        tip = "ניתן לשנות את התבנית בכל עת — ללא השפעה על סידורים שמורים"
    ),

    // ── Page 2: Blocking ────────────────────────────────────────────────────
    HelpPage(
        icon = Icons.Default.Block,
        gradientColors = listOf(BlockedRed, BlockedRedDark),
        title = "חסימת משמרות",
        subtitle = "הגדר מי יכול / לא יכול",
        steps = listOf(
            HelpStep(
                Icons.Default.Person,
                "בחר עובד",
                "לחץ על שמו ברשימה התחתונה",
                AccentIndigo
            ),
            HelpStep(
                Icons.Default.Block,
                "חסום תא = 'לא יכול'",
                "לחץ שוב על אותו תא → 'יכול רק' (כחול)",
                BlockedRed
            ),
            HelpStep(
                Icons.Default.CalendarMonth,
                "חסום יום שלם",
                "לחץ על כותרת היום → כל המשמרות נחסמות",
                Orange
            ),
            HelpStep(
                Icons.Default.AutoAwesome,
                "בחר אוטומטי או ידני",
                "אוטומטי = מהיר | ידני = שליטה מלאה",
                PrimaryGreen
            )
        ),
        tip = "'יכול רק' עדיף על חסימה מלאה — מאפשר לאלגוריתם גמישות"
    ),

    // ── Page 3: Creation ────────────────────────────────────────────────────
    HelpPage(
        icon = Icons.Default.EditCalendar,
        gradientColors = listOf(PrimaryGreen, PrimaryGreenDark),
        title = "יצירת הסידור",
        subtitle = "שיבוץ חכם עם כמה לחיצות",
        steps = listOf(
            HelpStep(
                Icons.Default.TouchApp,
                "לחץ תא לשיבוץ",
                "בחר עובד → לחץ משמרת לשיבוצו",
                AccentIndigo
            ),
            HelpStep(
                Icons.Default.Edit,
                "לחיצה ארוכה = טקסט חופשי",
                "הוסף הערה לתא — 'חג / חולה / חצי יום'",
                Orange
            ),
            HelpStep(
                Icons.Default.ZoomIn,
                "גרור לזום על הטבלה",
                "צבוט פנימה/החוצה לשינוי גודל התצוגה",
                PrimaryBlue
            ),
            HelpStep(
                Icons.Default.Save,
                "שמור ← הסידור נשמר בהיסטוריה",
                "הסידור מתווסף אוטומטית, שתף מיד",
                PrimaryGreen
            )
        ),
        tip = "האלגוריתם מחשב חלוקה הוגנת של שעות בין כל העובדים"
    ),

    // ── Page 4: History & Tips ───────────────────────────────────────────────
    HelpPage(
        icon = Icons.Default.History,
        gradientColors = listOf(Orange, OrangeDark),
        title = "היסטוריה וטיפים",
        subtitle = "כל הסידורים — תמיד בהישג יד",
        steps = listOf(
            HelpStep(
                Icons.Default.FolderOpen,
                "לחיצה ארוכה → תפריט אפשרויות",
                "שנה שם | ערוך חסימות | מחק | העתק",
                AccentIndigo
            ),
            HelpStep(
                Icons.Default.Edit,
                "ערוך תאים ישירות בסידור",
                "שינויים נשמרים אוטומטית בזמן אמת",
                Orange
            ),
            HelpStep(
                Icons.Default.Share,
                "שתף לווצאפ / הורד לגלריה",
                "הסידור מיוצא כתמונה בצבעים מלאים",
                WhatsAppGreen
            ),
            HelpStep(
                Icons.Default.Restore,
                "טיוטה שמורה אוטומטית",
                "עצרת באמצע? הכל שמור — המשך מהנקודה שעצרת",
                PrimaryBlue
            )
        ),
        tip = "סידורים לא נמחקים בטעות — תמיד מוצג חלון אישור לפני מחיקה"
    )
)

// ─── Help Button ──────────────────────────────────────────────────────────────

@Composable
fun HelpButton(onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "help_pulse")
    val ringScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring"
    )
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing ring
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(ringScale)
                    .background(
                        AccentIndigo.copy(alpha = ringAlpha),
                        CircleShape
                    )
            )
            // Main button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(AccentIndigo, AccentIndigoDark)),
                        CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
        Text(
            "עזרה",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = AccentIndigo.copy(alpha = 0.8f)
        )
    }
}

// ─── Help BottomSheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HelpBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { helpPages.size })
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top bar ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Close, "סגור",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                    // Page counter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / ${helpPages.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentIndigo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // ── Pager ────────────────────────────────────────────────────
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    HelpPageContent(helpPages[page])
                }

                Spacer(Modifier.height(16.dp))

                // ── Segmented progress dots ──────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    helpPages.forEachIndexed { idx, page ->
                        val isSelected = pagerState.currentPage == idx
                        val color = if (isSelected) page.gradientColors.first()
                                    else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { scope.launch { pagerState.animateScrollToPage(idx) } }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ── Navigation ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, AccentIndigo.copy(0.4f)
                            )
                        ) {
                            Icon(
                                Icons.Default.ChevronRight, null,
                                modifier = Modifier.size(18.dp),
                                tint = AccentIndigo
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("הקודם", color = AccentIndigo, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    if (pagerState.currentPage < helpPages.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        helpPages[pagerState.currentPage].gradientColors
                                    )
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("הבא", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Icon(
                                    Icons.Default.ChevronLeft, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(AccentIndigo, AccentIndigoDark))
                                )
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "הבנתי, בואנו!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Help page content ────────────────────────────────────────────────────────

@Composable
private fun HelpPageContent(page: HelpPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Icon ─────────────────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                page.gradientColors.first().copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = page.gradientColors.first().copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp, page.gradientColors.first().copy(alpha = 0.35f)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        page.icon, null,
                        tint = page.gradientColors.first(),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // ── Title ─────────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                page.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = page.gradientColors.first(),
                textAlign = TextAlign.Center
            )
            Text(
                page.subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // ── Steps ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            page.steps.forEachIndexed { idx, step ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = step.color.copy(alpha = 0.07f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, step.color.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Step number circle
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(step.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${idx + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Icon
                        Icon(
                            step.icon, null,
                            tint = step.color,
                            modifier = Modifier.size(18.dp)
                        )
                        // Text block
                        Column(Modifier.weight(1f)) {
                            Text(
                                step.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                            Text(
                                step.detail,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Pro Tip ───────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = page.gradientColors.first().copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, page.gradientColors.first().copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb, null,
                    tint = page.gradientColors.first(),
                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                )
                Text(
                    page.tip,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
