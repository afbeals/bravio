package dev.tvlauncher

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/** Whether the active profile wants a click when focus moves (see [Profile.focusSound]). */
val LocalFocusSound = staticCompositionLocalOf { false }

/** Focus-style presets from the design: outline (default), scale 1.04 plus outline, glow plus outline. */
fun focusedScaleFor(style: String): Float = if (style == "scale") 1.04f else 1f

/**
 * Every focusable thing in the design: a rounded box that gets a 6 px outline inside its bounds when
 * focused and a 4 px text-colored outline plus the raised fill while pressed. [content] receives whether
 * the box has focus so labels can change color.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun InkSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    style: String = "outline",
    fill: Color = Ink.colors.panel,
    pressedFill: Color = Ink.colors.raised,
    focusedOutline: Color = Ink.colors.focus,
    pressedScale: Float = 1f,
    onFocusChange: (Boolean) -> Unit = {},
    content: @Composable BoxScope.(focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val view = LocalView.current
    val sound = LocalFocusSound.current
    val shape = RoundedCornerShape(Ink.radius8)
    val c = Ink.colors
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.onFocusChanged {
            if (focused != it.isFocused) {
                focused = it.isFocused
                if (focused && sound) view.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT)
                onFocusChange(focused)
            }
        },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = fill,
            contentColor = c.text,
            focusedContainerColor = fill,
            focusedContentColor = c.text,
            pressedContainerColor = pressedFill,
            pressedContentColor = c.text,
        ),
        scale = ClickableSurfaceDefaults.scale(
            scale = 1f,
            focusedScale = focusedScaleFor(style),
            pressedScale = pressedScale,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(Ink.focusWidth, focusedOutline), shape = shape),
            pressedBorder = Border(BorderStroke(px(4), c.text), shape = shape),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = if (style == "glow") Glow(c.focus.copy(alpha = 0.32f), 8.dp) else Glow.None,
        ),
    ) {
        content(focused)
    }
}

/** 288 x 72 px button: primary (iris fill) or secondary (panel fill). */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    style: String = "outline",
) {
    val c = Ink.colors
    InkSurface(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier.size(px(288), px(72)),
        style = style,
        fill = if (primary) c.focus else c.panel,
        focusedOutline = if (primary) c.text else c.focus,
    ) { _ ->
        Box(Modifier.size(px(288), px(72)), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = InkType.label,
                color = if (primary) c.onFocus else c.text,
                maxLines = 1,
                modifier = if (enabled) Modifier else Modifier.alpha(0.45f),
            )
        }
    }
}

@Composable
fun InkIcon(@DrawableRes id: Int, modifier: Modifier = Modifier, size: Dp = px(28), tint: Color = Ink.colors.text) {
    Icon(painterResource(id), contentDescription = null, modifier = modifier.size(size), tint = tint)
}
