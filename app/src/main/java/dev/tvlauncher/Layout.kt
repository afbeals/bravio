package dev.tvlauncher

enum class RowKind { Favorites, Recent, MostUsed, Category, Other, Inputs, Quick }

/** [packages] is empty for the non-app rows ([RowKind.Inputs], [RowKind.Quick]). */
data class RowSpec(val title: String, val packages: List<String>, val kind: RowKind = RowKind.Category) {
    val id: String get() = rowId(kind, title)
}

const val SMART_ROW_LIMIT = 10
const val MOST_USED_MIN_LAUNCHES = 2

/** Stable id used to store a profile's row order. Categories are identified by name. */
fun rowId(kind: RowKind, title: String = ""): String =
    if (kind == RowKind.Category) "category:$title" else kind.name.lowercase()

/** Every row a profile can have, in the default order, whether or not it is switched on or has content. */
fun allRowIds(profile: Profile): List<String> =
    listOf(rowId(RowKind.Favorites), rowId(RowKind.Recent), rowId(RowKind.MostUsed)) +
        profile.categories.map { rowId(RowKind.Category, it.name) } +
        listOf(rowId(RowKind.Other), rowId(RowKind.Inputs), rowId(RowKind.Quick))

/** The profile's chosen order applied to [allRowIds]; rows it doesn't mention keep default order at the end. */
fun effectiveRowIds(profile: Profile): List<String> = orderIds(allRowIds(profile), profile.rowOrder)

fun rowLabel(id: String): String = when {
    id.startsWith("category:") -> id.removePrefix("category:")
    id == rowId(RowKind.Favorites) -> "Favorites"
    id == rowId(RowKind.Recent) -> "Recently used"
    id == rowId(RowKind.MostUsed) -> "Most used"
    id == rowId(RowKind.Other) -> "Other apps"
    id == rowId(RowKind.Inputs) -> "Inputs"
    id == rowId(RowKind.Quick) -> "Quick settings"
    else -> id
}

fun Profile.rowEnabled(id: String): Boolean = when (id) {
    rowId(RowKind.Favorites) -> showFavorites
    rowId(RowKind.Recent) -> showRecent
    rowId(RowKind.MostUsed) -> showMostUsed
    rowId(RowKind.Other) -> showOtherRow
    rowId(RowKind.Inputs) -> showHdmi
    rowId(RowKind.Quick) -> showQuickSettings
    else -> true
}

/**
 * Orders [available] by [order]: ids named in [order] come first in that order; stale ids (a deleted
 * category) are skipped and anything not named keeps its default order after them.
 */
fun orderIds(available: List<String>, order: List<String>): List<String> {
    val present = available.toSet()
    val listed = order.filter { it in present }.distinct()
    val listedSet = listed.toSet()
    return listed + available.filter { it !in listedSet }
}

fun orderRows(rows: List<RowSpec>, order: List<String>): List<RowSpec> {
    val byId = rows.associateBy { it.id }
    return orderIds(rows.map { it.id }, order).mapNotNull { byId[it] }
}

/**
 * Pure layout: which rows a profile shows, given the installed packages (already sorted).
 * Default order: Favorites, Recently used, Most used, categories, Other apps, Inputs, Quick settings,
 * then the profile's own [Profile.rowOrder] is applied. Hidden apps never appear. Apps removed from the
 * device are dropped. A package may sit in several categories. Whatever is in no category goes to the
 * "Other apps" row. [hasInputs] and [hasQuickTiles] say whether the TV has anything for those rows.
 */
fun buildRows(
    installed: List<String>,
    profile: Profile,
    hasInputs: Boolean = false,
    hasQuickTiles: Boolean = false,
): List<RowSpec> {
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

    if (profile.showHdmi && hasInputs) rows += RowSpec("Inputs", emptyList(), RowKind.Inputs)
    if (profile.showQuickSettings && hasQuickTiles) rows += RowSpec("Quick settings", emptyList(), RowKind.Quick)

    return orderRows(rows, profile.rowOrder)
}
