package app.krafted.chicktapfrenzy.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreFloatTest {

    @Test
    fun advanceFadesAndRisesMonotonically() {
        val initial = ScoreFloat(
            id = 1L,
            holeIndex = 0,
            label = "+2",
            tone = ScoreFloatTone.POSITIVE
        )
        val mid = initial.advance(0.3f)
        val late = mid.advance(0.3f)

        assertTrue(initial.alpha > mid.alpha)
        assertTrue(mid.alpha > late.alpha)
        assertTrue(initial.riseOffset < mid.riseOffset)
        assertTrue(mid.riseOffset < late.riseOffset)
        assertFalse(mid.isExpired)
    }

    @Test
    fun floatExpiresExactlyAtDurationBoundary() {
        val scoreFloat = ScoreFloat(
            id = 7L,
            holeIndex = 2,
            label = "-1 LIFE",
            tone = ScoreFloatTone.PENALTY
        )

        val expired = scoreFloat.advance(ScoreFloat.DEFAULT_DURATION_SEC)

        assertTrue(expired.isExpired)
        assertEquals(0f, expired.alpha)
        assertEquals(ScoreFloat.MAX_RISE_OFFSET, expired.riseOffset)
    }
}
