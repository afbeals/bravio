package dev.tvlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Full-screen clock (180 px time, 30 px date) that drifts up to 24 px so nothing burns in. Any key returns.
 * Between 22:00 and 07:00 it uses the dim palette; with the wallpaper option on, pictures sit behind an 80
 * percent scrim and a solid 1040 x 360 panel holds the time.
 */
@Composable
fun ClockScreen(config: Config, onExit: () -> Unit) {
    val profile = config.activeProfile
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusWhenReady() }
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val colors = if (config.screensaverDim && isNightHour(now.hour)) DimInk else DarkInk
    val (dx, dy) = driftOffset(now.hour * 60 + now.minute)
    val time = now.format(DateTimeFormatter.ofPattern(if (profile.clock24h) "HH:mm" else "h:mm a"))
    val date = runCatching { now.format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("")
    val picture = config.screensaverWallpaper && isPhotoWallpaper(profile.wallpaper)
    CompositionLocalProvider(LocalInkColors provides colors) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        onExit()
                        true
                    } else {
                        false
                    }
                }
                .focusRequester(focus)
                .focusable(),
            contentAlignment = Alignment.Center,
        ) {
            if (config.screensaverWallpaper) {
                WallpaperLayer(profile.wallpaper, if (picture) WallpaperSurface.ScrimOnly else WallpaperSurface.Full)
            }
            Box(
                Modifier.offset(px(dx.toInt()), px(dy.toInt())).then(
                    if (picture) Modifier.size(px(1040), px(360)).background(colors.bg, RoundedCornerShape(Ink.radius24)) else Modifier,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(time, style = InkType.screensaver, color = colors.text, maxLines = 1)
                    if (profile.showDate) Text(date, style = InkType.section, color = colors.muted, maxLines = 1)
                }
            }
        }
    }
}
