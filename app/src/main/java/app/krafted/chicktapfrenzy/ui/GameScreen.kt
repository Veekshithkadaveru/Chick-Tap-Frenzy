package app.krafted.chicktapfrenzy.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.krafted.chicktapfrenzy.game.ChickGameView
import app.krafted.chicktapfrenzy.viewmodel.GameUiState

@SuppressLint("ClickableViewAccessibility")
@Composable
fun GameScreen(
    uiState: GameUiState,
    onTick: (Float) -> Unit,
    onHoleTapped: (Int) -> Unit,
    onEndGame: () -> Unit,
    onExitToHome: () -> Unit
) {
    val context = LocalContext.current
    val gameView = remember { ChickGameView(context) }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState }
            .collect { state ->
                gameView.updateSnapshot(
                    app.krafted.chicktapfrenzy.game.GameSessionSnapshot(
                        score = state.score,
                        lives = state.lives,
                        round = state.round,
                        roundTimeRemaining = state.roundTimeRemaining,
                        isGameOver = state.isGameOver,
                        isRoundComplete = state.isRoundComplete,
                        holes = state.holes,
                        scoreFloats = state.scoreFloats
                    )
                )
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            gameView.stopThread()
        }
    }

    AndroidView(
        factory = {
            gameView.apply {
                setOnTick { delta ->
                    onTick(delta)
                }
                setOnHoleTapped { holeIndex ->
                    onHoleTapped(holeIndex)
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
}
