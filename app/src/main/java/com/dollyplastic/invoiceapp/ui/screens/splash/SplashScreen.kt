package com.dollyplastic.invoiceapp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.alpha


@Composable
fun SplashScreen() {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(-20f) } // Start slightly higher

    LaunchedEffect(Unit) {
        val animationSpec = tween<Float>(durationMillis = 800, easing = FastOutSlowInEasing)
        
        launch { alpha.animateTo(1f, animationSpec) }
        launch { offsetY.animateTo(0f, animationSpec) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // 1. ABSOLUTELY CENTERED LOGO
        // Matches the position of the native Android splash screen icon
        // SIZE: 286dp (User customized size)
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(286.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 2. TEXT & LOADING INDICATOR
        // Positioned below the center, independent of the Logo's position
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 130.dp + offsetY.value.dp) // Push down below icon + animation offset
                .alpha(alpha.value)
        ) {
            // App name
            Text(
                text = "InvoiceFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smart Billing Made Simple",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(24.dp))

            // Loading dots
            LoadingDots(
                color = Color(0xFF4A74B8)
            )
        }
    }
}


@Composable
fun LoadingDots(
    color: Color
) {
    val transitions = List(3) { rememberInfiniteTransition() }

    val offsets = transitions.mapIndexed { index, transition ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    delayMillis = index * 120,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        offsets.forEach { offset ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = offset.value.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


