package dev.tvlauncher

val DATE_PRESETS = listOf("EEEE, MMMM d", "EEE, MMM d", "d MMMM", "M/d/yyyy")
val CATEGORY_PRESETS = listOf("Video", "Music", "Games", "Kids", "Tools", "Family")
val FOCUS_STYLES = listOf("outline", "scale", "glow")
const val RECENT_LIMIT = 10
const val MAX_NAME_LENGTH = 40
const val MAX_PROFILE_NAME_LENGTH = 20

/** Adds [pkg] to the category called [categoryName], creating the category when it doesn't exist. */
fun Profile.withApp(pkg: String, categoryName: String): Profile {
    val existing = categories.any { it.name == categoryName }
    val updated = if (existing) {
        categories.map { c ->
            if (c.name == categoryName && pkg !in c.packages) c.copy(packages = c.packages + pkg) else c
        }
    } else {
        categories + Category(categoryName, listOf(pkg))
    }
    return copy(categories = updated)
}

/** Removes [pkg] from every category and drops categories left empty. */
fun Profile.withoutApp(pkg: String): Profile =
    copy(
        categories = categories
            .map { it.copy(packages = it.packages - pkg) }
            .filter { it.packages.isNotEmpty() },
    )

fun Profile.inAnyCategory(pkg: String): Boolean = categories.any { pkg in it.packages }

fun Profile.isFavorite(pkg: String): Boolean = pkg in favorites

fun Profile.toggleFavorite(pkg: String): Profile =
    copy(favorites = if (pkg in favorites) favorites - pkg else favorites + pkg)

fun Profile.unhide(pkg: String): Profile = copy(hidden = hidden - pkg)

/** Moves [pkg] by [delta] places within [this] list; stays put at either end or when absent. */
fun List<String>.moved(pkg: String, delta: Int): List<String> {
    val from = indexOf(pkg)
    if (from < 0) return this
    val to = (from + delta).coerceIn(0, lastIndex)
    if (to == from) return this
    return toMutableList().also {
        it.removeAt(from)
        it.add(to, pkg)
    }
}

fun Profile.moveInFavorites(pkg: String, delta: Int): Profile = copy(favorites = favorites.moved(pkg, delta))

fun Profile.moveInCategory(pkg: String, categoryName: String, delta: Int): Profile =
    copy(
        categories = categories.map { c ->
            if (c.name == categoryName) c.copy(packages = c.packages.moved(pkg, delta)) else c
        },
    )

/** Sets a custom label; a blank name clears it. Names are trimmed and capped. */
fun Profile.rename(pkg: String, name: String): Profile {
    val clean = name.trim().take(MAX_NAME_LENGTH)
    return copy(renames = if (clean.isEmpty()) renames - pkg else renames + (pkg to clean))
}

fun Profile.labelFor(pkg: String, default: String): String = renames[pkg] ?: default

/** Counts a launch and moves the app to the front of the recent list. */
fun Profile.recordLaunch(pkg: String): Profile =
    copy(
        launchCounts = launchCounts + (pkg to (launchCounts[pkg] ?: 0) + 1),
        recent = (listOf(pkg) + recent.filter { it != pkg }).take(RECENT_LIMIT),
    )

/** Next date format in [DATE_PRESETS]; an unknown (custom) pattern restarts at the first preset. */
fun Profile.nextDatePattern(): Profile {
    val next = (DATE_PRESETS.indexOf(datePattern) + 1) % DATE_PRESETS.size
    return copy(datePattern = DATE_PRESETS[next])
}

fun Config.nextFocusStyle(): Config {
    val next = (FOCUS_STYLES.indexOf(focusStyle) + 1) % FOCUS_STYLES.size
    return copy(focusStyle = FOCUS_STYLES[next])
}

/** The design covers one to six profiles, so that is the limit. */
const val MAX_PROFILES = 6

enum class NameProblem { Empty, Taken }

/** Null when [name] is acceptable. [others] are the names of the other profiles (compared ignoring case). */
fun validateProfileName(name: String, others: List<String>): NameProblem? {
    val clean = name.trim()
    return when {
        clean.isEmpty() -> NameProblem.Empty
        others.any { it.trim().equals(clean, ignoreCase = true) } -> NameProblem.Taken
        else -> null
    }
}

fun Config.canAddProfile(): Boolean = profiles.size < MAX_PROFILES

/** "Profile 2", "Profile 3", ... the first one not already used. */
fun Config.suggestProfileName(): String {
    val used = profiles.map { it.name.trim().lowercase() }.toSet()
    var n = profiles.size + 1
    while ("profile $n" in used) n++
    return "Profile $n"
}

/**
 * Adds a profile with default settings and makes it the active one. Returns this unchanged when the
 * profile limit is reached or [name] is not valid (see [validateProfileName]).
 */
fun Config.addProfile(name: String): Config {
    if (!canAddProfile() || validateProfileName(name, profiles.map { it.name }) != null) return this
    val ids = profiles.map { it.id }.toSet()
    var n = profiles.size + 1
    while ("p$n" in ids) n++
    val profile = Profile(id = "p$n", name = name.trim().take(MAX_PROFILE_NAME_LENGTH))
    return copy(profiles = profiles + profile, activeProfileId = profile.id)
}

/** Moves a row up (negative [delta]) or down within the profile's full row list and saves that order. */
fun Profile.moveRow(id: String, delta: Int): Profile = copy(rowOrder = effectiveRowIds(this).moved(id, delta))

fun Profile.resetRowOrder(): Profile = copy(rowOrder = emptyList())

/** Renames the active profile. An invalid name (blank, or used by another profile) is ignored. */
fun Config.renameActiveProfile(name: String): Config {
    val clean = name.trim().take(MAX_PROFILE_NAME_LENGTH)
    val others = profiles.filter { it.id != activeProfile.id }.map { it.name }
    if (validateProfileName(clean, others) != null) return this
    return updateActive { it.copy(name = clean) }
}

/** Deletes the active profile (never the last one) and falls back to the first remaining. */
fun Config.removeActiveProfile(): Config {
    if (profiles.size <= 1) return this
    val remaining = profiles.filter { it.id != activeProfile.id }
    return copy(profiles = remaining, activeProfileId = remaining.first().id)
}

fun Config.switchToNextProfile(): Config {
    val index = profiles.indexOfFirst { it.id == activeProfile.id }
    return copy(activeProfileId = profiles[(index + 1) % profiles.size].id)
}
