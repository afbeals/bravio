package dev.tvlauncher

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime

/** True while the wallpaper should be dimmed; provided once at the top of the home screen. */
val LocalDim = compositionLocalOf { false }

/**
 * Dim state for the day/dim wallpaper mode. Uses the light sensor when the TV has one (with hysteresis),
 * otherwise the clock (see [isNightHour]). Does nothing, and registers nothing, while [enabled] is false.
 */
@Composable
fun rememberDim(enabled: Boolean): Boolean {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val lightSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }
    var sensorDim by remember { mutableStateOf(false) }

    DisposableEffect(enabled, lightSensor) {
        if (!enabled || sensorManager == null || lightSensor == null) return@DisposableEffect onDispose { }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                sensorDim = nextDim(sensorDim, event.values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val clockDim by produceState(false, enabled, lightSensor) {
        while (enabled && lightSensor == null) {
            value = isNightHour(LocalDateTime.now().hour)
            delay(60_000)
        }
        if (!enabled || lightSensor != null) value = false
    }
    return enabled && (if (lightSensor != null) sensorDim else clockDim)
}

/** Current network, kept up to date with a default-network callback (needs ACCESS_NETWORK_STATE). */
@Composable
fun rememberNetworkState(): NetworkState {
    val context = LocalContext.current
    var state by remember { mutableStateOf(NetworkState.Offline) }
    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                state = classifyNetwork(
                    connected = true,
                    wifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                    ethernet = caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET),
                    validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                )
            }

            override fun onLost(network: Network) {
                state = NetworkState.Offline
            }
        }
        val registered = runCatching { cm?.registerDefaultNetworkCallback(callback) }.isSuccess
        onDispose { if (registered) runCatching { cm?.unregisterNetworkCallback(callback) } }
    }
    return state
}

/** One backup file in the app's external folder, so it can be moved with `adb pull` / `adb push`. */
object Backup {
    const val FILE_NAME = "launcher-backup.json"

    private fun file(context: Context): File? = context.getExternalFilesDir(null)?.let { File(it, FILE_NAME) }

    fun exists(context: Context): Boolean = file(context)?.let { it.isFile && it.length() > 0 } == true

    fun save(context: Context, config: Config): Boolean =
        runCatching { file(context)!!.writeText(Config.encode(config)) }.isSuccess

    /** Null when there is no file or it is not a valid config, so a bad file never wipes the current settings. */
    fun load(context: Context): Config? =
        runCatching { Config.decodeOrNull(file(context)!!.readText()) }.getOrNull()
}
