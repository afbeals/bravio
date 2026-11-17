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
    fun wallpaperPresetsMatchTheDesignAndUnknownIdsFallBackToInk() {
        assertEquals(
            listOf("Ink", "Raised Ink", "Iris dusk", "Night gradient", "Alpine lake", "Night sky"),
            WALLPAPER_PRESETS.map { it.name },
        )
        assertEquals(WALLPAPER_PRESETS.first(), wallpaperPreset("midnight"))
        assertEquals("Alpine lake", wallpaperName("alpine"))
    }

    @Test
    fun onlyPicturesGetTheScrimAndReadingSurface() {
        assertTrue(isPhotoWallpaper("alpine"))
        assertTrue(isPhotoWallpaper(fileWallpaperId("holiday.jpg")))
        assertTrue(isPhotoWallpaper(WALLPAPER_IMAGE))
        assertFalse(isPhotoWallpaper("ink"))
        assertFalse(isPhotoWallpaper("dusk"))
    }

    @Test
    fun imageNamesMustBePlainNamesOfImageFiles() {
        assertTrue(isSafeImageName("Family photo.PNG"))
        assertFalse(isSafeImageName("../config.json"))
        assertFalse(isSafeImageName("a/b.jpg"))
        assertFalse(isSafeImageName("notes.txt"))
        assertFalse(isSafeImageName(".."))
        assertEquals("holiday.jpg", wallpaperName(fileWallpaperId("holiday.jpg")))
    }

    @Test
    fun addProfileMakesAnActiveProfileWithTheGivenNameAndAUniqueId() {
        val config = Config(profiles = listOf(Profile("default", "Default"), Profile("p3", "Taken")))
        val added = config.addProfile("  Movie night ")
        assertEquals(3, added.profiles.size)
        assertEquals(added.profiles.map { it.id }.toSet().size, added.profiles.size)
        assertEquals("Movie night", added.activeProfile.name)
        assertEquals(added.profiles.last().id, added.activeProfileId)
    }

    @Test
    fun addProfileRefusesBlankDuplicateAndPastTheSixProfileLimit() {
        val config = Config(profiles = listOf(Profile("default", "Default")))
        assertSame(config, config.addProfile("   "))
        assertSame(config, config.addProfile("default"))
        var full = config
        (2..MAX_PROFILES).forEach { full = full.addProfile("P$it") }
        assertEquals(MAX_PROFILES, full.profiles.size)
        assertFalse(full.canAddProfile())
        assertSame(full, full.addProfile("One too many"))
    }

    @Test
    fun profileNameValidationNamesTheProblem() {
        assertEquals(NameProblem.Empty, validateProfileName("  ", emptyList()))
        assertEquals(NameProblem.Taken, validateProfileName("kids", listOf("Allan", "Kids")))
        assertEquals(null, validateProfileName("Guest", listOf("Allan", "Kids")))
    }

    @Test
    fun suggestedProfileNamesSkipOnesAlreadyInUse() {
        assertEquals("Profile 2", Config().suggestProfileName())
        val config = Config(profiles = listOf(Profile("default", "Default"), Profile("a", "Profile 2")))
        assertEquals("Profile 3", config.suggestProfileName())
    }

    @Test
    fun renamingToAnotherProfilesNameIsIgnored() {
        val config = Config(activeProfileId = "b", profiles = listOf(Profile("a", "Kids"), Profile("b", "B")))
        assertSame(config, config.renameActiveProfile("kids"))
        assertEquals("B2", config.renameActiveProfile("B2").activeProfile.name)
        // Keeping its own name (for example only changing the capitalisation) is allowed.
        assertEquals("b", config.renameActiveProfile("b").activeProfile.name)
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
    fun renameActiveProfileTrimsCapsAndIgnoresBlankNames() {
        val config = Config(activeProfileId = "b", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        val renamed = config.renameActiveProfile("  Kids  ")
        assertEquals("Kids", renamed.profiles.first { it.id == "b" }.name)
        assertEquals("A", renamed.profiles.first { it.id == "a" }.name)
        assertSame(config, config.renameActiveProfile("   "))
        assertEquals(MAX_PROFILE_NAME_LENGTH, config.renameActiveProfile("x".repeat(50)).activeProfile.name.length)
    }

    @Test
    fun switchToNextProfileWraps() {
        val config = Config(activeProfileId = "b", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        assertEquals("a", config.switchToNextProfile().activeProfileId)
        assertEquals("b", config.switchToNextProfile().switchToNextProfile().activeProfileId)
    }

    @Test
    fun moveRowSavesTheFullOrderAndStopsAtTheEnds() {
        val p = base.copy(categories = listOf(Category("Video", listOf("a"))))
        val moved = p.moveRow("inputs", -1)
        assertEquals(listOf("favorites", "recent", "mostused", "category:Video", "inputs", "other", "quick"), moved.rowOrder)
        assertEquals(moved.rowOrder, moved.moveRow("favorites", -1).rowOrder)
        assertEquals("favorites", moved.moveRow("favorites", 1).moveRow("favorites", -1).rowOrder.first())
        assertEquals("quick", moved.moveRow("quick", 1).rowOrder.last())
    }

    @Test
    fun resetRowOrderGoesBackToTheDefault() {
        val p = base.moveRow("quick", -3)
        assertTrue(p.rowOrder.isNotEmpty())
        assertTrue(p.resetRowOrder().rowOrder.isEmpty())
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
