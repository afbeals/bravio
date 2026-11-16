package dev.tvlauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.provider.Settings

enum class InputSignal { Connected, Standby, NoSignal }

data class InputEntry(val id: String, val label: String, val signal: InputSignal) {
    val status: String
        get() = when (signal) {
            InputSignal.Connected -> "Connected"
            InputSignal.Standby -> "Standby"
            InputSignal.NoSignal -> "No signal"
        }
}

/** The TV's physical inputs (HDMI, composite, component), read through the standard TvInputManager. */
class TvInputs(private val context: Context) {
    private val manager = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager

    fun load(): List<InputEntry> = runCatching {
        val m = manager ?: return emptyList()
        val wanted = setOf(TvInputInfo.TYPE_HDMI, TvInputInfo.TYPE_COMPOSITE, TvInputInfo.TYPE_COMPONENT)
        m.tvInputList
            .filter { it.isPassthroughInput && it.type in wanted }
            .map { info ->
                val label = (info.loadCustomLabel(context) ?: info.loadLabel(context))?.toString().orEmpty()
                InputEntry(info.id, label.ifBlank { info.id.substringAfterLast('/') }, signalOf(m.getInputState(info.id)))
            }
            .sortedBy { it.label }
    }.getOrDefault(emptyList())

    private fun signalOf(state: Int): InputSignal = when (state) {
        TvInputManager.INPUT_STATE_CONNECTED -> InputSignal.Connected
        TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> InputSignal.Standby
        else -> InputSignal.NoSignal
    }

    companion object {
        fun switchIntent(inputId: String): Intent =
            Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

data class QuickTile(val label: String, val intent: Intent)

/**
 * Shortcuts to Sony and Android TV setting screens. A tile only shows when its screen exists on this
 * TV, so the row adapts to other models without breaking. Entry points were read from this TV's package list.
 */
fun quickTiles(context: Context): List<QuickTile> {
    fun component(pkg: String, cls: String) =
        Intent(Intent.ACTION_MAIN).setComponent(ComponentName(pkg, cls)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun action(name: String) = Intent(name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    val candidates = listOf(
        QuickTile("Sleep timer", component("com.sony.dtv.timers", "com.sony.dtv.activity.TimersMainActivity")),
        QuickTile("Picture", component("com.sony.dtv.configsettings", "com.sony.dtv.configsettings.PictureSettingActivity")),
        QuickTile("Sound", component("com.sony.dtv.configsettings", "com.sony.dtv.configsettings.SoundSettingActivity")),
        QuickTile("Bravia Sync", component("com.sony.dtv.braviasyncsetting", "com.sony.dtv.braviasyncsetting.BraviaSyncSettingActivity")),
        QuickTile("Notifications", component("com.sony.dtv.notificationcenter", "com.sony.dtv.notificationcenter.NotificationCenterActivity")),
        QuickTile("Wi-Fi", action(Settings.ACTION_WIFI_SETTINGS)),
    )
    val pm = context.packageManager
    return candidates.filter { runCatching { it.intent.resolveActivity(pm) != null }.getOrDefault(false) }
}
