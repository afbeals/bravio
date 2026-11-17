package dev.tvlauncher

import kotlin.math.PI
import kotlin.math.sin

/** The clock may wander this far from the center in either direction (design px, one px per screen pixel). */
const val DRIFT_PX = 24f

/**
 * Slow drift for the clock screens (burn-in protection): a point on a Lissajous path that moves a little
 * every minute, as an offset in px from the center. Both values stay within [-DRIFT_PX, DRIFT_PX].
 */
fun driftOffset(minuteOfDay: Int): Pair<Float, Float> {
    val m = minuteOfDay.toDouble()
    val x = DRIFT_PX * sin(m * 2 * PI / 97.0)
    val y = DRIFT_PX * sin(m * 2 * PI / 61.0)
    return x.toFloat() to y.toFloat()
}

const val DIM_BELOW_LUX = 8f
const val UNDIM_ABOVE_LUX = 20f

/** Light-sensor dimming with hysteresis so the screen doesn't flicker around one threshold. */
fun nextDim(currentlyDim: Boolean, lux: Float): Boolean = when {
    lux < DIM_BELOW_LUX -> true
    lux > UNDIM_ABOVE_LUX -> false
    else -> currentlyDim
}

/** Fallback when there is no light sensor: dim between 22:00 and 07:00. */
fun isNightHour(hour: Int): Boolean = hour >= 22 || hour < 7

enum class NetworkState { Offline, NoInternet, WiFi, Ethernet, Other }

fun classifyNetwork(connected: Boolean, wifi: Boolean, ethernet: Boolean, validated: Boolean): NetworkState = when {
    !connected -> NetworkState.Offline
    !validated -> NetworkState.NoInternet
    wifi -> NetworkState.WiFi
    ethernet -> NetworkState.Ethernet
    else -> NetworkState.Other
}
