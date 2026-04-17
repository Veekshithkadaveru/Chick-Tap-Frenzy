package app.krafted.chicktapfrenzy.game

enum class ScoreFloatTone {
    POSITIVE,
    BONUS,
    PENALTY
}

data class ScoreFloat(
    val id: Long,
    val holeIndex: Int,
    val label: String,
    val tone: ScoreFloatTone,
    val ageSec: Float = 0f,
    val durationSec: Float = DEFAULT_DURATION_SEC
) {
    val progress: Float
        get() = (ageSec / durationSec).coerceIn(0f, 1f)

    val alpha: Float
        get() = 1f - progress

    val riseOffset: Float
        get() = progress * MAX_RISE_OFFSET

    val isExpired: Boolean
        get() = ageSec >= durationSec

    fun advance(deltaSeconds: Float): ScoreFloat {
        if (deltaSeconds <= 0f) return this
        return copy(ageSec = (ageSec + deltaSeconds).coerceAtMost(durationSec))
    }

    companion object {
        const val DEFAULT_DURATION_SEC: Float = 0.9f
        const val MAX_RISE_OFFSET: Float = 48f
    }
}
