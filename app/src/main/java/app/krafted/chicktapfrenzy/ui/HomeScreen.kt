package app.krafted.chicktapfrenzy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chicktapfrenzy.R
import app.krafted.chicktapfrenzy.ui.theme.ChickCream
import app.krafted.chicktapfrenzy.ui.theme.ChickCreamDim
import app.krafted.chicktapfrenzy.ui.theme.ChickInkShadow
import app.krafted.chicktapfrenzy.ui.theme.ChickRed
import app.krafted.chicktapfrenzy.ui.theme.ChickRedDark
import app.krafted.chicktapfrenzy.ui.theme.ChickRedLight
import app.krafted.chicktapfrenzy.ui.theme.ChickYellow
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowBright
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun HomeScreen(
    highScore: Int,
    onPlay: () -> Unit
) {
    val systemBarPadding: PaddingValues = WindowInsets.systemBars.asPaddingValues()


    val titleOffset = remember { Animatable(32f) }
    val titleAlpha = remember { Animatable(0f) }
    val buttonOffset = remember { Animatable(32f) }
    val buttonAlpha = remember { Animatable(0f) }
    val scoreAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(550)) }
        launch { titleOffset.animateTo(0f, tween(600, easing = EaseOutBack)) }
        launch {
            delay(180)
            buttonAlpha.animateTo(1f, tween(500))
        }
        launch {
            delay(180)
            buttonOffset.animateTo(0f, tween(560, easing = EaseOutBack))
        }
        launch {
            delay(360)
            scoreAlpha.animateTo(1f, tween(480))
        }
    }


    val idle = rememberInfiniteTransition(label = "home_idle")
    val float by idle.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "float"
    )
    val haloAlpha by idle.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )
    val titleShimmer by idle.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(3200, easing = LinearEasing, delayMillis = 1400),
            RepeatMode.Restart
        ),
        label = "title_shimmer"
    )
    val buttonShimmer by idle.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = LinearEasing, delayMillis = 900),
            RepeatMode.Restart
        ),
        label = "btn_shimmer"
    )
    val backgroundOffset by idle.animateFloat(
        initialValue = 0f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            tween(30000, easing = LinearEasing), RepeatMode.Reverse
        ),
        label = "bg_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.chickag6_back_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.3f
                    scaleY = 1.3f
                    translationX = backgroundOffset
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xAA000000),
                        0.45f to Color(0x33000000),
                        1f to Color(0xBB000000)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0f to Color.Transparent,
                        1f to Color(0x88000000)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(systemBarPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeroBadge(float = float, haloAlpha = haloAlpha)

            Spacer(modifier = Modifier.height(32.dp))

            TitleBlock(
                shimmerProgress = titleShimmer,
                offsetY = titleOffset.value,
                alpha = titleAlpha.value
            )

            Spacer(modifier = Modifier.height(40.dp))

            PlayButton(
                onPlay = onPlay,
                shimmerProgress = buttonShimmer,
                offsetY = buttonOffset.value,
                alpha = buttonAlpha.value
            )

            Spacer(modifier = Modifier.height(28.dp))

            BestScoreRow(
                highScore = highScore,
                alpha = scoreAlpha.value
            )
        }
    }
}

@Composable
private fun HeroBadge(float: Float, haloAlpha: Float) {
    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer { alpha = haloAlpha }
                .blur(40.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to ChickYellowSoft.copy(alpha = 0.75f),
                    0.6f to ChickYellow.copy(alpha = 0.3f),
                    1f to Color.Transparent
                )
            )
        }

        Box(
            modifier = Modifier
                .size(210.dp)
                .graphicsLayer { translationY = float }
                .shadow(
                    elevation = 22.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        0f to ChickCream,
                        1f to Color(0xFFF3E6C4)
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        0f to ChickYellowSoft,
                        1f to ChickYellow
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.chickag6_icon_512),
                contentDescription = "Chick Tap Frenzy",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(184.dp)
            )
        }
    }
}

@Composable
private fun TitleBlock(
    shimmerProgress: Float,
    offsetY: Float,
    alpha: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
            this.alpha = alpha
        }
    ) {
        Text(
            text = "CHICK  TAP",
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(ChickYellowSoft, ChickCream, ChickYellowSoft)
                ),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = 8.sp,
                shadow = Shadow(
                    color = ChickInkShadow,
                    offset = Offset(0f, 3f),
                    blurRadius = 8f
                ),
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "FRENZY",
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(ChickYellow, ChickYellowBright, ChickYellow)
                    ),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 58.sp,
                    letterSpacing = 4.sp,
                    shadow = Shadow(
                        color = ChickRedDark,
                        offset = Offset(0f, 6f),
                        blurRadius = 16f
                    ),
                    textAlign = TextAlign.Center
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        drawContent()
                        val sweep = shimmerProgress * size.width
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.35f to Color.White.copy(alpha = 0.0f),
                                0.5f to Color.White.copy(alpha = 0.22f),
                                0.65f to Color.White.copy(alpha = 0.0f),
                                1f to Color.Transparent,
                                startX = sweep - size.width * 0.4f,
                                endX = sweep + size.width * 0.4f
                            )
                        )
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap fast. Dodge the fox.",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = ChickCreamDim,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun PlayButton(
    onPlay: () -> Unit,
    shimmerProgress: Float,
    offsetY: Float,
    alpha: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
            this.alpha = alpha
        }
    ) {
        Box(
            modifier = Modifier
                .height(66.dp)
                .width(220.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = ChickRedDark,
                    spotColor = ChickRedDark
                )
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ChickRedDark, ChickRed, ChickRedLight)
                    )
                )
                .drawWithContent {
                    drawContent()
                    val sweep = shimmerProgress * size.width
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.35f to Color.White.copy(alpha = 0f),
                            0.5f to Color.White.copy(alpha = 0.28f),
                            0.65f to Color.White.copy(alpha = 0f),
                            1f to Color.Transparent,
                            startX = sweep - size.width * 0.35f,
                            endX = sweep + size.width * 0.35f
                        )
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onPlay() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PLAY",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = ChickCream,
                    letterSpacing = 10.sp
                )
            )
        }
    }
}

@Composable
private fun BestScoreRow(highScore: Int, alpha: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Divider(modifier = Modifier.width(48.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "BEST",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = ChickCreamDim,
                    letterSpacing = 4.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = highScore.toString().padStart(4, '0'),
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(ChickYellow, ChickYellowBright, ChickYellow)
                    ),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 4.sp,
                    shadow = Shadow(
                        color = ChickInkShadow,
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Divider(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        ChickCream.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                )
            )
    )
}
