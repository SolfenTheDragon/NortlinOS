package com.nortlinos.wearos.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionValidationThrottleTest {
    private val interval = SessionValidationThrottle.INTERVAL_MS

    @Test
    fun validatesWhenNeverValidated() {
        assertTrue(SessionValidationThrottle.shouldValidate(1_000L, null))
    }

    @Test
    fun skipsWithinInterval() {
        assertFalse(SessionValidationThrottle.shouldValidate(10_000L, 10_000L))
        assertFalse(SessionValidationThrottle.shouldValidate(10_000L + interval - 1, 10_000L))
    }

    @Test
    fun validatesOnceIntervalElapsed() {
        assertTrue(SessionValidationThrottle.shouldValidate(10_000L + interval, 10_000L))
    }

    @Test
    fun validatesWhenClockMovedBackwards() {
        assertTrue(SessionValidationThrottle.shouldValidate(5_000L, 10_000L))
    }
}
