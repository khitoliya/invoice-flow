package com.dollyplastic.invoiceapp.ui.common

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.softLayerShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    cornersRadius: Dp = 0.dp,
    shadowBlurRadius: Dp = 8.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp
) = drawBehind {
    val shadowColor = color.toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()
    
    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent
        frameworkPaint.setShadowLayer(
            shadowBlurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            cornersRadius.toPx(),
            cornersRadius.toPx(),
            paint
        )
    }
}
