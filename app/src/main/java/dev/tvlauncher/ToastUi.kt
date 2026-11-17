package dev.tvlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text

/** A short confirmation. [id] makes two identical messages in a row restart the 3 second timer. */
data class ToastMsg(val text: String, val error: Boolean = false, val id: Long = System.nanoTime())

const val TOAST_MILLIS = 3000L

/** 760 x 88 px toast near the bottom: icon and text, never focusable, so it never steals focus. */
@Composable
fun ToastHost(toast: ToastMsg?) {
    if (toast == null) return
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Row(
            Modifier
                .padding(bottom = px(78))
                .size(px(760), px(88))
                .background(Ink.colors.raised, RoundedCornerShape(Ink.radius8))
                .padding(px(24)),
            horizontalArrangement = Arrangement.spacedBy(Ink.gap16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InkIcon(if (toast.error) R.drawable.ic_x else R.drawable.ic_check, size = px(32))
            Text(toast.text, style = InkType.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
