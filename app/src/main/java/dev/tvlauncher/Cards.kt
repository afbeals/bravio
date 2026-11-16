package dev.tvlauncher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Text

private fun focusedScale(style: String) = when (style) {
    "scale" -> 1.1f
    "glow" -> 1.05f
    else -> 1f
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppCard(
    repo: AppRepository,
    app: AppEntry,
    label: String,
    style: String,
    onOpen: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        onLongClick = onMenu,
        modifier = modifier,
        scale = CardDefaults.scale(focusedScale = focusedScale(style)),
        border = CardDefaults.border(
            focusedBorder = if (style == "outline") Border(BorderStroke(3.dp, Color.White)) else Border.None,
        ),
        glow = CardDefaults.glow(
            focusedGlow = if (style == "glow") Glow(Color(0x80FFFFFF), 14.dp) else Glow.None,
        ),
    ) {
        Column(Modifier.padding(16.dp).width(112.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val icon = remember(app.packageName) { repo.icon(app)?.toBitmap(96, 96) }
            if (icon != null) Image(icon.asImageBitmap(), null, Modifier.size(72.dp))
            Text(label, fontSize = 14.sp, maxLines = 1)
        }
    }
}

/** A plain text tile, used for HDMI inputs and quick settings. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TileCard(title: String, subtitle: String?, style: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        scale = CardDefaults.scale(focusedScale = focusedScale(style)),
        border = CardDefaults.border(
            focusedBorder = if (style == "outline") Border(BorderStroke(3.dp, Color.White)) else Border.None,
        ),
        glow = CardDefaults.glow(
            focusedGlow = if (style == "glow") Glow(Color(0x80FFFFFF), 14.dp) else Glow.None,
        ),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp).width(140.dp)) {
            Text(title, fontSize = 18.sp, maxLines = 1)
            if (subtitle != null) Text(subtitle, fontSize = 13.sp, maxLines = 1, color = Color(0xFFB0BEC5))
        }
    }
}
