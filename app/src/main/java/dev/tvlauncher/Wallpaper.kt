package dev.tvlauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Legacy id: the single `wallpaper.jpg` of earlier versions. Still shown if that file exists. */
const val WALLPAPER_IMAGE = "image"

/** Image files from the launcher's folder are stored as `file:<name>`. */
const val FILE_PREFIX = "file:"

enum class WallpaperKind { Ink, Raised, Dusk, Night, Photo }

data class WallpaperPreset(val id: String, val name: String, val kind: WallpaperKind, @DrawableRes val photo: Int = 0)

/** The six presets of the design, in order. */
val WALLPAPER_PRESETS = listOf(
    WallpaperPreset("ink", "Ink", WallpaperKind.Ink),
    WallpaperPreset("raised", "Raised Ink", WallpaperKind.Raised),
    WallpaperPreset("dusk", "Iris dusk", WallpaperKind.Dusk),
    WallpaperPreset("night", "Night gradient", WallpaperKind.Night),
    WallpaperPreset("alpine", "Alpine lake", WallpaperKind.Photo, R.drawable.alpine_lake),
    WallpaperPreset("nightsky", "Night sky", WallpaperKind.Photo, R.drawable.night_sky),
)

/** An unknown id (including the ids of older versions) falls back to Ink. */
fun wallpaperPreset(id: String): WallpaperPreset = WALLPAPER_PRESETS.firstOrNull { it.id == id } ?: WALLPAPER_PRESETS.first()

fun isFileWallpaper(id: String): Boolean = id == WALLPAPER_IMAGE || id.startsWith(FILE_PREFIX)

/** True for wallpapers that are pictures; Home puts a scrim and an opaque reading surface over those. */
fun isPhotoWallpaper(id: String): Boolean = isFileWallpaper(id) || wallpaperPreset(id).kind == WallpaperKind.Photo

fun fileWallpaperId(name: String): String = FILE_PREFIX + name

/** A file name is only accepted as a plain name inside the launcher's folder. */
fun isSafeImageName(name: String): Boolean =
    name.isNotEmpty() && '/' !in name && '\\' !in name && name != "." && name != ".." &&
        name.substringAfterLast('.', "").lowercase() in IMAGE_EXTENSIONS

val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png")

fun wallpaperName(id: String): String = when {
    id == WALLPAPER_IMAGE -> "wallpaper.jpg"
    id.startsWith(FILE_PREFIX) -> id.removePrefix(FILE_PREFIX)
    else -> wallpaperPreset(id).name
}

/** The folder the launcher can read images from; `adb push` works without any storage permission. */
fun imageFolder(context: Context): File? = context.getExternalFilesDir(null)

/** Image files in [imageFolder], by name. The backup file is not an image so it never shows up. */
fun listWallpaperFiles(context: Context): List<File> =
    imageFolder(context)?.listFiles()
        ?.filter { it.isFile && it.length() > 0 && isSafeImageName(it.name) }
        ?.sortedBy { it.name.lowercase() }
        .orEmpty()

fun wallpaperFile(context: Context, id: String): File? {
    val dir = imageFolder(context) ?: return null
    val name = when {
        id.startsWith(FILE_PREFIX) -> id.removePrefix(FILE_PREFIX)
        id == WALLPAPER_IMAGE -> listOf("wallpaper.jpg", "wallpaper.jpeg", "wallpaper.png").firstOrNull { File(dir, it).isFile } ?: return null
        else -> return null
    }
    if (!isSafeImageName(name)) return null
    return File(dir, name).takeIf { it.isFile && it.length() > 0 }
}

fun imageSize(file: File): Pair<Int, Int>? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    return if (bounds.outWidth > 0) bounds.outWidth to bounds.outHeight else null
}

/** Decoded no wider than needed, in RGB_565 (2 bytes per pixel) to keep memory low. */
fun decodeWallpaper(file: File, targetWidth: Int = 1920): Bitmap? {
    val size = imageSize(file) ?: return null
    var sample = 1
    while (size.first / (sample * 2) >= targetWidth) sample *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    return BitmapFactory.decodeFile(file.path, options)
}

/** How much of Home the wallpaper may show: none (plain Ink), or only the margins around a reading surface. */
enum class WallpaperSurface { Full, Reading, ScrimOnly }

internal val DUSK = Brush.horizontalGradient(listOf(Color(0xFFC2ABF5), Color(0xFF14111C)))
internal val NIGHT = Brush.verticalGradient(listOf(Color(0xFF3A2F55), Color(0xFF0B0910)))

/**
 * The profile's wallpaper. Pictures get a 70 percent black scrim; with [WallpaperSurface.Reading] an opaque
 * surface also covers the content area (x 416, y 54, 1408 x 972 design px) so text never sits on a photo.
 * Screens without a sidebar pass [WallpaperSurface.Full] and show plain Ink instead of a picture.
 */
@Composable
fun WallpaperBackground(profile: Profile, surface: WallpaperSurface = WallpaperSurface.Full) {
    WallpaperLayer(profile.wallpaper, surface)
}

@Composable
fun WallpaperLayer(id: String, surface: WallpaperSurface) {
    val context = LocalContext.current
    val c = Ink.colors
    val preset = wallpaperPreset(id)
    val photo = isPhotoWallpaper(id)
    val file: ImageBitmap? by produceState<ImageBitmap?>(null, id) {
        value = if (isFileWallpaper(id)) {
            withContext(Dispatchers.IO) { wallpaperFile(context, id)?.let { decodeWallpaper(it)?.asImageBitmap() } }
        } else {
            null
        }
    }
    Box(Modifier.fillMaxSize().background(c.bg)) {
        when {
            photo && surface == WallpaperSurface.Full -> Unit
            photo -> {
                if (preset.kind == WallpaperKind.Photo && !isFileWallpaper(id)) {
                    Image(painterResource(preset.photo), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    file?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                }
                Box(Modifier.fillMaxSize().background(Color(if (surface == WallpaperSurface.ScrimOnly) 0xCC000000 else 0xB3000000)))
                if (surface == WallpaperSurface.Reading) {
                    Box(Modifier.offset(px(416), px(54)).size(px(1408), px(972)).background(c.bg))
                }
            }
            preset.kind == WallpaperKind.Raised -> Box(Modifier.fillMaxSize().background(c.panel))
            preset.kind == WallpaperKind.Dusk -> Box(Modifier.fillMaxSize().background(DUSK))
            preset.kind == WallpaperKind.Night -> Box(Modifier.fillMaxSize().background(NIGHT))
        }
    }
}
