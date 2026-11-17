package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test
    fun driftStaysWithin24PxAllDayAndKeepsMoving() {
        var previous: Pair<Float, Float>? = null
        var moved = 0
        for (minute in 0 until 1440) {
            val (x, y) = driftOffset(minute)
            assertTrue("x=$x at $minute", x in -DRIFT_PX..DRIFT_PX)
            assertTrue("y=$y at $minute", y in -DRIFT_PX..DRIFT_PX)
            if (previous != null && previous != (x to y)) moved++
            previous = x to y
        }
        assertTrue(moved > 1400)
    }

    @Test
    fun dimmingHasHysteresis() {
        assertTrue(nextDim(currentlyDim = false, lux = 2f))
        assertFalse(nextDim(currentlyDim = true, lux = 50f))
        // Between the thresholds the state is kept either way.
        assertTrue(nextDim(currentlyDim = true, lux = 12f))
        assertFalse(nextDim(currentlyDim = false, lux = 12f))
    }

    @Test
    fun nightHoursWrapMidnight() {
        assertTrue(isNightHour(22))
        assertTrue(isNightHour(0))
        assertTrue(isNightHour(6))
        assertFalse(isNightHour(7))
        assertFalse(isNightHour(21))
    }

    @Test
    fun networkClassificationPrefersOfflineThenValidation() {
        assertEquals(NetworkState.Offline, classifyNetwork(connected = false, wifi = true, ethernet = false, validated = true))
        assertEquals(NetworkState.NoInternet, classifyNetwork(true, wifi = true, ethernet = false, validated = false))
        assertEquals(NetworkState.WiFi, classifyNetwork(true, wifi = true, ethernet = false, validated = true))
        assertEquals(NetworkState.Ethernet, classifyNetwork(true, wifi = false, ethernet = true, validated = true))
        assertEquals(NetworkState.Other, classifyNetwork(true, wifi = false, ethernet = false, validated = true))
        assertNotEquals(NetworkState.WiFi, classifyNetwork(true, wifi = false, ethernet = true, validated = true))
    }
}
