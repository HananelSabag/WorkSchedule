package com.hananel.workschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananel.workschedule.ui.theme.AccentIndigo
import com.hananel.workschedule.ui.theme.AccentIndigoDark

private val stepLabels = listOf("חסימות", "שיבוץ", "תצוגה")

@Composable
fun WorkflowStepBar(currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        stepLabels.forEachIndexed { index, label ->
            val stepNum = index + 1
            val isDone = stepNum < currentStep
            val isCurrent = stepNum == currentStep

            // Step dot + label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 28.dp else 22.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> AccentIndigo
                                isDone -> AccentIndigo.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDone) "✓" else "$stepNum",
                        fontSize = if (isCurrent) 13.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCurrent -> androidx.compose.ui.graphics.Color.White
                            isDone -> androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> AccentIndigo
                        isDone -> AccentIndigo.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }

            // Connector line between steps (not after the last step)
            if (index < stepLabels.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .padding(bottom = 14.dp) // align with dots, not labels
                        .height(2.dp)
                        .width(48.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (stepNum < currentStep) AccentIndigo.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}
