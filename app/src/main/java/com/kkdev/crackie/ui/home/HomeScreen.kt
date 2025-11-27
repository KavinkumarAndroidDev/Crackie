package com.kkdev.crackie.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkdev.crackie.R
import com.kkdev.crackie.db.Fortune
import com.kkdev.crackie.db.FortuneRarity
import com.kkdev.crackie.ui.theme.GoldenGlow
import com.kkdev.crackie.ui.theme.RichBrown
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToFavorites: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowIntro by viewModel.shouldShowIntro.collectAsState()

    val context = LocalContext.current
    val soundPlayer = remember { SoundPlayer(context) }
    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    if (shouldShowIntro) {
        IntroModal(onDismiss = { viewModel.dismissIntro() })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MoodBackground(uiState)

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToFavorites,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Rounded.Favorite, contentDescription = "View Favorites")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppHeader(uiState)

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> CircularProgressIndicator()
                        is HomeUiState.Cooldown -> CooldownView(viewModel = viewModel)
                        is HomeUiState.ReadyToCrack -> {
                            CrackableCookieWorkflow(
                                fortune = state.fortune,
                                soundPlayer = soundPlayer,
                                onCookieCracked = { viewModel.onCookieCracked(state.fortune) },
                                onCompletion = { viewModel.completeCycle() },
                                onToggleFavorite = { viewModel.toggleFavorite(state.fortune) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntroModal(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition("intro_glow")
                val glow by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        tween(2000, easing = FastOutSlowInEasing),
                        RepeatMode.Reverse
                    ), label = "intro_glow"
                )

                Image(
                    painter = painterResource(id = R.drawable.full_fortune),
                    contentDescription = "Fortune Cookie",
                    modifier = Modifier.size(120.dp).scale(glow)
                )
                Text(
                    text = "Welcome to Fortune!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "A new fortune cookie awaits you every 24 hours. Tap it five times to crack it open and reveal the wisdom inside.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Text(
                    text = "What will you find? A Common, Golden, or even a legendary Mystic fortune?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDismiss, 
                    shape = RoundedCornerShape(12.dp), 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Let's Crack It!", 
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun MoodBackground(uiState: HomeUiState) {
    val rarity = (uiState as? HomeUiState.ReadyToCrack)?.fortune?.rarity ?: FortuneRarity.COMMON

    val infiniteTransition = rememberInfiniteTransition("mood_bg_transition")

    val mysticColor1 by infiniteTransition.animateColor(Color(0xFF1D0F2A), Color(0xFF0F1A2A), infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), "mystic1")
    val mysticColor2 by infiniteTransition.animateColor(Color(0xFF0F1A2A), Color(0xFF1D0F2A), infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), "mystic2")

    val goldenColor1 by infiniteTransition.animateColor(Color(0xFF422C0E), Color(0xFF5A3D16), infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), "golden1")
    val goldenColor2 by infiniteTransition.animateColor(Color(0xFF5A3D16), Color(0xFF422C0E), infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), "golden2")

    val defaultBrush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface))
    val mysticBrush = Brush.verticalGradient(listOf(mysticColor1, mysticColor2))
    val goldenBrush = Brush.verticalGradient(listOf(goldenColor1, goldenColor2))

    Crossfade(targetState = rarity, animationSpec = tween(1500), label = "bg_rarity_crossfade") { targetRarity ->
        val brush = when (targetRarity) {
            FortuneRarity.MYSTIC -> mysticBrush
            FortuneRarity.GOLDEN -> goldenBrush
            else -> defaultBrush
        }
        Box(modifier = Modifier.fillMaxSize().background(brush)) {
            AnimatedTiledBackground()
        }
    }
}

@Composable
fun AppHeader(uiState: HomeUiState) {
    Box(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
        AnimatedVisibility(
            visible = uiState !is HomeUiState.Loading,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Text(
                text = "Your Daily Fortune",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

fun shareFortune(context: Context, fortuneText: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out my fortune from the Fortune app:\n\n\"$fortuneText\"")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

@Composable
fun CrackableCookieWorkflow(
    fortune: Fortune,
    soundPlayer: SoundPlayer,
    onCookieCracked: () -> Unit,
    onCompletion: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var tapCount by remember { mutableIntStateOf(0) }
    val maxTaps = 5
    var isCookieCracked by remember { mutableStateOf(false) }
    var showFortune by remember { mutableStateOf(false) }
    var triggerCrackAnimation by remember { mutableStateOf(false) }
    var showRarityAnnouncement by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val screenShake = remember { Animatable(0f) }

    LaunchedEffect(tapCount) {
        if (tapCount == maxTaps - 1) {
            context.vibrate(HapticType.SHORT_BUZZ)
            for (i in 1..2) {
                screenShake.animateTo(5f, tween(40)); screenShake.animateTo(-5f, tween(40))
            }
            screenShake.animateTo(0f, tween(40))
        }
    }

    LaunchedEffect(triggerCrackAnimation) {
        if (triggerCrackAnimation) {
            context.vibrate(HapticType.FINAL_CRACK)
            soundPlayer.playTapSound()
            delay(150)
            isCookieCracked = true
            onCookieCracked()
        }
    }

    LaunchedEffect(isCookieCracked) {
        if (isCookieCracked) {
            delay(500) // Wait for explosion to settle a bit
            if (fortune.rarity != FortuneRarity.COMMON) {
                showRarityAnnouncement = true
                context.vibrate(HapticType.RARITY_REVEAL)
                delay(1200) // Duration of the rarity announcement
                showRarityAnnouncement = false
            }
            showFortune = true
            context.vibrate(HapticType.PAPER_REVEAL)
            soundPlayer.playRevealSound()
        }
    }

    Box(
        modifier = Modifier.graphicsLayer(translationX = screenShake.value),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !showFortune && !showRarityAnnouncement,
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CookieView(
                    tapCount = tapCount,
                    isCracked = isCookieCracked,
                    triggerCrackAnimation = triggerCrackAnimation,
                    fortuneRarity = fortune.rarity,
                    onTap = {
                        if (tapCount < maxTaps) {
                            tapCount++
                            when (tapCount) {
                                1 -> context.vibrate(HapticType.TICK_1)
                                2 -> context.vibrate(HapticType.TICK_2)
                                3 -> context.vibrate(HapticType.DOUBLE_TICK)
                                5 -> triggerCrackAnimation = true
                            }
                            if (tapCount < 5) soundPlayer.playTapSound()
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = tapCount == 0 && !isCookieCracked,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Text(
                        text = "Tap the cookie to crack it!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        if (showRarityAnnouncement) {
            RarityAnnouncer(rarity = fortune.rarity)
        }

        AnimatedVisibility(
            visible = showFortune,
            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FortunePaper(fortune = fortune)
                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onToggleFavorite() }) {
                        Icon(
                            imageVector = if (fortune.isFavorite) Icons.Filled.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Save Fortune",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("Save or share this message", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.7f))
                    IconButton(onClick = { shareFortune(context, fortune.text) }) {
                        Icon(Icons.Rounded.Share, "Share Fortune", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onCompletion,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Crack another tomorrow", 
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RarityAnnouncer(rarity: FortuneRarity) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "rarity_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, delayMillis = 50, easing = FastOutLinearInEasing),
        label = "rarity_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    val (text, color) = when (rarity) {
        FortuneRarity.GOLDEN -> "Golden!" to GoldenGlow
        FortuneRarity.MYSTIC -> "Mystic!" to Color(0xFFD0BCFF)
        else -> "" to Color.Transparent
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(color = color.copy(alpha = 0.5f), blurRadius = 20f)
            ),
            color = color,
            modifier = Modifier.scale(scale).alpha(alpha)
        )
    }
}

@Composable
fun CooldownView(viewModel: HomeViewModel) {
    val remainingTime by viewModel.cooldownTimer.collectAsState()

    val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.cookie_sleeping),
            contentDescription = "Sleeping Cookie",
            modifier = Modifier.size(180.dp)
        )

        Text(
            text = "I’m baking your next cookie...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        ) {
            Text(
                text = timerText,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Text(
            text = "Come back tomorrow for a fresh cookie",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        FactCard()
    }
}

@Composable
fun CookieView(
    tapCount: Int,
    isCracked: Boolean,
    triggerCrackAnimation: Boolean,
    fortuneRarity: FortuneRarity,
    onTap: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(tapCount) {
        scale.stop(); rotation.stop()
        launch { scale.animateTo(1f, spring(0.3f, 300f)) }
        launch { rotation.animateTo(0f, spring(0.4f, 200f)) }

        if (tapCount in 1..4) {
            val tapIntensity = 1f - (tapCount * 0.05f)
            launch { scale.animateTo(tapIntensity, spring(stiffness = 800f)) }
            launch { rotation.animateTo((Math.random() * 10 - 5).toFloat(), spring(stiffness = 500f)) }
        }
    }

    LaunchedEffect(triggerCrackAnimation) {
        if (triggerCrackAnimation) {
            for (i in 1..3) {
                rotation.animateTo(5f, tween(30)); rotation.animateTo(-5f, tween(30))
            }
            rotation.animateTo(0f, tween(30))
        }
    }

    val infiniteTransition = rememberInfiniteTransition("idle")
    val idleSway by infiniteTransition.animateFloat(-2f, 2f, infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), "sway")
    val idleBob by infiniteTransition.animateFloat(-1f, 1f, infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), "bob")

    Box(
        modifier = Modifier
            .size(250.dp)
            .clickable(
                onClick = onTap,
                enabled = !isCracked && !triggerCrackAnimation,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .scale(scale.value)
            .rotate(rotation.value + if (tapCount == 0) idleSway else 0f)
            .offset(y = if (tapCount == 0) idleBob.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCracked) {
            ExplodingCookie()
        } else {
            val cookieToShow = when (tapCount) {
                0 -> R.drawable.full_fortune
                1 -> R.drawable.fortune2
                2 -> R.drawable.fortune3
                3 -> R.drawable.fortune4
                4 -> R.drawable.fortune5
                else -> R.drawable.fortune5
            }
            Image(
                painter = painterResource(id = cookieToShow),
                contentDescription = stringResource(R.string.fortune_cookie_desc),
                modifier = Modifier.size(220.dp) // The cookie image itself
            )
        }
    }
}

@Composable
fun FortunePaper(fortune: Fortune) {
    val scaleX = remember { Animatable(0f) }; val scaleY = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }; val rotation = remember { Animatable(-20f) }

    val glowAlpha = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                glowAlpha.animateTo(1f, tween(100))
                glowAlpha.animateTo(0f, tween(300, delayMillis = 100))
            }
            launch {
                glowScale.animateTo(1.5f, tween(400, easing = FastOutSlowInEasing))
            }

            delay(100)
            launch { scaleX.animateTo(1f, spring(0.7f, 100f)) }
            launch { scaleY.animateTo(1f, spring(0.7f, 100f)) }
            launch { alpha.animateTo(1f, tween(400)) }
            launch { rotation.animateTo(0f, spring(0.6f, 150f)) }
        }
    }

    val paperModifier = if (fortune.rarity == FortuneRarity.GOLDEN) {
        Modifier.drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldenGlow.copy(alpha = 0.3f), Color.Transparent),
                        radius = size.width
                    )
                )
            }
        }
    } else Modifier

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(glowScale.value)
                .alpha(glowAlpha.value)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scaleX.value, scaleY.value)
                .alpha(alpha.value)
                .rotate(rotation.value)
                .then(paperModifier)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fortune_paper),
                contentDescription = "Fortune Paper",
                modifier = Modifier.scale(1.2f)
            )
            Text(
                text = fortune.text,
                style = MaterialTheme.typography.headlineMedium,
                color = RichBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun InfiniteTransition.animateBrush(
    initialValue: Brush,
    targetValue: Brush,
    animationSpec: InfiniteRepeatableSpec<Float>,
    label: String
): State<Brush> {
    val progress = animateFloat(0f, 1f, animationSpec, label)
    return remember(this, label) {
        derivedStateOf {
            if (progress.value < 0.5) initialValue else targetValue
        }
    }
}

@Composable
fun ExplodingCookie() {
    val leftHalf = rememberPieceState(); val rightHalf = rememberPieceState()
    val smallCrumb1 = rememberPieceState(); val smallCrumb2 = rememberPieceState()
    val mediumCrumb1 = rememberPieceState(); val mediumCrumb2 = rememberPieceState()
    val largeCrumb = rememberPieceState()

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { animatePiece(leftHalf, -90f, 80f, -25f, 1000) }; launch { animatePiece(rightHalf, 90f, 80f, 25f, 1000) }
            launch { animatePiece(smallCrumb1, -80f, 250f, -90f, 1200, delay = 50) }; launch { animatePiece(smallCrumb2, 70f, 300f, 120f, 1200, delay = 100) }
            launch { animatePiece(mediumCrumb1, -50f, 350f, 70f, 1300, delay = 150) }; launch { animatePiece(mediumCrumb2, 60f, 280f, -100f, 1100, delay = 120) }
            launch { animatePiece(largeCrumb, 0f, 220f, 20f, 1400, delay = 80) }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Piece(R.drawable.left_half, "Left half", leftHalf)
        Piece(R.drawable.right_half, "Right half", rightHalf)
        Piece(R.drawable.crumbs_small, "Small crumb", smallCrumb1)
        Piece(R.drawable.crumbs_small, "Small crumb", smallCrumb2)
        Piece(R.drawable.crumbs_medium, "Medium crumb", mediumCrumb1)
        Piece(R.drawable.crumbs_medium, "Medium crumb", mediumCrumb2)
        Piece(R.drawable.crumbs_large, "Large crumb", largeCrumb)
    }
}

enum class HapticType {
    TICK_1, TICK_2, DOUBLE_TICK, SHORT_BUZZ, FINAL_CRACK, PAPER_REVEAL, RARITY_REVEAL
}

@RequiresPermission(Manifest.permission.VIBRATE)
fun Context.vibrate(type: HapticType) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = when (type) {
            HapticType.TICK_1 -> VibrationEffect.createOneShot(25, 80)
            HapticType.TICK_2 -> VibrationEffect.createOneShot(35, 120)
            HapticType.DOUBLE_TICK -> VibrationEffect.createWaveform(longArrayOf(0, 25, 45, 25), intArrayOf(0, 150, 0, 150), -1)
            HapticType.SHORT_BUZZ -> VibrationEffect.createOneShot(60, 220)
            HapticType.FINAL_CRACK -> VibrationEffect.createWaveform(longArrayOf(0, 80, 150, 30), intArrayOf(0, 255, 0, 120), -1)
            HapticType.PAPER_REVEAL -> VibrationEffect.createWaveform(longArrayOf(0, 25, 15, 45, 200, 25), intArrayOf(0, 100, 0, 130, 0, 80), -1)
            HapticType.RARITY_REVEAL -> VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 80), intArrayOf(0, 255, 0, 200), -1)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(70) // Increased fallback vibration
    }
}

data class PieceState(val x: Animatable<Float, *>, val y: Animatable<Float, *>, val rotation: Animatable<Float, *>, val alpha: Animatable<Float, *>)
@Composable
fun rememberPieceState(): PieceState = remember { PieceState(Animatable(0f), Animatable(0f), Animatable(0f), Animatable(1f)) }
@Composable
fun Piece(@DrawableRes id: Int, desc: String, state: PieceState) {
    Image(painter = painterResource(id = id), contentDescription = desc, modifier = Modifier
        .offset(x = state.x.value.dp, y = state.y.value.dp)
        .rotate(state.rotation.value)
        .alpha(state.alpha.value))
}
suspend fun animatePiece(state: PieceState, targetX: Float, targetY: Float, targetRotation: Float, duration: Int, delay: Long = 0) {
    delay(delay); coroutineScope {
        launch { state.x.animateTo(targetX, spring(0.5f, 150f)) }
        launch { state.y.animateTo(targetY, spring(0.7f, 100f)) }
        launch { state.rotation.animateTo(targetRotation, tween(duration)) }
        launch { delay(duration / 3L); state.alpha.animateTo(0f, tween(duration * 2 / 3)) }
    }
}

@Composable
fun FactCard() {
    val fact = remember { FortuneFacts.allFacts.random() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Did you know?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = fact,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}