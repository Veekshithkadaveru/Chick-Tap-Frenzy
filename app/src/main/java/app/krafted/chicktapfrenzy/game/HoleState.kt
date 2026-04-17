package app.krafted.chicktapfrenzy.game

enum class HolePhase { EMPTY, RISING, VISIBLE, FALLING }

class HoleState {

    var phase: HolePhase = HolePhase.EMPTY
        private set

    var progress: Float = 0f
        private set

    var characterId: Int = 0
        private set

    var wasMissed: Boolean = false
        private set

    private var riseDurationSec: Float = DEFAULT_RISE_SEC
    private var visibleDurationSec: Float = DEFAULT_VISIBLE_SEC
    private var fallDurationSec: Float = DEFAULT_FALL_SEC

    val popOffset: Float
        get() = when (phase) {
            HolePhase.EMPTY -> 0f
            HolePhase.RISING -> progress.coerceIn(0f, 1f)
            HolePhase.VISIBLE -> 1f
            HolePhase.FALLING -> (1f - progress).coerceIn(0f, 1f)
        }

    val isTappable: Boolean
        get() = phase == HolePhase.RISING || phase == HolePhase.VISIBLE

    fun startPop(
        characterId: Int,
        riseSec: Float = DEFAULT_RISE_SEC,
        visibleSec: Float = DEFAULT_VISIBLE_SEC,
        fallSec: Float = DEFAULT_FALL_SEC
    ) {
        this.characterId = characterId
        this.riseDurationSec = riseSec
        this.visibleDurationSec = visibleSec
        this.fallDurationSec = fallSec
        this.phase = HolePhase.RISING
        this.progress = 0f
        this.wasMissed = false
    }

    fun markTapped() {
        phase = HolePhase.FALLING
        progress = 0f
    }

    fun reset() {
        phase = HolePhase.EMPTY
        progress = 0f
        characterId = 0
        wasMissed = false
    }

    fun update(deltaSeconds: Float) {
        wasMissed = false
        if (phase == HolePhase.EMPTY || deltaSeconds <= 0f) return

        var remaining = deltaSeconds
        while (remaining > 0f && phase != HolePhase.EMPTY) {
            val duration = currentPhaseDuration()
            if (duration <= 0f) {
                advancePhase()
                continue
            }
            val progressStep = remaining / duration
            if (progress + progressStep < 1f) {
                progress += progressStep
                remaining = 0f
            } else {
                val consumed = (1f - progress) * duration
                remaining -= consumed
                progress = 1f
                advancePhase()
            }
        }
    }

    private fun currentPhaseDuration(): Float = when (phase) {
        HolePhase.RISING -> riseDurationSec
        HolePhase.VISIBLE -> visibleDurationSec
        HolePhase.FALLING -> fallDurationSec
        HolePhase.EMPTY -> 0f
    }

    private fun advancePhase() {
        when (phase) {
            HolePhase.RISING -> {
                phase = HolePhase.VISIBLE
                progress = 0f
            }

            HolePhase.VISIBLE -> {
                phase = HolePhase.FALLING
                progress = 0f
                wasMissed = true
            }

            HolePhase.FALLING -> {
                phase = HolePhase.EMPTY
                progress = 0f
                characterId = 0
            }

            HolePhase.EMPTY -> Unit
        }
    }

    companion object {
        const val DEFAULT_RISE_SEC: Float = 0.25f
        const val DEFAULT_VISIBLE_SEC: Float = 1.0f
        const val DEFAULT_FALL_SEC: Float = 0.25f
    }
}
