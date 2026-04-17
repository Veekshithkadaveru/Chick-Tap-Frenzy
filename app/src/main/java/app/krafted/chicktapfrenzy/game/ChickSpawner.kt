package app.krafted.chicktapfrenzy.game

import kotlin.random.Random

class ChickSpawner(
    private val random: Random = Random.Default
) {
    private var accumulatedSpawnSec: Float = 0f

    fun reset() {
        accumulatedSpawnSec = 0f
    }

    fun tick(
        deltaSeconds: Float,
        round: Int,
        emptyHoleIndices: List<Int>
    ): SpawnEvent? {
        if (deltaSeconds <= 0f) return null

        val profile = profileForRound(round)
        accumulatedSpawnSec += deltaSeconds
        if (accumulatedSpawnSec < profile.spawnGapSec) return null

        accumulatedSpawnSec -= profile.spawnGapSec
        if (emptyHoleIndices.isEmpty()) return null

        val holeIndex = emptyHoleIndices[random.nextInt(emptyHoleIndices.size)]
        val chickType = rollChickType(round)
        return SpawnEvent(
            holeIndex = holeIndex,
            chickType = chickType,
            profile = profile
        )
    }

    fun profileForRound(round: Int): SpawnProfile {
        val safeRound = round.coerceAtLeast(1)
        return SpawnProfile(
            riseSec = 0.25f,
            visibleSec = (1.10f - (0.06f * (safeRound - 1))).coerceAtLeast(0.70f),
            fallSec = 0.25f,
            spawnGapSec = (0.95f - (0.08f * (safeRound - 1))).coerceAtLeast(0.55f)
        )
    }

    fun spawnWeightsForRound(round: Int): Map<ChickType, Int> {
        val safeRound = round.coerceAtLeast(1)
        val foxWeight = if (safeRound <= 1) {
            0
        } else {
            (4 + (3 * (safeRound - 2))).coerceAtMost(16)
        }

        return linkedMapOf(
            ChickType.COMMON_1 to 30,
            ChickType.COMMON_2 to 24,
            ChickType.COMMON_3 to 18,
            ChickType.COMMON_4 to 12,
            ChickType.SPRING to 8,
            ChickType.GOLDEN to 4,
            ChickType.FOX to foxWeight
        )
    }

    private fun rollChickType(round: Int): ChickType {
        val weights = spawnWeightsForRound(round)
        val totalWeight = weights.values.sum()
        val roll = random.nextInt(totalWeight)
        var cursor = 0

        for ((chickType, weight) in weights) {
            if (weight <= 0) continue
            cursor += weight
            if (roll < cursor) {
                return chickType
            }
        }

        return ChickType.COMMON_1
    }

    data class SpawnProfile(
        val riseSec: Float,
        val visibleSec: Float,
        val fallSec: Float,
        val spawnGapSec: Float
    )

    data class SpawnEvent(
        val holeIndex: Int,
        val chickType: ChickType,
        val profile: SpawnProfile
    )
}
