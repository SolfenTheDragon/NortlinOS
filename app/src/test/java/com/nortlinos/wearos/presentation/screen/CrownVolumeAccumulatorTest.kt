package com.nortlinos.wearos.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class CrownVolumeAccumulatorTest {
    @Test
    fun smallTurnsAccumulateIntoOneStep() {
        val accumulator = CrownVolumeAccumulator(pixelsPerStep = 40f)
        assertEquals(0, accumulator.add(15f))
        assertEquals(0, accumulator.add(15f))
        assertEquals(1, accumulator.add(15f))
    }

    @Test
    fun largeTurnProducesSeveralSteps() {
        val accumulator = CrownVolumeAccumulator(pixelsPerStep = 40f)
        assertEquals(2, accumulator.add(95f))
        assertEquals(1, accumulator.add(25f))
    }

    @Test
    fun counterClockwiseLowers() {
        val accumulator = CrownVolumeAccumulator(pixelsPerStep = 40f)
        assertEquals(-1, accumulator.add(-45f))
    }

    @Test
    fun reversingDiscardsLeftover() {
        val accumulator = CrownVolumeAccumulator(pixelsPerStep = 40f)
        assertEquals(0, accumulator.add(35f))
        assertEquals(0, accumulator.add(-10f))
        assertEquals(0, accumulator.add(-25f))
        assertEquals(-1, accumulator.add(-5f))
    }

    @Test
    fun zeroIsIgnored() {
        val accumulator = CrownVolumeAccumulator(pixelsPerStep = 40f)
        assertEquals(0, accumulator.add(0f))
    }
}
