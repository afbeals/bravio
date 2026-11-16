package dev.tvlauncher

import android.content.Context
import java.io.File

/** One JSON file, written atomically (temp file then rename) so a crash never leaves it half written. */
class ConfigStore(context: Context) {
    private val file = File(context.filesDir, "config.json")
    private val tmp = File(context.filesDir, "config.json.tmp")

    fun load(): Config = Config.decode(runCatching { file.readText() }.getOrNull())

    fun save(config: Config) {
        tmp.writeText(Config.encode(config))
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }
}
