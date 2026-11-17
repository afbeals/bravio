package dev.tvlauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/*
 * Ink & Iris design tokens, from the Figma style sheet. The design is drawn at 1920 x 1080 px and the TV
 * runs at 2x density (960 x 540 dp), so every design pixel is half a dp. Use [px] and [pxSp] with the
 * numbers exactly as written in the design.
 */

/** Design pixels to dp: the TV is 2x, so 96 px is 48 dp. */
fun px(designPixels: Int): Dp = (designPixels / 2f).dp

/** Design pixels to sp (same 2x rule). */
fun pxSp(designPixels: Float): TextUnit = (designPixels / 2f).sp

/** Raw ARGB values, kept as numbers so they can be checked in plain unit tests. */
object InkHex {
    const val DARK_BG = 0xFF14111CL
    const val DARK_PANEL = 0xFF282234L
    const val DARK_TEXT = 0xFFF4F0FAL
    const val DARK_MUTED = 0xFFBDB0D0L
    const val DARK_FOCUS = 0xFFC2ACF4L
    const val DARK_RAISED = 0xFF332C42L

    const val DIM_BG = 0xFF0B0910L
    const val DIM_PANEL = 0xFF1B1725L
    const val DIM_TEXT = 0xFFC6BBD7L
    const val DIM_MUTED = 0xFFB2A7C4L
    const val DIM_FOCUS = 0xFF9E8DC5L
    const val DIM_RAISED = 0xFF251F32L

    const val SIDEBAR = 0xFF000000L
}

@Immutable
data class InkColors(
    val bg: Color,
    val panel: Color,
    val text: Color,
    val muted: Color,
    val focus: Color,
    val sidebar: Color,
    val raised: Color,
    /** Text and thumb on a focus-colored fill (same hex as bg). */
    val onFocus: Color,
)

val DarkInk = InkColors(
    Color(InkHex.DARK_BG), Color(InkHex.DARK_PANEL), Color(InkHex.DARK_TEXT),
    Color(InkHex.DARK_MUTED), Color(InkHex.DARK_FOCUS), Color(InkHex.SIDEBAR),
    Color(InkHex.DARK_RAISED), Color(InkHex.DARK_BG),
)

val DimInk = InkColors(
    Color(InkHex.DIM_BG), Color(InkHex.DIM_PANEL), Color(InkHex.DIM_TEXT),
    Color(InkHex.DIM_MUTED), Color(InkHex.DIM_FOCUS), Color(InkHex.SIDEBAR),
    Color(InkHex.DIM_RAISED), Color(InkHex.DIM_BG),
)

val LocalInkColors = staticCompositionLocalOf { DarkInk }

/** One bundled variable font; each weight is a variation of the same file (Android 8+). */
@OptIn(ExperimentalTextApi::class)
val InterFamily = FontFamily(
    listOf(
        FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold,
    ).map { weight ->
        Font(
            resId = R.font.inter,
            weight = weight,
            style = FontStyle.Normal,
            variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
        )
    },
)

object Ink {
    val colors: InkColors
        @Composable @ReadOnlyComposable get() = LocalInkColors.current

    // Spacing: 8 px base, gaps 12 / 16 / 24 / 32 / 48 px.
    val gap8 = px(8)
    val gap12 = px(12)
    val gap16 = px(16)
    val gap24 = px(24)
    val gap32 = px(32)
    val gap48 = px(48)

    // Safe area: 96 px left and right, 54 px top and bottom.
    val safeHorizontal = px(96)
    val safeVertical = px(54)

    // Shape: 8 px cards, 16 px panels, 24 px avatars. Focus is a 6 px outline drawn inside the card.
    val radius8 = px(8)
    val radius16 = px(16)
    val radius24 = px(24)
    val focusWidth = px(6)

    // Artwork slot: 244 x 137 px (16:9). A square icon is 137 x 137 px, centered in the same 244 px slot.
    val artWidth = px(244)
    val artHeight = px(137)

    // Motion: focus changes take 120 ms; menus and wallpaper changes 150 ms.
    const val FOCUS_MILLIS = 120
    const val PANEL_MILLIS = 150
}

/** Text styles from the style sheet, in 1080p design pixels (Inter, line height 1.25). */
object InkType {
    private fun style(size: Float, weight: FontWeight) = TextStyle(
        fontFamily = InterFamily,
        fontWeight = weight,
        fontSize = pxSp(size),
        lineHeight = pxSp(size * 1.25f),
        letterSpacing = 0.sp,
    )

    val title = style(48f, FontWeight.SemiBold)
    val section = style(30f, FontWeight.SemiBold)
    val label = style(24f, FontWeight.Medium)
    val body = style(24f, FontWeight.Normal)
    val meta = style(20f, FontWeight.Normal)
    val clock = style(64f, FontWeight.Light)
    val screensaver = style(180f, FontWeight.Light)
}

/** Applies the palette (dark, or dim when the light sensor says so) and Inter to everything below it. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun InkTheme(dim: Boolean, content: @Composable () -> Unit) {
    val c = if (dim) DimInk else DarkInk
    val scheme = darkColorScheme(
        primary = c.focus,
        onPrimary = c.bg,
        secondary = c.muted,
        background = c.bg,
        onBackground = c.text,
        surface = c.panel,
        onSurface = c.text,
        surfaceVariant = c.panel,
        onSurfaceVariant = c.muted,
        border = c.focus,
    )
    CompositionLocalProvider(LocalInkColors provides c) {
        MaterialTheme(colorScheme = scheme) {
            CompositionLocalProvider(
                LocalContentColor provides c.text,
                LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(fontFamily = InterFamily, color = c.text)),
            ) {
                content()
            }
        }
    }
}

/** WCAG contrast ratio between two ARGB colors (alpha ignored), used to keep the palette readable. */
fun contrastRatio(a: Long, b: Long): Double {
    fun channel(value: Long): Double {
        val v = value / 255.0
        return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
    }

    fun luminance(argb: Long): Double {
        val r = channel((argb shr 16) and 0xFF)
        val g = channel((argb shr 8) and 0xFF)
        val b = channel(argb and 0xFF)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    val la = luminance(a)
    val lb = luminance(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}
