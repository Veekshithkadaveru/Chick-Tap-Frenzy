package app.krafted.chicktapfrenzy.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChickSpawnerTest {

    @Test
    fun roundOneNeverSelectsFoxOnHighestRoll() {
        val spawner = ChickSpawner(
            random = QueueRandom(listOf(0, 95))
        )

        val event = spawner.tick(
            deltaSeconds = 1f,
            round = 1,
            emptyHoleIndices = listOf(3)
        )

        checkNotNull(event)
        assertEquals(3, event.holeIndex)
        assertEquals(0, spawner.spawnWeightsForRound(1).getValue(ChickType.FOX))
        assertEquals(ChickType.GOLDEN, event.chickType)
    }

    @Test
    fun roundTwoCanSelectFoxAndFoxWeightCaps() {
        val spawner = ChickSpawner(
            random = QueueRandom(listOf(0, 99))
        )

        val event = spawner.tick(
            deltaSeconds = 1f,
            round = 2,
            emptyHoleIndices = listOf(1)
        )

        checkNotNull(event)
        assertEquals(ChickType.FOX, event.chickType)
        assertEquals(4, spawner.spawnWeightsForRound(2).getValue(ChickType.FOX))
        assertEquals(16, spawner.spawnWeightsForRound(10).getValue(ChickType.FOX))
    }

    @Test
    fun tickChoosesOnlyFromProvidedEmptyHoles() {
        val spawner = ChickSpawner(
            random = QueueRandom(listOf(1, 0))
        )

        val event = spawner.tick(
            deltaSeconds = 1f,
            round = 1,
            emptyHoleIndices = listOf(2, 4)
        )

        checkNotNull(event)
        assertEquals(4, event.holeIndex)
        assertTrue(event.holeIndex in listOf(2, 4))
        assertEquals(ChickType.COMMON_1, event.chickType)
    }
}
