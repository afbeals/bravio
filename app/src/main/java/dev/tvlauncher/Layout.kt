package dev.tvlauncher

enum class RowKind { Favorites, Recent, MostUsed, Category, Other }

data class RowSpec(val title: String, val packages: List<String>, val kind: RowKind = RowKind.Category)

const val SMART_ROW_LIMIT = 10
const val MOST_USED_MIN_LAUNCHES = 2

/**
 * Pure layout: which app rows a profile shows, given the installed packages (already sorted).
 * Order: Favorites, Recently used, Most used, categories, then "Other apps".
 * Hidden apps never appear. Apps removed from the device are dropped. A package may sit in several
 * categories. Whatever is in no category goes to the trailing "Other apps" row.
 */
fun buildRows(installed: List<String>, profile: Profile): List<RowSpec> {
    val visible = installed.filter { it !in profile.hidden }
    val visibleSet = visible.toSet()
    val rows = ArrayList<RowSpec>()

    if (profile.showFavorites) {
        val members = profile.favorites.filter { it in visibleSet }.distinct()
        if (members.isNotEmpty()) rows += RowSpec("Favorites", members, RowKind.Favorites)
    }
    if (profile.showRecent) {
        val members = profile.recent.filter { it in visibleSet }.distinct().take(SMART_ROW_LIMIT)
        if (members.isNotEmpty()) rows += RowSpec("Recently used", members, RowKind.Recent)
    }
    if (profile.showMostUsed) {
        val members = profile.launchCounts.entries
            .filter { it.key in visibleSet && it.value >= MOST_USED_MIN_LAUNCHES }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(SMART_ROW_LIMIT)
            .map { it.key }
        if (members.isNotEmpty()) rows += RowSpec("Most used", members, RowKind.MostUsed)
    }

    val categorized = HashSet<String>()
    var categoryRows = 0
    for (category in profile.categories) {
        val members = category.packages.filter { it in visibleSet }.distinct()
        if (members.isEmpty()) continue
        categorized += members
        rows += RowSpec(category.name, members, RowKind.Category)
        categoryRows++
    }

    val other = visible.filter { it !in categorized }
    if (other.isNotEmpty() && (profile.showOtherRow || rows.isEmpty())) {
        rows += RowSpec(if (categoryRows == 0) "Apps" else "Other apps", other, RowKind.Other)
    }
    return rows
}
