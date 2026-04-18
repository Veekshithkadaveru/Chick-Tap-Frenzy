package app.krafted.chicktapfrenzy.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.krafted.chicktapfrenzy.game.ChickGameView
import app.krafted.chicktapfrenzy.game.GameSession
import app.krafted.chicktapfrenzy.game.GameSessionSnapshot
import app.krafted.chicktapfrenzy.viewmodel.GameUiState
import kotlinx.coroutines.delay

private val HudBackground = Color(0x99000000)
private val ScoreGold = Color(0xFFFFD700)
private val LifeFull = Color(0xFFFF6B6B)
private val LifeEmpty = Color(0x44FFFFFF)
private val RoundBadge = Color(0xFF4ECDC4)
private val TimerGreen = Color(0xFF2ECC71)
private val TimerYellow = Color(0xFFF39C12)
private val TimerRed = Color(0xFFE74C3C)
private val HudWhite = Color(0xFFFFFFFF)
private val HudGlow = Color(0x33FFFFFF)

@SuppressLint("ClickableViewAccessibility")
@Composable
fun GameScreen(
    uiState: GameUiState,
    onTick: (Float) -> Unit,
    onHoleTapped: (Int) -> Unit,
    onEndGame: () -> Unit,
    onStartNextRound: () -> Unit
) {
    BackHandler(enabled = !uiState.isGameOver) {
        onEndGame()
    }

    val context = LocalContext.current
    val gameView = remember { ChickGameView(context) }

    var gameOverTriggered by remember { mutableStateOf(false) }
    var isRoundTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        gameView.updateSnapshot(
            GameSessionSnapshot(
                score = uiState.score,
                lives = uiState.lives,
                round = uiState.round,
                backgroundIndex = uiState.backgroundIndex,
                roundTimeRemaining = uiState.roundTimeRemaining,
                isGameOver = uiState.isGameOver,
                isRoundComplete = uiState.isRoundComplete,
                holes = uiState.holes,
                scoreFloats = uiState.scoreFloats
            )
        )
        gameView.setBackgroundIndex(uiState.backgroundIndex)
    }

    LaunchedEffect(uiState.isRoundComplete) {
        if (uiState.isRoundComplete && !uiState.isGameOver) {
            isRoundTransitioning = true
            delay(2000)
            isRoundTransitioning = false
            onStartNextRound()
        }
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver && !gameOverTriggered) {
            gameOverTriggered = true
            delay(1200)
            onEndGame()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            gameView.stopThread()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                gameView.apply {
                    setOnTick { delta ->
                        onTick(delta)
                    }
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            val tappedHole = checkHoleTap(event.x, event.y)
                            if (tappedHole >= 0) {
                                onHoleTapped(tappedHole)
                            }
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        HudOverlay(
            score = uiState.score,
            lives = uiState.lives,
            round = uiState.round,
            roundTimeRemaining = uiState.roundTimeRemaining,
            isGameOver = uiState.isGameOver
        )

        AnimatedVisibility(
            visible = isRoundTransitioning,
            enter = fadeIn(tween(400)) + scaleIn(tween(400)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xBB000000))
                    .padding(32.dp)
            ) {
                Text(
                    text = "ROUND ${uiState.round} COMPLETE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SPEED UP!",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HudOverlay(
    score: Int,
    lives: Int,
    round: Int,
    roundTimeRemaining: Float,
    isGameOver: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            ScorePanel(score = score)
            RoundTimerPanel(round = round, timeRemaining = roundTimeRemaining)
            LivesPanel(lives = lives, maxLives = GameSession.STARTING_LIVES)
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isGameOver,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GAME OVER",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xCCE74C3C),
                                    Color(0xCCC0392B)
                                )
                            )
                        )
                        .padding(horizontal = 40.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ScorePanel(score: Int) {
    val animatedScore = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        )
    }

    LaunchedEffect(score) {
        if (score > 0) {
            pulseScale.snapTo(1.18f)
            pulseScale.animateTo(1f, tween(durationMillis = 260, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pulseScale.value
                scaleY = pulseScale.value
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC1A1200), Color(0xAA000000))
                )
            )
            .border(1.dp, ScoreGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCORE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = HudWhite.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp
        )
        Text(
            text = "${animatedScore.value.toInt()}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = ScoreGold
        )
    }
}

@Composable
private fun RoundTimerPanel(round: Int, timeRemaining: Float) {
    val timerFraction = (timeRemaining / GameSession.ROUND_DURATION_SEC).coerceIn(0f, 1f)
    val timerColor = when {
        timerFraction > 0.5f -> TimerGreen
        timerFraction > 0.2f -> TimerYellow
        else -> TimerRed
    }
    val isCritical = timeRemaining in 0.01f..5f

    val infinitePulse = rememberInfiniteTransition(label = "timerPulse")
    val criticalScale by infinitePulse.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerScale"
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = criticalScale
                scaleY = criticalScale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC001510), Color(0xAA000000))
                )
            )
            .border(
                1.dp,
                (if (isCritical) TimerRed else RoundBadge).copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROUND",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HudWhite.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$round",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = RoundBadge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(48.dp)) {
                val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)

                drawArc(
                    color = HudGlow,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    topLeft = Offset(stroke.width / 2, stroke.width / 2),
                    size = Size(
                        size.width - stroke.width,
                        size.height - stroke.width
                    )
                )

                drawArc(
                    color = timerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * timerFraction,
                    useCenter = false,
                    style = stroke,
                    topLeft = Offset(stroke.width / 2, stroke.width / 2),
                    size = Size(
                        size.width - stroke.width,
                        size.height - stroke.width
                    )
                )
            }

            Text(
                text = "${timeRemaining.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = timerColor
            )
        }
    }
}

@Composable
private fun LivesPanel(lives: Int, maxLives: Int) {
    val previousLives = remember { mutableStateOf(lives) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(lives) {
        if (lives < previousLives.value) {
            val sequence = listOf(9f, -9f, 7f, -7f, 4f, -4f, 0f)
            for (x in sequence) {
                shakeOffset.animateTo(x, tween(durationMillis = 55, easing = LinearEasing))
            }
        }
        previousLives.value = lives
    }

    Column(
        modifier = Modifier
            .offset(x = shakeOffset.value.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC1A0505), Color(0xAA000000))
                )
            )
            .border(1.dp, LifeFull.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LIVES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = HudWhite.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..maxLives) {
                EggLife(filled = i <= lives, justBroke = i == previousLives.value && lives < previousLives.value)
            }
        }
    }
}

@Composable
private fun EggLife(filled: Boolean, justBroke: Boolean = false) {
    val crackProgress = remember { Animatable(if (filled) 0f else 1f) }

    LaunchedEffect(filled, justBroke) {
        if (!filled && justBroke) {
            crackProgress.snapTo(0f)
            crackProgress.animateTo(1f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
        } else if (!filled) {
            crackProgress.snapTo(1f)
        } else {
            crackProgress.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.size(24.dp, 30.dp)) {
        val w = size.width
        val h = size.height
        val progress = crackProgress.value

        val shellColor = if (filled) LifeFull else LifeEmpty
        val pivotX = w / 2f
        val pivotY = h * 0.95f
        val tiltDeg = if (justBroke) progress * 14f else 0f

        rotate(degrees = tiltDeg, pivot = Offset(pivotX, pivotY)) {
            drawOval(
                color = shellColor,
                topLeft = Offset(w * 0.05f, 0f),
                size = Size(w * 0.9f, h)
            )

            if (filled) {
                drawOval(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(w * 0.25f, h * 0.12f),
                    size = Size(w * 0.35f, h * 0.25f)
                )
            }

            if (!filled && progress > 0.15f) {
                val crackAlpha = (progress * 1.2f).coerceAtMost(1f)
                val crackStroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                val cx = w * 0.5f
                val top = h * 0.2f
                val mid1 = Offset(cx - w * 0.15f, h * 0.35f)
                val mid2 = Offset(cx + w * 0.12f, h * 0.5f)
                val mid3 = Offset(cx - w * 0.1f, h * 0.65f)
                val bottom = Offset(cx + w * 0.08f, h * 0.8f)
                val points = listOf(Offset(cx, top), mid1, mid2, mid3, bottom)
                val crackColor = Color(0xFF2B0000).copy(alpha = crackAlpha)
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = crackColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = crackStroke.width,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
