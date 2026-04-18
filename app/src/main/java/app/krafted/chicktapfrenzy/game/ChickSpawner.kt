package app.krafted.chicktapfrenzy.game

import kotlin.math.pow
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
        score: Int,
        emptyHoleIndices: List<Int>
    ): List<SpawnEvent> {
        if (deltaSeconds <= 0f) return emptyList()

        val profile = profileForScore(score)
        accumulatedSpawnSec += deltaSeconds
        if (accumulatedSpawnSec < profile.spawnGapSec) return emptyList()

        accumulatedSpawnSec -= profile.spawnGapSec
        if (emptyHoleIndices.isEmpty()) return emptyList()

        val spawnCount = when {
            score > 40 && emptyHoleIndices.size >= 3 && random.nextFloat() < 0.10f -> 3
            score > 15 && emptyHoleIndices.size >= 2 && random.nextFloat() < 0.20f -> 2
            else -> 1
        }
        val chosenHoles = emptyHoleIndices.shuffled(random).take(spawnCount)
        return chosenHoles.map { holeIndex ->
            SpawnEvent(
                holeIndex = holeIndex,
                chickType = rollChickType(score),
                profile = profile
            )
        }
    }

    fun profileForScore(score: Int): SpawnProfile {
        val tier = score / 10
        val scale = 0.95f.pow(tier)
        return SpawnProfile(
            riseSec = 0.25f,
            visibleSec = (1.10f * scale).coerceAtLeast(0.60f),
            fallSec = 0.25f,
            spawnGapSec = (0.95f * scale).coerceAtLeast(0.40f)
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

    fun spawnWeightsForScore(score: Int): Map<ChickType, Int> {
        val foxWeight = if (score < 10) 0
            else ((score - 10).toFloat() / 30f * 28f).toInt().coerceIn(0, 28)
        return linkedMapOf(
            ChickType.CHICKEN to 80,
            ChickType.GOLDEN to 4,
            ChickType.FOX to foxWeight
        )
    }

    private fun rollChickType(score: Int): ChickType {
        val weights = spawnWeightsForScore(score)
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

        return ChickType.CHICKEN
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
