package dev.tvlauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class RowType { Toggle, Cycle, Action }

private class Help(val heading: String, val body: List<String>)

private sealed interface Entry

private class SectionEntry(val title: String) : Entry

private class SettingEntry(
    val key: String,
    val label: String,
    val value: String,
    val type: RowType,
    val on: Boolean = false,
    val help: Help,
    val onClick: () -> Unit,
) : Entry

private fun onOff(value: Boolean) = if (value) "On" else "Off"

private fun formatDate(pattern: String): String =
    runCatching { LocalDate.now().format(DateTimeFormatter.ofPattern(pattern)) }.getOrDefault("?")

@Composable
fun SettingsScreen(
    config: Config,
    rows: List<RowSpec>,
    update: ((Config) -> Config) -> Unit,
    onHidden: () -> Unit,
    onWallpaper: () -> Unit,
    onRowOrder: () -> Unit,
    onProfiles: () -> Unit,
    onSearch: () -> Unit,
    onPreviewClock: () -> Unit,
    onTvSettings: () -> Unit,
    notify: (String, Boolean) -> Unit,
    onDone: () -> Unit,
) {
    val profile = config.activeProfile
    val context = LocalContext.current
    val style = config.focusStyle
    var renaming by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var restoring by remember { mutableStateOf(false) }
    var help by remember { mutableStateOf<Help?>(null) }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }

    fun toast(message: String, error: Boolean = false) = notify(message, error)
    fun edit(transform: (Profile) -> Profile) = update { c -> c.updateActive(transform) }

    val forProfile = "For ${profile.name}"
    val forAll = "All profiles"
    fun mine(vararg lines: String) = Help(forProfile, lines.toList())
    fun everyone(vararg lines: String) = Help(forAll, lines.toList())

    val entries = buildList<Entry> {
        add(SectionEntry("Profiles"))
        add(SettingEntry("switch", "Switch profile", "${profile.name} · ${countOf(config.profiles.size, "profile", "profiles")}", RowType.Action,
            help = mine("Pick who is watching.", "", "Profile changes do not provide security."), onClick = onProfiles))
        add(SettingEntry("rename", "Rename profile", profile.name, RowType.Action,
            help = mine("Change the name shown in the sidebar and profile picker."), onClick = { renaming = true }))
        if (config.canAddProfile()) {
            add(SettingEntry("add", "Add profile", "", RowType.Action,
                help = everyone("You can have up to $MAX_PROFILES profiles."), onClick = { adding = true }))
        }
        if (config.profiles.size > 1) {
            add(SettingEntry("delete", "Delete profile", "", RowType.Action,
                help = mine("Removes this profile’s categories, favorites and settings."), onClick = { deleting = true }))
        }

        add(SectionEntry("Clock and date"))
        add(SettingEntry("clock", "Clock", onOff(profile.showClock), RowType.Toggle, profile.showClock,
            mine("Clock and date settings apply to ${profile.name}."), { edit { it.copy(showClock = !it.showClock) } }))
        add(SettingEntry("time", "Time format", if (profile.clock24h) "24-hour" else "12-hour", RowType.Cycle,
            help = mine("12-hour clock: 8:41 PM", "24-hour clock: 20:41"), onClick = { edit { it.copy(clock24h = !it.clock24h) } }))
        add(SettingEntry("date", "Date", onOff(profile.showDate), RowType.Toggle, profile.showDate,
            mine("The date sits under the clock on Home."), { edit { it.copy(showDate = !it.showDate) } }))
        add(SettingEntry("dateformat", "Date format", formatDate(profile.datePattern), RowType.Cycle,
            help = mine("Four date examples:", "") .let { Help(forProfile, it.body + DATE_PRESETS.map(::formatDate)) },
            onClick = { edit { it.nextDatePattern() } }))

        add(SectionEntry("Rows on Home"))
        add(SettingEntry("favorites", "Favorites", onOff(profile.showFavorites), RowType.Toggle, profile.showFavorites,
            mine("Empty rows stay hidden even when switched on."), { edit { it.copy(showFavorites = !it.showFavorites) } }))
        add(SettingEntry("recent", "Recently launched", onOff(profile.showRecent), RowType.Toggle, profile.showRecent,
            mine("Most used and recently launched use counts stored on this TV, separately for each profile."), { edit { it.copy(showRecent = !it.showRecent) } }))
        add(SettingEntry("mostused", "Most used", onOff(profile.showMostUsed), RowType.Toggle, profile.showMostUsed,
            mine("Most used and recently launched use counts stored on this TV, separately for each profile."), { edit { it.copy(showMostUsed = !it.showMostUsed) } }))
        add(SettingEntry("other", "Other apps", onOff(profile.showOtherRow), RowType.Toggle, profile.showOtherRow,
            mine("Apps that are in no category."), { edit { it.copy(showOtherRow = !it.showOtherRow) } }))
        add(SettingEntry("hdmi", "HDMI inputs", onOff(profile.showHdmi), RowType.Toggle, profile.showHdmi,
            mine("Switch inputs from Home."), { edit { it.copy(showHdmi = !it.showHdmi) } }))
        add(SettingEntry("quick", "Quick settings", onOff(profile.showQuickSettings), RowType.Toggle, profile.showQuickSettings,
            mine("Shortcuts to sleep timer, picture, sound and more."), { edit { it.copy(showQuickSettings = !it.showQuickSettings) } }))
        add(SettingEntry("order", "Row order", "", RowType.Action,
            help = mine("Move rows up or down. The order is saved for ${profile.name}."), onClick = onRowOrder))

        add(SectionEntry("Appearance"))
        add(SettingEntry("wallpaper", "Wallpaper", wallpaperName(profile.wallpaper), RowType.Action,
            help = mine("Wallpaper applies to ${profile.name}.", "", "Pictures get a dark scrim so text stays readable."), onClick = onWallpaper))
        add(SettingEntry("dim", "Dim at night", onOff(config.dimAtNight), RowType.Toggle, config.dimAtNight,
            everyone("Uses the TV’s light sensor to switch to a darker palette in a dark room."), { update { it.copy(dimAtNight = !it.dimAtNight) } }))
        add(SettingEntry("focus", "Focus style", FOCUS_LABELS[config.focusStyle] ?: config.focusStyle, RowType.Cycle,
            help = everyone("Outline: a 6 px iris outline.", "", "Scale: outline and a slight enlarge.", "", "Glow: outline and a soft glow."),
            onClick = { update { it.nextFocusStyle() } }))
        add(SettingEntry("sound", "Navigation sounds", onOff(profile.focusSound), RowType.Toggle, profile.focusSound,
            mine("Plays a click when focus moves to a new item.", "", "Uses the TV’s system sound effects setting."), { edit { it.copy(focusSound = !it.focusSound) } }))
        add(SettingEntry("saverwall", "Screensaver wallpaper", onOff(config.screensaverWallpaper), RowType.Toggle, config.screensaverWallpaper,
            everyone("Shows the clock over the profile’s wallpaper instead of plain Ink."), { update { it.copy(screensaverWallpaper = !it.screensaverWallpaper) } }))
        add(SettingEntry("saverdim", "Dim screensaver at night", onOff(config.screensaverDim), RowType.Toggle, config.screensaverDim,
            everyone("Uses the dim palette between 22:00 and 07:00."), { update { it.copy(screensaverDim = !it.screensaverDim) } }))
        add(SettingEntry("saverpreview", "Preview screensaver", "", RowType.Action,
            help = everyone("Hold Back on Home shows the clock any time.", "", "Enable the system screensaver once with:", "settings put secure screensaver_components dev.tvlauncher/.ClockDreamService"),
            onClick = onPreviewClock))
        add(SettingEntry("hidden", "Hidden apps", countOf(profile.hidden.size, "app", "apps"), RowType.Action,
            help = mine("Hidden apps stay launchable from system screens."), onClick = onHidden))

        add(SectionEntry("Backup"))
        add(SettingEntry("backup", "Back up settings", "", RowType.Action,
            help = everyone("Saves every profile to ${Backup.FILE_NAME} in the launcher’s folder."),
            onClick = { if (Backup.save(context, config)) toast("Saved ${Backup.FILE_NAME}") else toast("Could not save the backup", true) }))
        add(SettingEntry("restore", "Restore settings", "", RowType.Action,
            help = everyone("Replaces all profiles and settings with the backup file."),
            onClick = { if (Backup.exists(context)) restoring = true else toast("No ${Backup.FILE_NAME} found", true) }))

        add(SectionEntry("Launch · all profiles"))
        add(SettingEntry("launch", "On launch", if (config.askProfileAtLaunch) "Ask who’s watching" else "Open Home", RowType.Cycle,
            help = everyone("Screensaver, focus style, and launch behavior apply to every profile."),
            onClick = { update { it.copy(askProfileAtLaunch = !it.askProfileAtLaunch) } }))
    }
    val firstKey = entries.filterIsInstance<SettingEntry>().first().key

    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile, WallpaperSurface.Reading)
        Sidebar(
            profileName = profile.name,
            rows = rows,
            style = style,
            onHome = onDone,
            onSearch = onSearch,
            onProfiles = onProfiles,
            onRow = { onDone() },
            onSettings = {},
            onTvSettings = onTvSettings,
            modifier = Modifier.padding(start = Ink.safeHorizontal, top = Ink.safeVertical),
        )
        Column(Modifier.fillMaxSize().padding(start = px(448), top = px(64))) {
            Text("Launcher settings", style = InkType.title)
            Spacer(Modifier.height(px(8)))
            Text("${profile.name} · Changes save automatically", style = InkType.body, color = Ink.colors.muted)
            Row(Modifier.padding(top = px(28))) {
                LazyColumn(
                    Modifier.width(px(1000)).height(px(728)),
                    verticalArrangement = Arrangement.spacedBy(Ink.gap12),
                ) {
                    items(entries.size, key = { i -> (entries[i] as? SettingEntry)?.key ?: "s:${(entries[i] as SectionEntry).title}" }) { i ->
                        when (val e = entries[i]) {
                            is SectionEntry -> Text(
                                e.title, style = InkType.section,
                                modifier = Modifier.height(px(48)).padding(top = px(8)),
                            )

                            is SettingEntry -> SettingRow(
                                e, style,
                                modifier = if (e.key == firstKey) Modifier.focusRequester(first) else Modifier,
                                onFocus = { help = e.help },
                            )
                        }
                    }
                    item(key = "done") { InkButton("Done", onDone, primary = true, style = style) }
                }
                Spacer(Modifier.width(px(48)))
                HelpPanel(help ?: entries.filterIsInstance<SettingEntry>().first().help)
            }
        }
        Text(
            "OK  Select     Back  Return",
            style = InkType.body, color = Ink.colors.muted,
            modifier = Modifier.padding(start = px(448), top = px(986)),
        )
        if (renaming) {
            RenameProfileDialog(profile.name, onSave = { name -> update { it.renameActiveProfile(name) }; renaming = false }, onCancel = { renaming = false })
        }
        if (adding) {
            CreateProfileDialog(config, onCreate = { name -> adding = false; update { it.addProfile(name) } }, onCancel = { adding = false })
        }
        if (deleting) {
            ConfirmDialog(
                title = "Delete ${profile.name}’s profile?",
                body = "Its categories, favorites and settings are removed. Other profiles are not affected.",
                confirmLabel = "Delete",
                onConfirm = { deleting = false; update { it.removeActiveProfile() } },
                onCancel = { deleting = false },
            )
        }
        if (restoring) {
            ConfirmDialog(
                title = "Replace all settings?",
                body = "Every profile and setting is replaced by the backup file. This can’t be undone.",
                confirmLabel = "Restore",
                onConfirm = {
                    restoring = false
                    val restored = Backup.load(context)
                    if (restored == null) toast("The backup file is not valid. Nothing was changed.", true) else {
                        update { restored }
                        toast("Settings restored")
                    }
                },
                onCancel = { restoring = false },
            )
        }
    }
}

private val FOCUS_LABELS = mapOf("outline" to "Outline", "scale" to "Scale", "glow" to "Glow")

@Composable
private fun SettingRow(entry: SettingEntry, style: String, modifier: Modifier, onFocus: () -> Unit) {
    val c = Ink.colors
    InkSurface(
        onClick = entry.onClick,
        modifier = modifier.fillMaxWidth().height(px(76)),
        style = style,
        onFocusChange = { if (it) onFocus() },
    ) { _ ->
        Row(Modifier.fillMaxSize().padding(px(16)), verticalAlignment = Alignment.CenterVertically) {
            Text(entry.label, style = InkType.label, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (entry.value.isNotEmpty()) {
                Text(entry.value, style = InkType.body, color = c.muted, textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(px(16)))
            }
            when (entry.type) {
                RowType.Toggle -> Switch(entry.on)
                else -> InkIcon(R.drawable.ic_chevron_right, size = px(32), tint = c.muted)
            }
        }
    }
}

@Composable
private fun Switch(on: Boolean) {
    val c = Ink.colors
    Box(
        Modifier.size(px(64), px(32)).background(if (on) c.focus else c.raised, RoundedCornerShape(px(16))),
    ) {
        Box(
            Modifier
                .padding(start = if (on) px(36) else px(4), top = px(4))
                .size(px(24))
                .background(if (on) c.onFocus else c.text, RoundedCornerShape(px(12))),
        )
    }
}

@Composable
private fun HelpPanel(help: Help) {
    Column(
        Modifier.size(px(328), px(620)).background(Ink.colors.panel, RoundedCornerShape(Ink.radius16)).padding(px(24)),
        verticalArrangement = Arrangement.spacedBy(px(20)),
    ) {
        Text(help.heading, style = InkType.section, maxLines = 2)
        Text(help.body.joinToString("\n"), style = InkType.body, color = Ink.colors.muted)
    }
}

/** A centered question with Cancel (focused first, the safe choice) and a confirming button. */
@Composable
fun ConfirmDialog(title: String, body: String, confirmLabel: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    val cancel = remember { FocusRequester() }
    LaunchedEffect(Unit) { cancel.requestFocusWhenReady() }
    BackHandler(enabled = true) { onCancel() }
    Box(Modifier.fillMaxSize().background(Color(0xD1000000)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.size(px(1000), px(460)).background(Ink.colors.bg, RoundedCornerShape(Ink.radius16)).padding(px(48)),
            verticalArrangement = Arrangement.spacedBy(Ink.gap24),
        ) {
            Text(title, style = InkType.title)
            Text(body, style = InkType.body, color = Ink.colors.muted)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                InkButton("Cancel", onCancel, Modifier.focusRequester(cancel))
                InkButton(confirmLabel, onConfirm, primary = true)
            }
        }
    }
}


/** A plain list row (label, optional value, optional trailing content) used by the sub-screens. */
@Composable
fun ListRow(
    label: String,
    value: String,
    style: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = Ink.colors.text,
    trailing: @Composable (() -> Unit)? = null,
) {
    InkSurface(onClick = onClick, modifier = modifier.fillMaxWidth().height(px(76)), style = style) { _ ->
        Row(Modifier.fillMaxSize().padding(px(16)), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = InkType.label, color = labelColor, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (value.isNotEmpty()) {
                Text(value, style = InkType.body, color = Ink.colors.muted, maxLines = 1)
                Spacer(Modifier.width(px(16)))
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun SubScreen(profile: Profile, title: String, subtitle: String, hint: String, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(start = Ink.safeHorizontal, top = px(64), end = Ink.safeHorizontal)) {
            Text(title, style = InkType.title)
            Spacer(Modifier.height(px(8)))
            Text(subtitle, style = InkType.body, color = Ink.colors.muted)
            Spacer(Modifier.height(px(28)))
            content()
        }
        Text(hint, style = InkType.body, color = Ink.colors.muted, modifier = Modifier.padding(start = Ink.safeHorizontal, top = px(986)))
    }
}

/** Rows in the profile's order. OK picks a row up, Up and Down move it, OK or Back drops it. */
@Composable
fun RowOrderScreen(profile: Profile, style: String, onMove: (String, Int) -> Unit, onReset: () -> Unit, onDone: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    var grabbed by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = grabbed != null) { grabbed = null }
    val ids = effectiveRowIds(profile)
    SubScreen(
        profile, "Row order", "${profile.name} · Choose the order of rows on Home",
        if (grabbed == null) "OK  Pick up a row     Back  Return" else "Up / Down  Move     OK  Drop",
    ) {
        LazyColumn(
            Modifier.width(px(1000)).height(px(780)).onPreviewKeyEvent { event ->
                val id = grabbed
                if (id != null && event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp -> { onMove(id, -1); true }
                        Key.DirectionDown -> { onMove(id, 1); true }
                        else -> false
                    }
                } else {
                    false
                }
            },
            verticalArrangement = Arrangement.spacedBy(Ink.gap12),
        ) {
            items(ids.size, key = { ids[it] }) { i ->
                val id = ids[i]
                val enabled = profile.rowEnabled(id)
                ListRow(
                    label = rowLabel(id),
                    value = when {
                        grabbed == id -> "Moving"
                        !enabled -> "Off"
                        else -> ""
                    },
                    style = style,
                    onClick = { grabbed = if (grabbed == id) null else id },
                    modifier = if (i == 0) Modifier.focusRequester(first) else Modifier,
                    labelColor = if (enabled) Ink.colors.text else Ink.colors.muted,
                    trailing = { InkIcon(R.drawable.ic_move_horizontal, size = px(32), tint = if (grabbed == id) Ink.colors.focus else Ink.colors.muted) },
                )
            }
            item(key = "buttons") {
                Row(Modifier.padding(top = Ink.gap12), horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                    InkButton("Done", onDone, primary = true, style = style)
                    InkButton("Reset order", onReset, style = style)
                }
            }
        }
    }
}

@Composable
fun HiddenAppsScreen(profile: Profile, style: String, labelOf: (String) -> String, onUnhide: (String) -> Unit, onUnhideAll: () -> Unit, onDone: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    val hidden = remember(profile.hidden) { profile.hidden.sortedBy { labelOf(it).lowercase() } }
    SubScreen(
        profile, "Hidden apps",
        if (hidden.isEmpty()) profile.name else "${profile.name} · ${countOf(hidden.size, "hidden app", "hidden apps")}",
        "OK  Select     Back  Return",
    ) {
        if (hidden.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                Text("Nothing is hidden", style = InkType.section)
                Text("All installed apps are available in this profile’s Home rows.", style = InkType.body, color = Ink.colors.muted)
                InkButton("Done", onDone, Modifier.focusRequester(first), primary = true, style = style)
            }
        } else {
            LazyColumn(Modifier.width(px(1000)).height(px(780)), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
                items(hidden.size, key = { hidden[it] }) { i ->
                    ListRow(
                        labelOf(hidden[i]), "Unhide", style, { onUnhide(hidden[i]) },
                        modifier = if (i == 0) Modifier.focusRequester(first) else Modifier,
                        trailing = { InkIcon(R.drawable.ic_chevron_right, size = px(32), tint = Ink.colors.muted) },
                    )
                }
                item(key = "all") { ListRow("Unhide all (${hidden.size})", "", style, onUnhideAll) }
                item(key = "done") { InkButton("Done", onDone, Modifier.padding(top = Ink.gap12), primary = true, style = style) }
            }
        }
    }
}


/** Shown instead of Home when the saved settings can't be read. Nothing is overwritten until the user picks. */
@Composable
fun ConfigErrorScreen(onRetry: () -> Unit, onRestore: () -> Unit, onReset: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    Box(Modifier.fillMaxSize().background(Ink.colors.bg)) {
        Column(Modifier.padding(start = px(448), top = px(64))) {
            Text("Your library", style = InkType.title)
            Column(Modifier.padding(top = px(186)), verticalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                Text("Configuration could not be read", style = InkType.title)
                Text(
                    "Your saved configuration is unchanged. Try again, restore a backup, or start fresh (the unreadable file is kept as config.corrupt.json).",
                    style = InkType.body, color = Ink.colors.muted, modifier = Modifier.width(px(1000)),
                )
                Row(Modifier.padding(top = px(32)), horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                    InkButton("Try again", onRetry, Modifier.focusRequester(first), primary = true)
                    InkButton("Restore backup", onRestore)
                    InkButton("Start fresh", onReset)
                }
            }
        }
        Text(
            "Home and Back stay in the launcher.",
            style = InkType.body, color = Ink.colors.muted,
            modifier = Modifier.padding(start = px(448), top = px(986)),
        )
    }
}
