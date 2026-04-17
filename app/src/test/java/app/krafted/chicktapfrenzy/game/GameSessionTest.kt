package app.krafted.chicktapfrenzy.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun tappingEachScoringChickAppliesExpectedPointsAndFeedback() {
        val expectations = linkedMapOf(
            ChickType.COMMON_1 to Pair(1, ScoreFloatTone.POSITIVE),
            ChickType.COMMON_2 to Pair(1, ScoreFloatTone.POSITIVE),
            ChickType.COMMON_3 to Pair(2, ScoreFloatTone.POSITIVE),
            ChickType.COMMON_4 to Pair(2, ScoreFloatTone.POSITIVE),
            ChickType.SPRING to Pair(3, ScoreFloatTone.POSITIVE),
            ChickType.GOLDEN to Pair(5, ScoreFloatTone.BONUS)
        )

        expectations.forEach { (type, expected) ->
            val session = spawnSession(type = type)

            val tapped = session.onHoleTapped(0)

            assertEquals(expected.first.toInt(), tapped.score)
            assertEquals(GameSession.STARTING_LIVES, tapped.lives)
            assertEquals(HolePhase.FALLING, tapped.holes[0].phase)
            assertEquals(type, tapped.holes[0].chickType)
            assertEquals(1, tapped.scoreFloats.size)
            assertEquals("+${expected.first}", tapped.scoreFloats.single().label)
            assertEquals(expected.second, tapped.scoreFloats.single().tone)
        }
    }

    @Test
    fun tappingFoxCostsLifeAndCreatesPenaltyFloat() {
        val session = spawnSession(
            type = ChickType.FOX,
            round = 2
        )

        val tapped = session.onHoleTapped(0)

        assertEquals(0, tapped.score)
        assertEquals(2, tapped.lives)
        assertEquals(HolePhase.FALLING, tapped.holes[0].phase)
        assertEquals(1, tapped.scoreFloats.size)
        assertEquals("-1 LIFE", tapped.scoreFloats.single().label)
        assertEquals(ScoreFloatTone.PENALTY, tapped.scoreFloats.single().tone)
    }

    @Test
    fun emptyAndRepeatedTapsAreNoOps() {
        val untouchedSession = GameSession()
        val emptyTap = untouchedSession.onHoleTapped(0)
        assertEquals(0, emptyTap.score)
        assertEquals(0, emptyTap.scoreFloats.size)

        val session = spawnSession(type = ChickType.COMMON_1)
        val firstTap = session.onHoleTapped(0)
        val secondTap = session.onHoleTapped(0)

        assertEquals(firstTap.score, secondTap.score)
        assertEquals(firstTap.lives, secondTap.lives)
        assertEquals(1, secondTap.scoreFloats.size)
        assertEquals(HolePhase.FALLING, secondTap.holes[0].phase)
    }

    @Test
    fun missingGoldenCostsLifeWhileMissingFoxDoesNot() {
        val goldenSession = spawnSession(type = ChickType.GOLDEN)
        val goldenMiss = goldenSession.onCharacterMissed(0)
        assertEquals(2, goldenMiss.lives)
        assertEquals(0, goldenMiss.score)
        assertEquals(ScoreFloatTone.PENALTY, goldenMiss.scoreFloats.single().tone)

        val foxSession = spawnSession(
            type = ChickType.FOX,
            round = 2
        )
        val foxMiss = foxSession.onCharacterMissed(0)
        assertEquals(GameSession.STARTING_LIVES, foxMiss.lives)
        assertTrue(foxMiss.scoreFloats.isEmpty())
    }

    @Test
    fun tickPublishesSpawnedHoleAndAutoMissPenalty() {
        val session = GameSession(
            chickSpawner = ChickSpawner(
                random = QueueRandom(
                    listOf(
                        0, rollForType(1, ChickType.COMMON_2),
                        0, rollForType(1, ChickType.COMMON_1)
                    )
                )
            )
        )

        val spawned = session.tick(1f)
        val afterMiss = session.tick(1.4f)

        assertEquals(ChickType.COMMON_2, spawned.holes[0].chickType)
        assertTrue(spawned.holes[0].isTappable)
        assertEquals(2, afterMiss.lives)
        assertEquals(ScoreFloatTone.PENALTY, afterMiss.scoreFloats.first().tone)
        assertFalse(afterMiss.isGameOver)
    }

    @Test
    fun threeMissesTriggerGameOver() {
        val session = GameSession(
            chickSpawner = ChickSpawner(
                random = QueueRandom(
                    listOf(
                        0, rollForType(1, ChickType.COMMON_1),
                        0, rollForType(1, ChickType.COMMON_1),
                        0, rollForType(1, ChickType.COMMON_1)
                    )
                )
            )
        )

        repeat(2) {
            session.tick(1f)
            session.onCharacterMissed(0)
            session.tick(0.3f)
        }

        session.tick(1f)
        val finalState = session.onCharacterMissed(0)

        assertEquals(0, finalState.lives)
        assertTrue(finalState.isGameOver)
        assertEquals(1, finalState.scoreFloats.size)
        assertEquals(ScoreFloatTone.PENALTY, finalState.scoreFloats.single().tone)
    }

    private fun spawnSession(
        type: ChickType,
        round: Int = 1
    ): GameSession {
        val session = GameSession(
            chickSpawner = ChickSpawner(
                random = QueueRandom(listOf(0, rollForType(round, type)))
            ),
            startingRound = round
        )
        session.tick(1f)
        return session
    }
}
