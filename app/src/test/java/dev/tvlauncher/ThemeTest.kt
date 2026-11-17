package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {
    @Test
    fun contrastMatchesTheFigmaStyleSheet() {
        // The style sheet quotes these ratios measured on the panel color (cards, menus), the tightest
        // surface; the palette must keep meeting them.
        assertEquals(13.7, contrastRatio(InkHex.DARK_TEXT, InkHex.DARK_PANEL), 0.2)
        assertEquals(7.5, contrastRatio(InkHex.DARK_MUTED, InkHex.DARK_PANEL), 0.2)
        assertEquals(9.6, contrastRatio(InkHex.DIM_TEXT, InkHex.DIM_PANEL), 0.2)
    }

    @Test
    fun everyTextColorMeetsAaOnItsBackgroundAndPanel() {
        val pairs = listOf(
            InkHex.DARK_TEXT to InkHex.DARK_BG, InkHex.DARK_TEXT to InkHex.DARK_PANEL,
            InkHex.DARK_MUTED to InkHex.DARK_BG, InkHex.DARK_MUTED to InkHex.DARK_PANEL,
            InkHex.DIM_TEXT to InkHex.DIM_BG, InkHex.DIM_TEXT to InkHex.DIM_PANEL,
            InkHex.DIM_MUTED to InkHex.DIM_BG, InkHex.DIM_MUTED to InkHex.DIM_PANEL,
        )
        for ((text, background) in pairs) {
            val ratio = contrastRatio(text, background)
            assertTrue("%08X on %08X is only %.2f".format(text, background, ratio), ratio >= 4.5)
        }
    }

    @Test
    fun focusOutlineIsClearlyVisibleAgainstEverySurfaceItSitsOn() {
        // Non-text UI parts need 3:1.
        for (surface in listOf(InkHex.DARK_BG, InkHex.DARK_PANEL)) {
            assertTrue(contrastRatio(InkHex.DARK_FOCUS, surface) >= 3.0)
        }
        for (surface in listOf(InkHex.DIM_BG, InkHex.DIM_PANEL)) {
            assertTrue(contrastRatio(InkHex.DIM_FOCUS, surface) >= 3.0)
        }
    }

    @Test
    fun contrastIsSymmetricAndBlackOnWhiteIs21() {
        assertEquals(21.0, contrastRatio(0xFFFFFFFFL, 0xFF000000L), 0.001)
        assertEquals(contrastRatio(InkHex.DARK_TEXT, InkHex.DARK_BG), contrastRatio(InkHex.DARK_BG, InkHex.DARK_TEXT), 1e-9)
    }
}
