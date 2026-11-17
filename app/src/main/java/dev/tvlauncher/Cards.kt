package dev.tvlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text

/** The banner when the app has one (16:9, fills the slot), otherwise its square icon. */
private class Artwork(val image: ImageBitmap, val isBanner: Boolean)

private fun loadArtwork(repo: AppRepository, app: AppEntry): Artwork? {
    repo.banner(app)?.let { return Artwork(it.toBitmap(320, 180).asImageBitmap(), true) }
    repo.icon(app)?.let { return Artwork(it.toBitmap(160, 160).asImageBitmap(), false) }
    return null
}

/**
 * One app: a 244 x 137 px artwork slot (banner, or a 137 px square icon centered in it) with a two-line
 * name below. Focus draws the outline inside the slot and brightens the name. [onFocus] reports the
 * focus change and whether the name was cut off (the Home subtitle then shows the full name).
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppCard(
    repo: AppRepository,
    app: AppEntry,
    label: String,
    favorite: Boolean,
    style: String,
    onOpen: () -> Unit,
    onMenu: () -> Unit,
    onFocus: (focused: Boolean, nameCutOff: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val art = remember(app.packageName) { loadArtwork(repo, app) }
    var cutOff by remember { mutableStateOf(false) }
    val c = Ink.colors
    var focused by remember { mutableStateOf(false) }
    Column(Modifier.width(Ink.artWidth), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
        InkSurface(
            onClick = onOpen,
            onLongClick = onMenu,
            modifier = modifier.size(Ink.artWidth, Ink.artHeight),
            style = style,
            pressedFill = c.panel,
            pressedScale = 0.95f,
            onFocusChange = {
                focused = it
                onFocus(it, cutOff)
            },
        ) { _ ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                art?.let {
                    if (it.isBanner) {
                        Image(it.image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Image(it.image, null, Modifier.size(Ink.artHeight), contentScale = ContentScale.Fit)
                    }
                }
                if (favorite) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = px(10), end = px(10))
                            .size(px(24))
                            .background(Color.Black, RoundedCornerShape(Ink.radius8)),
                    ) { InkIcon(R.drawable.ic_star, size = px(24)) }
                }
            }
        }
        Text(
            label,
            style = InkType.label,
            color = if (focused) c.text else c.muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            onTextLayout = { cutOff = it.hasVisualOverflow },
            modifier = Modifier.width(Ink.artWidth).height(px(60)),
        )
    }
}

/** HDMI or other TV input: 310 x 160 px, icon and name on top, status below. */
@Composable
fun InputTile(title: String, status: String, style: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Ink.colors
    InkSurface(onClick = onClick, modifier = modifier.size(px(310), px(160)), style = style) { _ ->
        Column(Modifier.fillMaxSize().padding(px(20)), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InkIcon(R.drawable.ic_monitor, size = px(32))
                Spacer(Modifier.width(Ink.gap12))
                Text(title, style = InkType.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(status, style = InkType.body, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun quickIcon(label: String): Int = when (label) {
    "Sleep timer" -> R.drawable.ic_clock
    "Picture" -> R.drawable.ic_image
    "Sound" -> R.drawable.ic_volume_2
    "Bravia Sync" -> R.drawable.ic_tv
    "Notifications" -> R.drawable.ic_bell
    "Wi-Fi" -> R.drawable.ic_wifi
    else -> R.drawable.ic_settings
}

/** A shortcut to a TV settings screen: 204 x 148 px, icon above a two-line label. */
@Composable
fun QuickTileCard(label: String, style: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    InkSurface(onClick = onClick, modifier = modifier.size(px(204), px(148)), style = style) { _ ->
        Column(Modifier.fillMaxSize().padding(px(16)), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
            InkIcon(quickIcon(label), size = px(36))
            Text(label, style = InkType.label, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
