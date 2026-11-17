package dev.tvlauncher

import android.content.Context
import java.io.File

/** One JSON file, written atomically (temp file then rename) so a crash never leaves it half written. */
class ConfigStore(context: Context) {
    private val file = File(context.filesDir, "config.json")
    private val tmp = File(context.filesDir, "config.json.tmp")
    private val quarantined = File(context.filesDir, "config.corrupt.json")

    fun loadResult(): ConfigLoad {
        val exists = file.isFile
        val text = if (exists) runCatching { file.readText() }.getOrNull() else null
        return Config.classify(exists, text)
    }

    /** Settings to use right now: what was saved, or defaults for a first run or an unreadable file. */
    fun load(): Config = (loadResult() as? ConfigLoad.Loaded)?.config ?: Config()

    /** Returns false when the write failed (disk full, storage error); the previous file is left untouched. */
    fun save(config: Config): Boolean = runCatching {
        tmp.writeText(Config.encode(config))
        if (!tmp.renameTo(file)) {
            file.delete()
            check(tmp.renameTo(file)) { "rename failed" }
        }
    }.isSuccess

    /** Moves an unreadable settings file aside (kept for recovery) so defaults can be written. */
    fun quarantineCorrupt(): Boolean = runCatching {
        quarantined.delete()
        file.renameTo(quarantined)
    }.getOrDefault(false)
}
