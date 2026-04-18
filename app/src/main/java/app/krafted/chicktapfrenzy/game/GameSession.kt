package app.krafted.chicktapfrenzy.game

class GameSession(
    private val chickSpawner: ChickSpawner = ChickSpawner(),
    private val startingRound: Int = 1,
    private val roundDurationSec: Float = ROUND_DURATION_SEC
) {
    private val holeStates = List(HOLE_COUNT) { HoleState() }
    private val activeTypes = MutableList<ChickType?>(HOLE_COUNT) { null }
    private val resolvedMisses = MutableList(HOLE_COUNT) { false }
    private val scoreFloats = mutableListOf<ScoreFloat>()

    private var nextScoreFloatId: Long = 1L
    private var score: Int = 0
    private var lives: Int = STARTING_LIVES
    private var round: Int = 1
    private var backgroundIndex: Int = 0
    private var roundTimeRemaining: Float = roundDurationSec
    private var isGameOver: Boolean = false
    private var isRoundComplete: Boolean = false

    init {
        reset()
    }

    fun reset(): GameSessionSnapshot {
        holeStates.forEach(HoleState::reset)
        activeTypes.fill(null)
        resolvedMisses.fill(false)
        scoreFloats.clear()
        chickSpawner.reset()

        nextScoreFloatId = 1L
        score = 0
        lives = STARTING_LIVES
        round = startingRound.coerceAtLeast(1)
        backgroundIndex = 0
        roundTimeRemaining = roundDurationSec
        isGameOver = false
        isRoundComplete = false

        return snapshot()
    }

    fun tick(deltaSeconds: Float): GameSessionSnapshot {
        if (deltaSeconds <= 0f || isGameOver) {
            return snapshot()
        }

        for (index in scoreFloats.indices) {
            scoreFloats[index] = scoreFloats[index].advance(deltaSeconds)
        }
        scoreFloats.removeAll { it.isExpired }

        if (!isRoundComplete) {
            roundTimeRemaining -= deltaSeconds
            if (roundTimeRemaining <= 0f) {
                roundTimeRemaining = 0f
                isRoundComplete = true
                holeStates.forEach(HoleState::reset)
                activeTypes.fill(null)
                resolvedMisses.fill(false)
            }
        }

        holeStates.forEachIndexed { holeIndex, holeState ->
            holeState.update(deltaSeconds)

            if (holeState.wasMissed) {
                resolveMiss(holeIndex, allowFallingPhase = true, triggerFall = false)
            }

            if (holeState.phase == HolePhase.EMPTY) {
                activeTypes[holeIndex] = null
                resolvedMisses[holeIndex] = false
            }
        }

        if (!isGameOver && !isRoundComplete) {
            val spawnEvents = chickSpawner.tick(
                deltaSeconds = deltaSeconds,
                score = score,
                round = round,
                emptyHoleIndices = emptyHoleIndices()
            )
            spawnEvents.forEach { spawn(it) }
        }

        return snapshot()
    }

    fun startNextRound(): GameSessionSnapshot {
        round += 1
        backgroundIndex = (backgroundIndex + 1) % 5
        roundTimeRemaining = roundDurationSec
        isRoundComplete = false
        chickSpawner.reset()
        return snapshot()
    }

    fun onHoleTapped(holeIndex: Int): GameSessionSnapshot {
        if (!canInteractWithHole(holeIndex)) {
            return snapshot()
        }

        val holeState = holeStates[holeIndex]
        val chickType = activeTypes[holeIndex] ?: return snapshot()
        if (!holeState.isTappable) {
            return snapshot()
        }

        resolvedMisses[holeIndex] = true
        when (chickType) {
            ChickType.FOX -> {
                loseLife(holeIndex, label = "-1")
            }

            ChickType.GOLDEN -> {
                score += chickType.scoreValue
                scoreFloats += createScoreFloat(
                    holeIndex = holeIndex,
                    label = "+${chickType.scoreValue}",
                    tone = ScoreFloatTone.BONUS
                )
            }

            else -> {
                score += chickType.scoreValue
                scoreFloats += createScoreFloat(
                    holeIndex = holeIndex,
                    label = "+${chickType.scoreValue}",
                    tone = ScoreFloatTone.POSITIVE
                )
            }
        }

        holeState.markTapped()
        return snapshot()
    }

    fun onCharacterMissed(holeIndex: Int): GameSessionSnapshot {
        resolveMiss(holeIndex, allowFallingPhase = false, triggerFall = true)
        return snapshot()
    }

    fun endGame(): GameSessionSnapshot {
        isGameOver = true
        return snapshot()
    }

    fun snapshot(): GameSessionSnapshot = GameSessionSnapshot(
        score = score,
        lives = lives,
        round = round,
        backgroundIndex = backgroundIndex,
        roundTimeRemaining = roundTimeRemaining,
        isGameOver = isGameOver,
        isRoundComplete = isRoundComplete,
        holes = holeStates.mapIndexed { holeIndex, holeState ->
            HoleSnapshot(
                holeIndex = holeIndex,
                phase = holeState.phase,
                progress = holeState.progress,
                popOffset = holeState.popOffset,
                isTappable = holeState.isTappable,
                chickType = activeTypes[holeIndex]
            )
        },
        scoreFloats = scoreFloats.toList()
    )

    private fun spawn(spawnEvent: ChickSpawner.SpawnEvent) {
        val holeIndex = spawnEvent.holeIndex
        activeTypes[holeIndex] = spawnEvent.chickType
        resolvedMisses[holeIndex] = false
        holeStates[holeIndex].startPop(
            characterId = spawnEvent.chickType.characterId,
            riseSec = spawnEvent.profile.riseSec,
            visibleSec = spawnEvent.profile.visibleSec,
            fallSec = spawnEvent.profile.fallSec
        )
    }

    private fun resolveMiss(
        holeIndex: Int,
        allowFallingPhase: Boolean,
        triggerFall: Boolean
    ) {
        if (!canInteractWithHole(holeIndex)) {
            return
        }

        val holeState = holeStates[holeIndex]
        if (!allowFallingPhase && holeState.phase == HolePhase.FALLING) {
            return
        }
        if (resolvedMisses[holeIndex]) {
            return
        }

        val chickType = activeTypes[holeIndex] ?: return
        resolvedMisses[holeIndex] = true
        if (!chickType.penalizesOnMiss) {
            return
        }

        loseLife(holeIndex, label = "-1")
        if (triggerFall && holeState.isTappable) {
            holeState.markTapped()
        }
    }

    private fun loseLife(holeIndex: Int, label: String) {
        lives = (lives - 1).coerceAtLeast(0)
        scoreFloats += createScoreFloat(
            holeIndex = holeIndex,
            label = label,
            tone = ScoreFloatTone.PENALTY
        )
        if (lives == 0) {
            isGameOver = true
        }
    }

    private fun createScoreFloat(
        holeIndex: Int,
        label: String,
        tone: ScoreFloatTone
    ): ScoreFloat = ScoreFloat(
        id = nextScoreFloatId++,
        holeIndex = holeIndex,
        label = label,
        tone = tone
    )

    private fun emptyHoleIndices(): List<Int> =
        holeStates.mapIndexedNotNull { holeIndex, holeState ->
            if (holeState.phase == HolePhase.EMPTY) holeIndex else null
        }

    private fun canInteractWithHole(holeIndex: Int): Boolean =
        !isGameOver && holeIndex in 0 until HOLE_COUNT

    companion object {
        const val HOLE_COUNT: Int = 6
        const val STARTING_LIVES: Int = 3
        const val ROUND_DURATION_SEC: Float = 30f
    }
}

data class GameSessionSnapshot(
    val score: Int,
    val lives: Int,
    val round: Int,
    val backgroundIndex: Int,
    val roundTimeRemaining: Float,
    val isGameOver: Boolean,
    val isRoundComplete: Boolean,
    val holes: List<HoleSnapshot>,
    val scoreFloats: List<ScoreFloat>
)
