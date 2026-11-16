package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutTest {
    private val installed = listOf("a", "b", "c", "d")

    @Test
    fun noCategoriesGivesOneAppsRow() {
        val rows = buildRows(installed, Profile("p", "P"))
        assertEquals(listOf(RowSpec("Apps", installed, RowKind.Other)), rows)
    }

    @Test
    fun hiddenAppsNeverAppear() {
        val rows = buildRows(installed, Profile("p", "P", hidden = setOf("b")))
        assertEquals(listOf("a", "c", "d"), rows.single().packages)
    }

    @Test
    fun categorizedAppsLeaveTheOtherRow() {
        val profile = Profile("p", "P", categories = listOf(Category("Video", listOf("c", "a"))))
        val rows = buildRows(installed, profile)
        assertEquals(RowSpec("Video", listOf("c", "a"), RowKind.Category), rows[0])
        assertEquals(RowSpec("Other apps", listOf("b", "d"), RowKind.Other), rows[1])
    }

    @Test
    fun uninstalledAndHiddenMembersAreDroppedAndEmptyCategoriesSkipped() {
        val profile = Profile(
            "p", "P",
            hidden = setOf("a"),
            categories = listOf(Category("Gone", listOf("zzz")), Category("Mixed", listOf("a", "b", "zzz"))),
        )
        val rows = buildRows(installed, profile)
        assertEquals(listOf("Mixed", "Other apps"), rows.map { it.title })
        assertEquals(listOf("b"), rows[0].packages)
    }

    @Test
    fun otherRowCanBeTurnedOffButNeverLeavesTheScreenEmpty() {
        val withCat = Profile("p", "P", showOtherRow = false, categories = listOf(Category("V", listOf("a"))))
        assertEquals(listOf("V"), buildRows(installed, withCat).map { it.title })
        val noCat = Profile("p", "P", showOtherRow = false)
        assertEquals(listOf("Apps"), buildRows(installed, noCat).map { it.title })
    }

    @Test
    fun appInTwoCategoriesShowsInBoth() {
        val profile = Profile("p", "P", categories = listOf(Category("X", listOf("a")), Category("Y", listOf("a"))))
        val rows = buildRows(installed, profile)
        assertEquals(listOf("a"), rows[0].packages)
        assertEquals(listOf("a"), rows[1].packages)
        assertTrue(rows.last().packages.none { it == "a" })
    }

    @Test
    fun smartRowsComeFirstInOrderAndKeepAppsInOtherToo() {
        val profile = Profile(
            "p", "P",
            favorites = listOf("d", "a"),
            recent = listOf("c", "b"),
            launchCounts = mapOf("b" to 5, "a" to 2, "c" to 1),
            categories = listOf(Category("Video", listOf("a"))),
        )
        val rows = buildRows(installed, profile)
        assertEquals(listOf("Favorites", "Recently used", "Most used", "Video", "Other apps"), rows.map { it.title })
        assertEquals(listOf("d", "a"), rows[0].packages)
        assertEquals(listOf("c", "b"), rows[1].packages)
        // "c" has one launch, below the minimum, so only b (5) and a (2) qualify, most first.
        assertEquals(listOf("b", "a"), rows[2].packages)
        assertEquals(listOf("b", "c", "d"), rows[4].packages)
    }

    @Test
    fun smartRowsSkipHiddenUninstalledAndDisabledRows() {
        val profile = Profile(
            "p", "P",
            hidden = setOf("a"),
            favorites = listOf("a", "gone"),
            recent = listOf("a"),
            launchCounts = mapOf("a" to 9, "b" to 3),
            showMostUsed = false,
        )
        val titles = buildRows(installed, profile).map { it.title }
        assertEquals(listOf("Apps"), titles)
    }

    @Test
    fun mostUsedBreaksTiesByPackageNameAndCapsAtTen() {
        val many = (1..15).map { "app%02d".format(it) }
        val counts = many.associateWith { 3 }
        val rows = buildRows(many, Profile("p", "P", launchCounts = counts))
        val mostUsed = rows.first { it.kind == RowKind.MostUsed }
        assertEquals(10, mostUsed.packages.size)
        assertEquals(many.take(10), mostUsed.packages)
    }
}
