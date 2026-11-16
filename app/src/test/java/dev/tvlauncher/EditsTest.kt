package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EditsTest {
    private val base = Profile("p", "P")

    @Test
    fun withAppCreatesTheCategoryOnce() {
        val p = base.withApp("a", "Video").withApp("b", "Video").withApp("a", "Video")
        assertEquals(listOf(Category("Video", listOf("a", "b"))), p.categories)
    }

    @Test
    fun withoutAppDropsEmptyCategoriesAndKeepsOthers() {
        val p = base.withApp("a", "Video").withApp("a", "Music").withApp("b", "Music").withoutApp("a")
        assertEquals(listOf(Category("Music", listOf("b"))), p.categories)
        assertTrue(!p.inAnyCategory("a"))
        assertTrue(p.inAnyCategory("b"))
    }

    @Test
    fun datePatternCyclesAndWrapsAndRecoversFromCustom() {
        var p = base.copy(datePattern = DATE_PRESETS.last())
        p = p.nextDatePattern()
        assertEquals(DATE_PRESETS.first(), p.datePattern)
        assertEquals(DATE_PRESETS[0], base.copy(datePattern = "custom").nextDatePattern().datePattern)
    }

    @Test
    fun wallpaperCyclesThroughPresetsAndOnlyOffersTheImageWhenItExists() {
        val ids = WALLPAPER_PRESETS.map { it.id }
        var p = base.copy(wallpaper = ids.last())
        assertEquals(ids.first(), p.nextWallpaper(imageAvailable = false).wallpaper)
        assertEquals(WALLPAPER_IMAGE, p.nextWallpaper(imageAvailable = true).wallpaper)
        p = base.copy(wallpaper = WALLPAPER_IMAGE)
        assertEquals(ids.first(), p.nextWallpaper(imageAvailable = true).wallpaper)
    }

    @Test
    fun missingImageOrUnknownWallpaperFallsBackToTheFirstPreset() {
        assertEquals(WALLPAPER_PRESETS.first(), wallpaperPreset("gone"))
        assertEquals(WALLPAPER_PRESETS.first().id, base.copy(wallpaper = WALLPAPER_IMAGE).nextWallpaper(false).wallpaper)
    }

    @Test
    fun addProfileMakesAUniqueActiveProfile() {
        val config = Config(profiles = listOf(Profile("default", "Default"), Profile("p3", "Taken")))
        val added = config.addProfile()
        assertEquals(3, added.profiles.size)
        assertEquals(added.profiles.map { it.id }.toSet().size, added.profiles.size)
        assertEquals(added.profiles.last().id, added.activeProfileId)
    }

    @Test
    fun removeActiveProfileKeepsAtLeastOne() {
        val single = Config()
        assertSame(single, single.removeActiveProfile())
        val two = Config(activeProfileId = "b", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        val removed = two.removeActiveProfile()
        assertEquals(listOf("a"), removed.profiles.map { it.id })
        assertEquals("a", removed.activeProfileId)
    }

    @Test
    fun switchToNextProfileWraps() {
        val config = Config(activeProfileId = "b", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        assertEquals("a", config.switchToNextProfile().activeProfileId)
        assertEquals("b", config.switchToNextProfile().switchToNextProfile().activeProfileId)
    }

    @Test
    fun toggleFavoriteAddsThenRemoves() {
        val added = base.toggleFavorite("a").toggleFavorite("b")
        assertEquals(listOf("a", "b"), added.favorites)
        assertTrue(added.isFavorite("a"))
        val removed = added.toggleFavorite("a")
        assertEquals(listOf("b"), removed.favorites)
        assertFalse(removed.isFavorite("a"))
    }

    @Test
    fun movedShiftsWithinBoundsAndIgnoresUnknownApps() {
        val list = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), list.moved("a", 1))
        assertEquals(listOf("a", "c", "b"), list.moved("c", -1))
        assertEquals(list, list.moved("a", -1))
        assertEquals(list, list.moved("c", 1))
        assertEquals(list, list.moved("zzz", 1))
        assertEquals(listOf("b", "c", "a"), list.moved("a", 5))
    }

    @Test
    fun moveInCategoryOnlyTouchesThatCategory() {
        val p = base.withApp("a", "X").withApp("b", "X").withApp("a", "Y").withApp("b", "Y")
        val moved = p.moveInCategory("a", "X", 1)
        assertEquals(listOf("b", "a"), moved.categories.first { it.name == "X" }.packages)
        assertEquals(listOf("a", "b"), moved.categories.first { it.name == "Y" }.packages)
    }

    @Test
    fun renameTrimsCapsAndBlankClears() {
        val renamed = base.rename("a", "  My App  ")
        assertEquals("My App", renamed.labelFor("a", "Original"))
        assertEquals("Original", renamed.rename("a", "   ").labelFor("a", "Original"))
        assertEquals(MAX_NAME_LENGTH, base.rename("a", "x".repeat(100)).renames.getValue("a").length)
    }

    @Test
    fun recordLaunchCountsAndKeepsRecentUniqueNewestFirstAndCapped() {
        var p = base
        repeat(3) { p = p.recordLaunch("a") }
        p = p.recordLaunch("b").recordLaunch("a")
        assertEquals(4, p.launchCounts.getValue("a"))
        assertEquals(listOf("a", "b"), p.recent)
        (1..20).forEach { p = p.recordLaunch("x$it") }
        assertEquals(RECENT_LIMIT, p.recent.size)
        assertEquals("x20", p.recent.first())
    }

    @Test
    fun unhideRemovesOnlyThatApp() {
        val p = base.copy(hidden = setOf("a", "b")).unhide("a")
        assertEquals(setOf("b"), p.hidden)
    }

    @Test
    fun focusStyleCyclesAndWraps() {
        var config = Config()
        val seen = (1..FOCUS_STYLES.size).map { config = config.nextFocusStyle(); config.focusStyle }
        assertEquals(FOCUS_STYLES.drop(1) + FOCUS_STYLES.first(), seen)
    }

    @Test
    fun noCategoryPresetCollidesWithASmartRowTitle() {
        val smartTitles = setOf("Favorites", "Recently used", "Most used", "Apps", "Other apps")
        assertTrue(CATEGORY_PRESETS.none { it in smartTitles })
    }
}
