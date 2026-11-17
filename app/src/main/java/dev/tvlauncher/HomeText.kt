package dev.tvlauncher

/** A row with more apps than this scrolls, so its counter follows the focused app ("7 / 42"). */
const val CARDS_PER_SCREEN = 5

fun countOf(count: Int, singular: String, plural: String): String = "$count ${if (count == 1) singular else plural}"

/**
 * The small text at the right of a row heading. Smart rows say where their data lives, input and
 * quick-setting rows count their tiles, and a row too long to fit shows the focused position.
 * [focusedIndex] is the focused app's position in this row, or null when focus is elsewhere.
 */
fun rowCounter(row: RowSpec, tileCount: Int, focusedIndex: Int?, profileName: String): String = when (row.kind) {
    RowKind.MostUsed -> "On this TV"
    RowKind.Recent -> profileName
    RowKind.Inputs -> countOf(tileCount, "input", "inputs")
    RowKind.Quick -> countOf(tileCount, "control", "controls")
    else -> {
        val total = row.packages.size
        if (total > CARDS_PER_SCREEN && focusedIndex != null) "${focusedIndex + 1} / $total" else countOf(total, "app", "apps")
    }
}

fun homeSubtitle(profileName: String, focusedLongName: String?): String =
    if (focusedLongName != null) "Focused: $focusedLongName" else "$profileName’s apps, exactly where you left them."
