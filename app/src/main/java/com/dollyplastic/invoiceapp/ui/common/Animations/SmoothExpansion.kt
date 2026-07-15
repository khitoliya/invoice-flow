package com.dollyplastic.invoiceapp.ui.common.Animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun SmoothExpansion(
    visible: Boolean,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(duration, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ) + fadeIn(
            animationSpec = tween(duration)
        ),
        exit = shrinkVertically(
            animationSpec = tween(duration, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(
            animationSpec = tween(duration)
        )
    ) {
        content()
    }
}
