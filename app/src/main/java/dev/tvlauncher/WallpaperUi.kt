package dev.tvlauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface WallpaperStage {
    data object Grid : WallpaperStage
    data object Files : WallpaperStage
    data class Preview(val id: String, val from: WallpaperStage) : WallpaperStage
}

/** The picture or color of a wallpaper, filling its box. */
@Composable
private fun WallpaperSwatch(id: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preset = wallpaperPreset(id)
    val file: ImageBitmap? by produceState<ImageBitmap?>(null, id) {
        value = if (isFileWallpaper(id)) {
            withContext(Dispatchers.IO) { wallpaperFile(context, id)?.let { decodeWallpaper(it, 480)?.asImageBitmap() } }
        } else {
            null
        }
    }
    val c = Ink.colors
    Box(modifier) {
        when {
            isFileWallpaper(id) -> {
                Box(Modifier.fillMaxSize().background(c.raised))
                file?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            preset.kind == WallpaperKind.Photo -> Image(painterResource(preset.photo), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            preset.kind == WallpaperKind.Raised -> Box(Modifier.fillMaxSize().background(c.panel))
            preset.kind == WallpaperKind.Dusk -> Box(Modifier.fillMaxSize().background(DUSK))
            preset.kind == WallpaperKind.Night -> Box(Modifier.fillMaxSize().background(NIGHT))
            else -> Box(Modifier.fillMaxSize().background(c.bg))
        }
    }
}

/** 360 x 244 px thumbnail: picture on top (outline when focused, check when selected), name below. */
@Composable
private fun WallpaperThumb(label: String, selected: Boolean, style: String, onClick: () -> Unit, modifier: Modifier = Modifier, swatch: @Composable () -> Unit) {
    Column(Modifier.width(px(360)), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
        InkSurface(onClick = onClick, modifier = modifier.size(px(360), px(196)), style = style) { _ ->
            Box(Modifier.fillMaxSize()) {
                swatch()
                if (selected) {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(top = px(16), end = px(16)).size(px(32)).background(Color.Black, RoundedCornerShape(Ink.radius8)),
                    ) { InkIcon(R.drawable.ic_check, size = px(32)) }
                }
            }
        }
        Text(label, style = InkType.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun WallpaperScreen(profile: Profile, style: String, onApply: (String) -> Unit, onClose: () -> Unit) {
    var stage by remember { mutableStateOf<WallpaperStage>(WallpaperStage.Grid) }
    BackHandler(enabled = true) {
        when (val s = stage) {
            WallpaperStage.Grid -> onClose()
            WallpaperStage.Files -> stage = WallpaperStage.Grid
            is WallpaperStage.Preview -> stage = s.from
        }
    }
    when (val s = stage) {
        WallpaperStage.Grid -> WallpaperGrid(profile, style, onPick = { stage = WallpaperStage.Preview(it, WallpaperStage.Grid) }, onFiles = { stage = WallpaperStage.Files }, onDone = onClose)
        WallpaperStage.Files -> WallpaperFiles(
            profile, style,
            onPick = { name -> stage = WallpaperStage.Preview(fileWallpaperId(name), WallpaperStage.Files) },
            onCancel = { stage = WallpaperStage.Grid },
        )
        is WallpaperStage.Preview -> WallpaperPreview(s.id, profile, style, onApply = { onApply(s.id) }, onCancel = { stage = s.from })
    }
}

@Composable
private fun Frame(profile: Profile, title: String, subtitle: String, hint: String, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(start = Ink.safeHorizontal, top = px(64), end = Ink.safeHorizontal)) {
            Text(title, style = InkType.title)
            Spacer(Modifier.height(px(8)))
            Text(subtitle, style = InkType.body, color = Ink.colors.muted)
            Spacer(Modifier.height(px(28)))
            content()
        }
        Text(hint, style = InkType.body, color = Ink.colors.muted, modifier = Modifier.padding(start = Ink.safeHorizontal, top = px(986)))
    }
}

@Composable
private fun WallpaperGrid(profile: Profile, style: String, onPick: (String) -> Unit, onFiles: () -> Unit, onDone: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    Frame(profile, "Wallpaper", "${profile.name} · Focus to preview, OK to select", "OK  Select wallpaper     Back  Return") {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(px(1192)).height(px(560)),
            horizontalArrangement = Arrangement.spacedBy(px(32)),
            verticalArrangement = Arrangement.spacedBy(px(32)),
        ) {
            items(WALLPAPER_PRESETS, key = { it.id }) { preset ->
                WallpaperThumb(
                    preset.name, selected = profile.wallpaper == preset.id, style = style,
                    onClick = { onPick(preset.id) },
                    modifier = if (preset === WALLPAPER_PRESETS.first()) Modifier.focusRequester(first) else Modifier,
                ) { WallpaperSwatch(preset.id, Modifier.fillMaxSize()) }
            }
            item(key = "storage") {
                WallpaperThumb("From TV storage", selected = isFileWallpaper(profile.wallpaper), style = style, onClick = onFiles) {
                    Box(Modifier.fillMaxSize().background(Ink.colors.raised), contentAlignment = Alignment.Center) {
                        InkIcon(R.drawable.ic_hard_drive, size = px(64), tint = Ink.colors.focus)
                    }
                }
            }
        }
        Spacer(Modifier.height(px(46)))
        InkButton("Done", onDone, style = style)
    }
}

@Composable
private fun WallpaperFiles(profile: Profile, style: String, onPick: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val files = remember { listWallpaperFiles(context) }
    val sizes = remember(files) { files.associate { it.name to imageSize(it) } }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    val folder = imageFolder(context)?.path ?: "the launcher folder"
    Frame(profile, "Choose an image", "TV storage · ${countOf(files.size, "image", "images")}", "OK  Select     Back  Return") {
        if (files.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Ink.gap24), modifier = Modifier.width(px(1000))) {
                Text("No images found", style = InkType.section)
                Text(
                    "Copy a .jpg or .png file to $folder, for example with: adb push photo.jpg $folder/",
                    style = InkType.body, color = Ink.colors.muted,
                )
                InkButton("Cancel", onCancel, Modifier.focusRequester(first), primary = true, style = style)
            }
        } else {
            LazyColumn(Modifier.width(px(1000)).height(px(740)), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
                items(files, key = { it.name }) { file ->
                    val size = sizes[file.name]
                    ListRow(
                        file.name, size?.let { "${it.first} × ${it.second}" } ?: "", style, { onPick(file.name) },
                        modifier = if (file === files.first()) Modifier.focusRequester(first) else Modifier,
                    )
                }
                item(key = "cancel") { InkButton("Cancel", onCancel, style = style) }
            }
        }
    }
}

/** A mock of Home over the candidate wallpaper, so the scrim and reading surface can be judged before applying. */
@Composable
private fun WallpaperPreview(id: String, profile: Profile, style: String, onApply: () -> Unit, onCancel: () -> Unit) {
    val apply = remember { FocusRequester() }
    LaunchedEffect(Unit) { apply.requestFocusWhenReady() }
    val c = Ink.colors
    Box(Modifier.fillMaxSize()) {
        WallpaperLayer(id, WallpaperSurface.Reading)
        Box(Modifier.offset(Ink.safeHorizontal, Ink.safeVertical).size(px(296), px(972)).background(c.sidebar, RoundedCornerShape(Ink.radius16))) {
            Text("LIBRARY", style = InkType.section, color = c.focus, modifier = Modifier.padding(px(24)))
        }
        Column(Modifier.offset(px(448), px(64))) {
            Text("Your library", style = InkType.title)
            Spacer(Modifier.height(px(8)))
            Text("${profile.name}’s apps, exactly where you left them.", style = InkType.body, color = c.muted)
            Spacer(Modifier.height(px(64)))
            Text("Favorites", style = InkType.section)
            Spacer(Modifier.height(px(12)))
            Row(horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                repeat(5) { Box(Modifier.size(Ink.artWidth, Ink.artHeight).background(c.panel, RoundedCornerShape(Ink.radius8))) }
            }
        }
        Row(
            Modifier.offset(px(448), px(814)).size(px(1336), px(124)).background(c.panel, RoundedCornerShape(Ink.radius8)).padding(px(24)),
            horizontalArrangement = Arrangement.spacedBy(Ink.gap24),
        ) {
            InkButton("Apply wallpaper", onApply, Modifier.focusRequester(apply), primary = true, style = style)
            InkButton("Cancel", onCancel, style = style)
        }
        Text("OK  Select     Back  Cancel", style = InkType.body, color = c.muted, modifier = Modifier.padding(start = px(448), top = px(986)))
    }
}
