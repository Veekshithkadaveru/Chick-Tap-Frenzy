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
        round: Int,
        emptyHoleIndices: List<Int>
    ): List<SpawnEvent> {
        if (deltaSeconds <= 0f) return emptyList()

        val scoreProfile = profileForScore(score, round)
        val roundProfile = profileForRound(round)
        val profile = SpawnProfile(
            riseSec = 0.25f,
            visibleSec = minOf(scoreProfile.visibleSec, roundProfile.visibleSec),
            fallSec = 0.25f,
            spawnGapSec = minOf(scoreProfile.spawnGapSec, roundProfile.spawnGapSec)
        )
        accumulatedSpawnSec += deltaSeconds
        if (accumulatedSpawnSec < profile.spawnGapSec) return emptyList()

        accumulatedSpawnSec -= profile.spawnGapSec
        if (emptyHoleIndices.isEmpty()) return emptyList()

        // Multi-spawn only unlocks in higher rounds so early game stays calm
        val spawnCount = when {
            round >= 6 && emptyHoleIndices.size >= 3 && random.nextFloat() < 0.12f -> 3
            round >= 3 && emptyHoleIndices.size >= 2 && random.nextFloat() < 0.22f -> 2
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

    // Score-based scaling is intentionally gentle so it doesn't outpace round difficulty
    fun profileForScore(score: Int, round: Int = 1): SpawnProfile {
        val tier = score / 15                               // ticks every 15 pts (was 10)
        val scale = 0.97f.pow(tier)                        // 3 % per tier (was 5 %)
        // Base limits are set by the round profile; score only tightens within that
        val roundVisibleFloor = profileForRound(round).visibleSec
        val roundGapFloor     = profileForRound(round).spawnGapSec
        return SpawnProfile(
            riseSec = 0.25f,
            visibleSec = (roundVisibleFloor * scale).coerceAtLeast(roundVisibleFloor * 0.80f),
            fallSec = 0.25f,
            spawnGapSec = (roundGapFloor * scale).coerceAtLeast(roundGapFloor * 0.80f)
        )
    }

    fun profileForRound(round: Int): SpawnProfile {
        val r = round.coerceAtLeast(1)
        // Round 1: very slow — chick stays 2.5 s, spawns every 1.8 s
        // Each round: −0.20 s visible, −0.15 s gap, floored at 0.70 s / 0.45 s
        val visibleSec  = (2.50f - (0.20f * (r - 1))).coerceAtLeast(0.70f)
        val spawnGapSec = (1.80f - (0.15f * (r - 1))).coerceAtLeast(0.45f)
        return SpawnProfile(
            riseSec = 0.25f,
            visibleSec = visibleSec,
            fallSec = 0.25f,
            spawnGapSec = spawnGapSec
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
