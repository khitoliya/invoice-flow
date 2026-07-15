package com.dollyplastic.invoiceapp.ui.screens.applock



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText
import androidx.compose.runtime.getValue


@Composable
fun PinBoxes(
    pin: String,
    showPin: Boolean,
    active: Boolean = false,
    total: Int = 4
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.5f at 400
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(total) { index ->
            val isCurrent = active && index == pin.length
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        if (isCurrent) 2.dp else 1.dp,
                        if (isCurrent) AppColors.PrimaryBlue else Color(0xFFE5E7EB),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (index < pin.length) {
                    Text(
                        text = if (showPin) pin[index].toString() else "•",
                        style = AppText.Heading,
                        color = AppColors.Foreground
                    )
                } else if (isCurrent) {
                    Text(
                        text = "|",
                        style = AppText.Heading.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Light),
                        color = AppColors.PrimaryBlue.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
