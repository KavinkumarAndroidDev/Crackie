package com.kkdev.crackie.ui.home
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.kkdev.crackie.R
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AnimatedTiledBackground() {
    val vector = ImageVector.vectorResource(id = R.drawable.cookie_bg)
    val painter = rememberVectorPainter(image = vector)
    val tileSizePx = with(LocalDensity.current) { 180.dp.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "background_silhouette_transition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_silhouette_progress"
    )

    val patternColor = MaterialTheme.colorScheme.onBackground
    val baseAlpha = if (MaterialTheme.colorScheme.background.red > 0.5f) 0.06f else 0.04f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        val hexWidth = tileSizePx * sqrt(3f)
        val hexHeight = tileSizePx * 2f
        val tilesX = (size.width / hexWidth).toInt() + 2
        val tilesY = (size.height / (hexHeight * 0.75f)).toInt() + 2
        val totalTiles = tilesX * tilesY

        for (q in -tilesX..tilesX) {
            for (r in -tilesY..tilesY) {
                val x = hexWidth * (q + r / 2f) + centerX - hexWidth/2
                val y = (hexHeight * 0.75f) * r + centerY - hexHeight/2

                val posHash = (q * 13 + r * 31) % totalTiles
                val timeOffset = (posHash.toFloat() / totalTiles)

                val localProgress = (animationProgress + timeOffset) % 1.0f

                val fade = sin(localProgress * PI.toFloat())

                val dynamicAlpha = baseAlpha * fade

                val floatOffset = sin(localProgress * PI.toFloat() * 2) * 15f // Drifts 15px up and down

                if (x > -tileSizePx && x < size.width + tileSizePx && y > -tileSizePx && y < size.height + tileSizePx) {
                    translate(left = x, top = y + floatOffset) {
                        with(painter) {
                            draw(
                                size = Size(tileSizePx, tileSizePx),
                                alpha = dynamicAlpha.toFloat(),
                                colorFilter = ColorFilter.tint(patternColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
