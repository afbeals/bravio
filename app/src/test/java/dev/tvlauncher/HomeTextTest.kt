package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeTextTest {
    private fun apps(n: Int, kind: RowKind = RowKind.Category) = RowSpec("Row", List(n) { "p$it" }, kind)

    @Test
    fun shortRowsCountTheirAppsWithTheRightPlural() {
        assertEquals("5 apps", rowCounter(apps(5), 0, focusedIndex = 2, profileName = "Allan"))
        assertEquals("1 app", rowCounter(apps(1), 0, focusedIndex = null, profileName = "Allan"))
    }

    @Test
    fun longRowsShowTheFocusedPositionOnlyWhileFocused() {
        assertEquals("1 / 42", rowCounter(apps(42), 0, focusedIndex = 0, profileName = "Allan"))
        assertEquals("42 / 42", rowCounter(apps(42), 0, focusedIndex = 41, profileName = "Allan"))
        assertEquals("42 apps", rowCounter(apps(42), 0, focusedIndex = null, profileName = "Allan"))
    }

    @Test
    fun smartAndTileRowsUseTheirOwnWording() {
        assertEquals("On this TV", rowCounter(apps(3, RowKind.MostUsed), 0, null, "Allan"))
        assertEquals("Allan", rowCounter(apps(3, RowKind.Recent), 0, null, "Allan"))
        assertEquals("4 inputs", rowCounter(apps(0, RowKind.Inputs), 4, null, "Allan"))
        assertEquals("1 control", rowCounter(apps(0, RowKind.Quick), 1, null, "Allan"))
    }

    @Test
    fun subtitleShowsTheFullNameOnlyWhenTheCardCutItOff() {
        assertEquals("Allan’s apps, exactly where you left them.", homeSubtitle("Allan", null))
        assertEquals("Focused: A very long name", homeSubtitle("Allan", "A very long name"))
    }
}
