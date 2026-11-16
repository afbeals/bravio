package dev.tvlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigTest {
    @Test
    fun roundTripKeepsEverything() {
        val config = Config(
            activeProfileId = "kid",
            askProfileAtLaunch = true,
            profiles = listOf(
                Profile("default", "Default"),
                Profile(
                    "kid", "Kid",
                    showClock = false, clock24h = true, datePattern = "d MMM",
                    hidden = setOf("x"), categories = listOf(Category("Fun", listOf("y"))),
                ),
            ),
        )
        assertEquals(config, Config.decode(Config.encode(config)))
    }

    @Test
    fun newFieldsRoundTripAndOldConfigsStillLoad() {
        val profile = Profile(
            "p", "P",
            favorites = listOf("a"), renames = mapOf("a" to "Alpha"), launchCounts = mapOf("a" to 4),
            recent = listOf("a"), showQuickSettings = true, showHdmi = false,
        )
        val config = Config(profiles = listOf(profile), activeProfileId = "p", focusStyle = "glow", dimAtNight = true, backShowsClock = false)
        assertEquals(config, Config.decode(Config.encode(config)))
        // A config written before these fields existed keeps working with defaults.
        val old = Config.decode("""{"activeProfileId":"p","profiles":[{"id":"p","name":"P"}]}""")
        assertEquals("scale", old.focusStyle)
        assertTrue(old.activeProfile.showHdmi)
        assertTrue(old.activeProfile.favorites.isEmpty())
    }

    @Test
    fun strictDecodeRejectsWhatALenientLoadWouldReplaceWithDefaults() {
        assertNull(Config.decodeOrNull(null))
        assertNull(Config.decodeOrNull("not json"))
        assertNull(Config.decodeOrNull("""{"profiles":[]}"""))
        assertEquals("a", Config.decodeOrNull("""{"activeProfileId":"a","profiles":[{"id":"a","name":"A"}]}""")?.activeProfile?.id)
    }

    @Test
    fun missingBlankOrCorruptTextGivesDefaults() {
        assertEquals(Config(), Config.decode(null))
        assertEquals(Config(), Config.decode(""))
        assertEquals(Config(), Config.decode("{ not json"))
        assertEquals(Config(), Config.decode("""{"profiles":[]}"""))
    }

    @Test
    fun unknownFieldsFromANewerVersionAreIgnored() {
        val config = Config.decode("""{"future":1,"profiles":[{"id":"a","name":"A","extra":true}],"activeProfileId":"a"}""")
        assertEquals("a", config.activeProfile.id)
    }

    @Test
    fun unknownActiveProfileFallsBackToTheFirst() {
        val config = Config(activeProfileId = "nope", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        assertEquals("a", config.activeProfile.id)
    }

    @Test
    fun updateActiveOnlyChangesTheActiveProfile() {
        val config = Config(activeProfileId = "b", profiles = listOf(Profile("a", "A"), Profile("b", "B")))
        val updated = config.updateActive { it.copy(showClock = false) }
        assertEquals(true, updated.profiles[0].showClock)
        assertFalse(updated.profiles[1].showClock)
    }
}
