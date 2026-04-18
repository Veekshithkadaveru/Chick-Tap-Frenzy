package app.krafted.chicktapfrenzy.viewmodel

import app.krafted.chicktapfrenzy.game.GameSession
import app.krafted.chicktapfrenzy.game.HoleSnapshot
import app.krafted.chicktapfrenzy.game.ScoreFloat

data class GameUiState(
    val score: Int = 0,
    val lives: Int = 3,
    val round: Int = 1,
    val backgroundIndex: Int = 0,
    val roundTimeRemaining: Float = 30f,
    val isGameOver: Boolean = false,
    val isRoundComplete: Boolean = false,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val currentCombo: Int = 0,
    val holes: List<HoleSnapshot> = List(GameSession.HOLE_COUNT) { holeIndex ->
        HoleSnapshot.empty(holeIndex)
    },
    val scoreFloats: List<ScoreFloat> = emptyList()
)
