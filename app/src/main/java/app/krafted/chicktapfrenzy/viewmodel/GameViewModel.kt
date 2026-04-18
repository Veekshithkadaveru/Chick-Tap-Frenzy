package app.krafted.chicktapfrenzy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.chicktapfrenzy.data.db.AppDatabase
import app.krafted.chicktapfrenzy.data.db.ScoreEntity
import app.krafted.chicktapfrenzy.game.GameSession
import app.krafted.chicktapfrenzy.game.GameSessionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).scoreDao()
    private val gameSession = GameSession()
    private val sessionLock = Any()
    private var hasPersistedCurrentGame = false

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeHighScore().collect { max ->
                _uiState.update { it.copy(highScore = max ?: 0) }
            }
        }

        syncSessionState(gameSession.snapshot())
    }

    fun tickGame(deltaSeconds: Float) {
        val snapshot = synchronized(sessionLock) { gameSession.tick(deltaSeconds) }
        syncSessionState(snapshot)
        persistGameOverIfNeeded(snapshot)
    }

    fun onHoleTapped(holeIndex: Int) {
        val snapshot = synchronized(sessionLock) { gameSession.onHoleTapped(holeIndex) }
        syncSessionState(snapshot)
        persistGameOverIfNeeded(snapshot)
    }

    fun onCharacterMissed(holeIndex: Int) {
        val snapshot = synchronized(sessionLock) { gameSession.onCharacterMissed(holeIndex) }
        syncSessionState(snapshot)
        persistGameOverIfNeeded(snapshot)
    }

    fun startGame() {
        hasPersistedCurrentGame = false
        val snapshot = synchronized(sessionLock) { gameSession.reset() }
        _uiState.update { current ->
            current.copy(
                score = snapshot.score,
                lives = snapshot.lives,
                round = snapshot.round,
                backgroundIndex = snapshot.backgroundIndex,
                roundTimeRemaining = snapshot.roundTimeRemaining,
                isGameOver = false,
                isRoundComplete = snapshot.isRoundComplete,
                isNewHighScore = false,
                holes = snapshot.holes,
                scoreFloats = snapshot.scoreFloats
            )
        }
    }

    fun startNextRound() {
        val snapshot = synchronized(sessionLock) { gameSession.startNextRound() }
        syncSessionState(snapshot)
    }

    fun endGame() {
        val snapshot = synchronized(sessionLock) { gameSession.endGame() }
        syncSessionState(snapshot)
        persistGameOverIfNeeded(snapshot)
    }

    private fun syncSessionState(snapshot: GameSessionSnapshot) {
        _uiState.update { current ->
            current.copy(
                score = snapshot.score,
                lives = snapshot.lives,
                round = snapshot.round,
                backgroundIndex = snapshot.backgroundIndex,
                roundTimeRemaining = snapshot.roundTimeRemaining,
                isGameOver = current.isGameOver || snapshot.isGameOver,
                isRoundComplete = snapshot.isRoundComplete,
                holes = snapshot.holes,
                scoreFloats = snapshot.scoreFloats
            )
        }
    }

    private fun persistGameOverIfNeeded(snapshot: GameSessionSnapshot) {
        if (!snapshot.isGameOver || hasPersistedCurrentGame) {
            return
        }

        hasPersistedCurrentGame = true
        val finalScore = snapshot.score
        val currentHighScore = uiState.value.highScore

        _uiState.update { it.copy(isGameOver = true) }
        viewModelScope.launch {
            val previousHighScore = dao.getHighScoreOnce() ?: 0
            dao.insertScore(ScoreEntity(score = finalScore))
            val isNewHighScore = finalScore > previousHighScore

            _uiState.update { current ->
                current.copy(
                    isGameOver = true,
                    highScore = maxOf(currentHighScore, finalScore),
                    isNewHighScore = isNewHighScore
                )
            }
        }
    }
}
