package com.kkdev.crackie.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkdev.crackie.R

@Composable
fun SplashScreen() {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floating_logo")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(alpha.value)
            .offset(y = offsetY.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cookie_bg),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Fortune",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(36.dp))
        ThreeDotsLoader()
    }
}

@Composable
fun ThreeDotsLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_loader")
    val dotSize = 12.dp
    val spacing = 8.dp

    @Composable
    fun Dot(delay: Int) {
        val yOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -15f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 800
                    -15f at 200
                    0f at 400
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delay)
            ),
            label = "dot_offset"
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = yOffset.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        Dot(0)
        Dot(150)
        Dot(300)
    }
}
