package dev.tvlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

data class AppEntry(
    val packageName: String,
    val activityName: String,
    val label: String,
    val intent: Intent,
)

/** Lists launchable apps: leanback entries first, then regular launcher entries, deduped by package. */
class AppRepository(private val context: Context) {
    private val pm get() = context.packageManager

    fun load(): List<AppEntry> {
        val seen = HashSet<String>()
        val result = ArrayList<AppEntry>()
        for (category in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            for (info in pm.queryIntentActivities(query, 0)) {
                val entry = info.toEntry(category) ?: continue
                if (entry.packageName == context.packageName) continue
                if (seen.add(entry.packageName)) result += entry
            }
        }
        return result.sortedBy { it.label.lowercase() }
    }

    fun icon(entry: AppEntry): Drawable? =
        runCatching { pm.getActivityIcon(entry.intent) }.getOrNull()

    fun banner(entry: AppEntry): Drawable? =
        runCatching { pm.getActivityBanner(entry.intent) }.getOrNull()

    private fun ResolveInfo.toEntry(category: String): AppEntry? {
        val ai = activityInfo ?: return null
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(category)
            .setClassName(ai.packageName, ai.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return AppEntry(ai.packageName, ai.name, loadLabel(pm).toString(), intent)
    }
}
