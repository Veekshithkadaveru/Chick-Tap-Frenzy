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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chicktapfrenzy.R
import app.krafted.chicktapfrenzy.ui.theme.ChickCream
import app.krafted.chicktapfrenzy.ui.theme.ChickCreamDim
import app.krafted.chicktapfrenzy.ui.theme.ChickInk
import app.krafted.chicktapfrenzy.ui.theme.ChickInkShadow
import app.krafted.chicktapfrenzy.ui.theme.ChickRed
import app.krafted.chicktapfrenzy.ui.theme.ChickRedDark
import app.krafted.chicktapfrenzy.ui.theme.ChickRedLight
import app.krafted.chicktapfrenzy.ui.theme.ChickYellow
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowBright
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowSoft
import app.krafted.chicktapfrenzy.viewmodel.GameUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun GameOverScreen(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    val systemBarPadding: PaddingValues = WindowInsets.systemBars.asPaddingValues()

    val titleOffset = remember { Animatable(32f) }
    val titleAlpha = remember { Animatable(0f) }
    val scoreOffset = remember { Animatable(32f) }
    val scoreAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(550)) }
        launch { titleOffset.animateTo(0f, tween(600, easing = EaseOutBack)) }
        launch {
            delay(150)
            scoreAlpha.animateTo(1f, tween(500))
        }
        launch {
            delay(150)
            scoreOffset.animateTo(0f, tween(560, easing = EaseOutBack))
        }
        launch {
            delay(400)
            buttonAlpha.animateTo(1f, tween(600))
        }
    }

    val idle = rememberInfiniteTransition(label = "game_over_idle")
    val shimmer by idle.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = LinearEasing, delayMillis = 1000),
            RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.chickag6_back_5),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xCC000000),
                        0.5f to Color(0x66000000),
                        1f to Color(0xEE000000)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = titleOffset.value
                    alpha = titleAlpha.value
                }
            ) {
                Text(
                    text = "THE  PARTY  IS",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ChickYellowSoft, ChickCream, ChickYellowSoft)
                        ),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 6.sp,
                        shadow = Shadow(
                            color = ChickInkShadow,
                            offset = Offset(0f, 3f),
                            blurRadius = 8f
                        ),
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "OVER",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ChickYellow, ChickYellowBright, ChickYellow)
                        ),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 72.sp,
                        letterSpacing = 4.sp,
                        shadow = Shadow(
                            color = ChickRedDark,
                            offset = Offset(0f, 6f),
                            blurRadius = 18f
                        ),
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            ScoreCard(
                score = uiState.score,
                highScore = uiState.highScore,
                isNewHighScore = uiState.isNewHighScore,
                offsetY = scoreOffset.value,
                alpha = scoreAlpha.value
            )

            Spacer(modifier = Modifier.height(56.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = buttonAlpha.value }
            ) {
                GameOverButton(
                    text = "PLAY AGAIN",
                    primary = true,
                    shimmerProgress = shimmer,
                    onClick = onPlayAgain
                )

                Spacer(modifier = Modifier.height(20.dp))

                GameOverButton(
                    text = "HOME",
                    primary = false,
                    onClick = onBackToHome
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    offsetY: Float,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
            }
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    0f to ChickInk.copy(alpha = 0.92f),
                    1f to Color(0xFF2D1A08).copy(alpha = 0.96f)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "YOUR SCORE",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = ChickCreamDim,
                    letterSpacing = 4.sp
                )
            )
            
            Text(
                text = score.toString(),
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(ChickYellowBright, ChickYellow)
                    ),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 64.sp,
                    shadow = Shadow(
                        color = ChickInkShadow,
                        offset = Offset(0f, 4f),
                        blurRadius = 10f
                    )
                )
            )

            if (isNewHighScore) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(ChickYellow, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW BEST!",
                        style = TextStyle(
                            color = ChickRedDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Divider(modifier = Modifier.fillMaxWidth(0.6f))
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BEST:",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ChickCreamDim,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = highScore.toString(),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ChickCream
                    )
                )
            }
        }
    }
}

@Composable
private fun GameOverButton(
    text: String,
    primary: Boolean,
    shimmerProgress: Float = 0f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(0.8f)
            .shadow(
                elevation = if (primary) 12.dp else 4.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = if (primary) ChickRedDark else Color.Black
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (primary) {
                    Brush.horizontalGradient(listOf(ChickRedDark, ChickRed, ChickRedLight))
                } else {
                    Brush.horizontalGradient(listOf(ChickInk, Color(0xFF2D1A08)))
                }
            )
            .border(
                width = 1.dp,
                color = if (primary) Color.Transparent else ChickCream.copy(alpha = 0.25f),
                shape = RoundedCornerShape(32.dp)
            )
            .then(
                if (primary) {
                    Modifier.drawWithContent {
                        drawContent()
                        val sweep = shimmerProgress * size.width
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.45f to Color.White.copy(alpha = 0.25f),
                                0.55f to Color.White.copy(alpha = 0.25f),
                                1f to Color.Transparent,
                                startX = sweep - size.width * 0.3f,
                                endX = sweep + size.width * 0.3f
                            )
                        )
                    }
                } else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (primary) ChickCream else ChickCreamDim,
                letterSpacing = 6.sp
            )
        )
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
                        ChickCream.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Preview(showBackground = true)
@Composable
fun GameOverScreenPreview() {
    GameOverScreen(
        uiState = GameUiState(score = 1250, highScore = 2400, isNewHighScore = false),
        onPlayAgain = {},
        onBackToHome = {}
    )
}
