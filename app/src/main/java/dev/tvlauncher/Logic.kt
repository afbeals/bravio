package dev.tvlauncher

import kotlin.math.PI
import kotlin.math.sin

/**
 * Slow drift for the clock screens (burn-in protection): a point on a Lissajous path that moves
 * a little every minute. Both fractions stay within [0.1, 0.9] of the screen so nothing is clipped.
 */
fun driftFraction(minuteOfDay: Int): Pair<Float, Float> {
    val m = minuteOfDay.toDouble()
    val x = 0.5 + 0.4 * sin(m * 2 * PI / 97.0)
    val y = 0.5 + 0.4 * sin(m * 2 * PI / 61.0)
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
