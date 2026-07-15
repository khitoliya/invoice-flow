package com.dollyplastic.invoiceapp.ui.screens.processing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class ProcessingStep(
    val title: String,
    val description: String,
    val status: StepStatus,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null
)

@Composable
fun ProcessingTimeline(
    steps: List<ProcessingStep>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        itemsIndexed(steps) { index, step ->
            TimelineItem(
                step = step,
                isLast = index == steps.lastIndex
            )
        }
    }
}

@Composable
fun TimelineItem(
    step: Step,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
         // Connector Column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = getStatusColor(step.status),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStatusIcon(step.status),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content Column
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .weight(1f)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (step.status == StepStatus.PENDING) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            
            if (step.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (step.status == StepStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            // Actions Row
            if ((step.actionLabel != null && step.onAction != null) || (step.secondaryActionLabel != null && step.onSecondaryAction != null)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (step.actionLabel != null && step.onAction != null) {
                        Button(
                            onClick = step.onAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (step.status == StepStatus.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(step.actionLabel)
                        }
                    }
                    
                    if (step.secondaryActionLabel != null && step.onSecondaryAction != null) {
                        OutlinedButton(
                            onClick = step.onSecondaryAction
                        ) {
                            Text(step.secondaryActionLabel)
                        }
                    }
                }
            }
        }
    }
}

// Fix for renamed parameter in separated file (fixing early)
typealias Step = ProcessingStep

@Composable
fun getStatusColor(status: StepStatus): Color {
    return when (status) {
        StepStatus.COMPLETED -> Color(0xFF4CAF50) // Green
        StepStatus.FAILED -> MaterialTheme.colorScheme.error
        StepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        StepStatus.PENDING -> Color.LightGray
        StepStatus.SKIPPED -> Color.Gray
    }
}

@Composable
fun getStatusIcon(status: StepStatus): ImageVector {
    return when (status) {
        StepStatus.COMPLETED -> Icons.Default.Check
        StepStatus.FAILED -> Icons.Default.Close
        StepStatus.IN_PROGRESS -> Icons.Default.Refresh // Or loading spinner logic
        StepStatus.PENDING -> Icons.Default.Check // Greyed out check
        StepStatus.SKIPPED -> Icons.Default.Check
    }
}
