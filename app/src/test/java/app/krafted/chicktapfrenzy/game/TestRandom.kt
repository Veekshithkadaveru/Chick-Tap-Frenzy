package app.krafted.chicktapfrenzy.game

import kotlin.random.Random

class QueueRandom(
    values: List<Int>
) : Random() {
    private val queue = ArrayDeque(values)

    override fun nextBits(bitCount: Int): Int {
        throw UnsupportedOperationException("nextBits should not be called in these tests")
    }

    override fun nextInt(until: Int): Int {
        require(until > 0) { "until must be positive" }
        val nextValue = queue.removeFirstOrNull()
            ?: error("QueueRandom ran out of values for bound $until")
        require(nextValue in 0 until until) {
            "Random value $nextValue is outside 0 until $until"
        }
        return nextValue
    }
}

fun rollForType(round: Int, chickType: ChickType): Int {
    val weights = ChickSpawner().spawnWeightsForRound(round)
    var cursor = 0
    for ((type, weight) in weights) {
        if (weight <= 0) continue
        if (type == chickType) {
            return cursor
        }
        cursor += weight
    }
    error("No selectable weight for $chickType in round $round")
}
