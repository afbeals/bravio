package dev.tvlauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val WALLPAPER_IMAGE = "image"

data class WallpaperPreset(val id: String, val name: String, val top: Long, val bottom: Long)

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset("midnight", "Midnight", 0xFF141A21, 0xFF090C10),
    WallpaperPreset("ocean", "Ocean", 0xFF0F3550, 0xFF050F18),
    WallpaperPreset("dusk", "Dusk", 0xFF38234F, 0xFF0C0812),
    WallpaperPreset("forest", "Forest", 0xFF16402A, 0xFF07130C),
    WallpaperPreset("charcoal", "Charcoal", 0xFF303036, 0xFF0E0E10),
)

fun wallpaperPreset(id: String): WallpaperPreset = WALLPAPER_PRESETS.firstOrNull { it.id == id } ?: WALLPAPER_PRESETS.first()

fun wallpaperName(id: String): String = if (id == WALLPAPER_IMAGE) "Your image" else wallpaperPreset(id).name

/** Next wallpaper in the cycle. "Your image" is only offered while an image file exists. */
fun Profile.nextWallpaper(imageAvailable: Boolean): Profile {
    val ids = WALLPAPER_PRESETS.map { it.id } + if (imageAvailable) listOf(WALLPAPER_IMAGE) else emptyList()
    val next = (ids.indexOf(wallpaper) + 1) % ids.size
    return copy(wallpaper = ids[next])
}

/** App-specific external storage: `adb push` works without any storage permission. */
fun wallpaperImageFile(context: Context): File? {
    val dir = context.getExternalFilesDir(null) ?: return null
    return listOf("wallpaper.jpg", "wallpaper.jpeg", "wallpaper.png")
        .map { File(dir, it) }
        .firstOrNull { it.isFile && it.length() > 0 }
}

/** Decoded at screen size in RGB_565 (2 bytes per pixel) to keep memory low. */
fun decodeWallpaper(file: File, targetWidth: Int = 1920): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= targetWidth) sample *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    return BitmapFactory.decodeFile(file.path, options)
}

@Composable
fun WallpaperBackground(profile: Profile) {
    val context = LocalContext.current
    val preset = wallpaperPreset(profile.wallpaper)
    val image: ImageBitmap? by produceState<ImageBitmap?>(null, profile.wallpaper) {
        value = if (profile.wallpaper == WALLPAPER_IMAGE) {
            withContext(Dispatchers.IO) { wallpaperImageFile(context)?.let { decodeWallpaper(it)?.asImageBitmap() } }
        } else {
            null
        }
    }
    val dim = LocalDim.current
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(preset.top), Color(preset.bottom))))) {
        image?.let { bitmap ->
            Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            // Darkens the image so white text stays readable on a bright photo.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x59000000), Color(0xB3000000))),
                ),
            )
        }
        // Day/dim mode: a flat dark layer over whatever wallpaper is showing.
        if (dim) Box(Modifier.fillMaxSize().background(Color(0x8C000000)))
    }
}
