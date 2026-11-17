package dev.tvlauncher

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Result of reading the settings file. [Corrupt] must never be silently replaced with defaults. */
sealed interface ConfigLoad {
    data class Loaded(val config: Config) : ConfigLoad
    data object Fresh : ConfigLoad
    data object Corrupt : ConfigLoad
}

@Serializable
data class Category(val name: String, val packages: List<String> = emptyList())

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val clock24h: Boolean = false,
    val datePattern: String = "EEEE, MMMM d",
    val hidden: Set<String> = emptySet(),
    val categories: List<Category> = emptyList(),
    val showOtherRow: Boolean = true,
    val wallpaper: String = "ink",
    val favorites: List<String> = emptyList(),
    val renames: Map<String, String> = emptyMap(),
    val launchCounts: Map<String, Int> = emptyMap(),
    val recent: List<String> = emptyList(),
    val showFavorites: Boolean = true,
    val showRecent: Boolean = true,
    val showMostUsed: Boolean = true,
    val showHdmi: Boolean = true,
    val showQuickSettings: Boolean = false,
    /** Plays the system navigation click whenever focus moves to a new item. */
    val focusSound: Boolean = false,
    /** Row ids in the order this profile wants them; empty means the default order. See [rowId]. */
    val rowOrder: List<String> = emptyList(),
)

@Serializable
data class Config(
    val activeProfileId: String = DEFAULT_PROFILE_ID,
    val askProfileAtLaunch: Boolean = false,
    val profiles: List<Profile> = listOf(Profile(DEFAULT_PROFILE_ID, "Default")),
    val focusStyle: String = "outline",
    /** Screensaver clock sits over the profile wallpaper instead of plain Ink. */
    val screensaverWallpaper: Boolean = false,
    /** Screensaver uses the dim palette between 22:00 and 07:00. */
    val screensaverDim: Boolean = true,
    val dimAtNight: Boolean = false,
) {
    val activeProfile: Profile
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.first()

    fun updateActive(transform: (Profile) -> Profile): Config {
        val active = activeProfile
        return copy(
            activeProfileId = active.id,
            profiles = profiles.map { if (it.id == active.id) transform(it) else it },
        )
    }

    companion object {
        const val DEFAULT_PROFILE_ID = "default"
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

        /** Strict parse for restores: null when the text is not a usable config. */
        fun decodeOrNull(text: String?): Config? {
            if (text.isNullOrBlank()) return null
            val parsed = runCatching { json.decodeFromString<Config>(text) }.getOrNull() ?: return null
            return parsed.takeIf { it.profiles.isNotEmpty() }
        }

        /** Never throws: a missing, empty or corrupt file yields the default config. */
        fun decode(text: String?): Config = decodeOrNull(text) ?: Config()

        /** Tells a first run (no file yet) apart from a file that exists but cannot be read as settings. */
        fun classify(fileExists: Boolean, text: String?): ConfigLoad = when {
            !fileExists -> ConfigLoad.Fresh
            else -> decodeOrNull(text)?.let { ConfigLoad.Loaded(it) } ?: ConfigLoad.Corrupt
        }

        fun encode(config: Config): String = json.encodeToString(Config.serializer(), config)
    }
}
