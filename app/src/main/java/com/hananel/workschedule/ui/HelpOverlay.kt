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

// ─── Data model for help pages ──────────────────────────────────────────────

private data class HelpPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val steps: List<HelpStep>
)

private data class HelpStep(
    val icon: ImageVector,
    val text: String,
    val color: Color
)

private val helpPages = listOf(
    HelpPage(
        icon = Icons.Default.Settings,
        iconColor = PrimaryTeal,
        title = "הגדרה ראשונית",
        subtitle = "עשה זאת פעם אחת בלבד",
        steps = listOf(
            HelpStep(Icons.Default.PersonAdd, "הוסף עובדים בניהול עובדים", PrimaryTeal),
            HelpStep(Icons.Default.TableChart, "הגדר טבלת משמרות וימים", PrimaryTeal),
            HelpStep(Icons.Default.Check, "מוכן! אפשר ליצור סידורים", PrimaryGreen)
        )
    ),
    HelpPage(
        icon = Icons.Default.EditCalendar,
        iconColor = PrimaryBlue,
        title = "יצירת סידור חדש",
        subtitle = "תהליך מהתחלה ועד הסוף",
        steps = listOf(
            HelpStep(Icons.Default.Block, "חסום משמרות: לא יכול / יכול רק", BlockedRed),
            HelpStep(Icons.Default.AutoAwesome, "בחר: אוטומטי מהיר או ידני מלא", PrimaryGreen),
            HelpStep(Icons.Default.Share, "שתף בווצאפ או הורד לגלריה", Color(0xFF25D366))
        )
    ),
    HelpPage(
        icon = Icons.Default.History,
        iconColor = Orange,
        title = "היסטוריה וניהול",
        subtitle = "גישה מהירה לכל הסידורים",
        steps = listOf(
            HelpStep(Icons.Default.FolderOpen, "פתח סידור ישן לצפייה ועריכה", PrimaryTeal),
            HelpStep(Icons.Default.Edit, "ערוך תאים ישירות — נשמר מיד", Orange),
            HelpStep(Icons.Default.Restore, "טיוטה: ממשיך מהנקודה שעצרת", Color(0xFFFF9800))
        )
    )
)

// ─── Help Button (floating ? icon for HomeScreen) ────────────────────────────

@Composable
fun HelpButton(onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "help")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hp"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                Brush.linearGradient(listOf(PrimaryTeal.copy(alpha = 0.9f), PrimaryBlue.copy(alpha = 0.8f))),
                CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ─── Full Help BottomSheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HelpBottomSheet(
    onDismiss: () -> Unit
) {
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
                // Handle + close button row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, "סגור", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                    // Page indicator text
                    Text(
                        "${pagerState.currentPage + 1}/${helpPages.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    HelpPageContent(helpPages[page])
                }

                Spacer(Modifier.height(20.dp))

                // Dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(helpPages.size) { idx ->
                        val selected = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .width(if (selected) 24.dp else 8.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(if (selected) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant)
                                .clickable { scope.launch { pagerState.animateScrollToPage(idx) } }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Navigation row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("הקודם")
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    if (pagerState.currentPage < helpPages.size - 1) {
                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("הבא", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronLeft, null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("הבנתי!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpPageContent(page: HelpPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Big icon
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = page.iconColor.copy(alpha = 0.1f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    page.icon,
                    null,
                    tint = page.iconColor,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        // Title + subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                page.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                page.subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Steps
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            page.steps.forEachIndexed { idx, step ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = step.color.copy(alpha = 0.07f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Step number
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(step.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${idx + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Icon(step.icon, null, tint = step.color, modifier = Modifier.size(20.dp))
                        Text(
                            step.text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
