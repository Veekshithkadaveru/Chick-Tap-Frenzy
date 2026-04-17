package app.krafted.chicktapfrenzy.game

data class HoleSnapshot(
    val holeIndex: Int,
    val phase: HolePhase,
    val popOffset: Float,
    val isTappable: Boolean,
    val chickType: ChickType?
) {
    companion object {
        fun empty(holeIndex: Int): HoleSnapshot = HoleSnapshot(
            holeIndex = holeIndex,
            phase = HolePhase.EMPTY,
            popOffset = 0f,
            isTappable = false,
            chickType = null
        )
    }
}
