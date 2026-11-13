package moe.shizuku.manager.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartCircuitBreakerTest {

    @Test
    fun opensOnMaxAttemptsWithinWindowAndReportsOnce() {
        val b = StartCircuitBreaker(maxAttempts = 3, windowMs = 1000)
        assertFalse(b.open)
        assertFalse(b.record(0))
        assertFalse(b.record(200))
        assertTrue(b.record(400)) // opens here
        assertTrue(b.open)
        // further records while open return false and keep open
        assertFalse(b.record(500))
        assertTrue(b.open)
    }

    @Test
    fun attemptsOutsideWindowAreDropped() {
        val b = StartCircuitBreaker(maxAttempts = 3, windowMs = 1000)
        assertFalse(b.record(0))
        assertFalse(b.record(900))
        // third is outside 1000ms window relative to first -> first dropped
        assertFalse(b.record(1200))
        assertFalse(b.open)
        assertFalse(b.record(1500))
        assertTrue(b.record(1800))
        assertTrue(b.open)
    }

    @Test
    fun resetClearsCountAndRearms() {
        val b = StartCircuitBreaker(maxAttempts = 2, windowMs = 1000)
        assertFalse(b.record(0))
        assertTrue(b.record(100))
        assertTrue(b.open)
        b.reset()
        assertFalse(b.open)
        assertFalse(b.record(500))
        assertTrue(b.record(600))
    }

    @Test
    fun singleAttemptDoesNotOpenWhenMaxGreaterThanOne() {
        val b = StartCircuitBreaker(maxAttempts = 5, windowMs = 120000)
        assertFalse(b.record(1000))
        assertFalse(b.open)
    }
}
