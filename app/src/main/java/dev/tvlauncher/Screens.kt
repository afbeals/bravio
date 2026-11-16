package dev.tvlauncher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun onOff(value: Boolean) = if (value) "On" else "Off"

/** Focus can be requested before the target is attached (lazy lists, first frame), so retry briefly. */
suspend fun FocusRequester.requestFocusWhenReady() {
    repeat(20) {
        if (runCatching { requestFocus() }.getOrDefault(false)) return
        delay(30)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) { Text(text) }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, color = Color(0xFF90A4AE), modifier = Modifier.padding(top = 12.dp))
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: Config,
    update: ((Config) -> Config) -> Unit,
    onHidden: () -> Unit,
    onDone: () -> Unit,
) {
    val profile = config.activeProfile
    val context = LocalContext.current
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    val imageAvailable = remember { wallpaperImageFile(context) != null }
    val dateSample = remember(profile.datePattern) {
        runCatching { LocalDate.now().format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("?")
    }
    var confirmRestore by remember { mutableStateOf(false) }
    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    fun editProfile(transform: (Profile) -> Profile) = update { c -> c.updateActive(transform) }

    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 32.dp)) {
            Text("Launcher settings", fontSize = 28.sp, color = Color.White)
            Text("Profile: ${profile.name}", fontSize = 18.sp, color = Color(0xFFB0BEC5))
            LazyColumn(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { SectionTitle("Profiles") }
                item {
                    SettingButton(
                        "Switch profile (now ${profile.name}, ${config.profiles.size} total)",
                        { update { it.switchToNextProfile() } },
                        Modifier.focusRequester(first),
                    )
                }
                item { SettingButton("Add profile", { update { it.addProfile() } }) }
                if (config.profiles.size > 1) {
                    item { SettingButton("Delete profile ${profile.name}", { update { it.removeActiveProfile() } }) }
                }

                item { SectionTitle("Clock and date (this profile)") }
                item { SettingButton("Clock: ${onOff(profile.showClock)}", { editProfile { it.copy(showClock = !it.showClock) } }) }
                item {
                    SettingButton(
                        "Time format: ${if (profile.clock24h) "24-hour" else "12-hour"}",
                        { editProfile { it.copy(clock24h = !it.clock24h) } },
                    )
                }
                item { SettingButton("Date: ${onOff(profile.showDate)}", { editProfile { it.copy(showDate = !it.showDate) } }) }
                item { SettingButton("Date format: $dateSample", { editProfile { it.nextDatePattern() } }) }

                item { SectionTitle("Rows (this profile)") }
                item { SettingButton("Favorites row: ${onOff(profile.showFavorites)}", { editProfile { it.copy(showFavorites = !it.showFavorites) } }) }
                item { SettingButton("Recently used row: ${onOff(profile.showRecent)}", { editProfile { it.copy(showRecent = !it.showRecent) } }) }
                item { SettingButton("Most used row: ${onOff(profile.showMostUsed)}", { editProfile { it.copy(showMostUsed = !it.showMostUsed) } }) }
                item { SettingButton("HDMI inputs row: ${onOff(profile.showHdmi)}", { editProfile { it.copy(showHdmi = !it.showHdmi) } }) }
                item { SettingButton("Quick settings row: ${onOff(profile.showQuickSettings)}", { editProfile { it.copy(showQuickSettings = !it.showQuickSettings) } }) }
                item { SettingButton("\"Other apps\" row: ${onOff(profile.showOtherRow)}", { editProfile { it.copy(showOtherRow = !it.showOtherRow) } }) }
                item { SettingButton("Hidden apps (${profile.hidden.size})", onHidden) }

                item { SectionTitle("Look") }
                item { SettingButton("Wallpaper: ${wallpaperName(profile.wallpaper)}", { editProfile { it.nextWallpaper(imageAvailable) } }) }
                item { SettingButton("Dim wallpaper in the dark: ${onOff(config.dimAtNight)}", { update { it.copy(dimAtNight = !it.dimAtNight) } }) }
                item { SettingButton("Focus style: ${config.focusStyle}", { update { it.nextFocusStyle() } }) }

                item { SectionTitle("Behavior") }
                item {
                    SettingButton(
                        "On launch: ${if (config.askProfileAtLaunch) "ask who's watching" else "open the home screen"}",
                        { update { it.copy(askProfileAtLaunch = !it.askProfileAtLaunch) } },
                    )
                }
                item {
                    SettingButton(
                        "Back on the home screen: ${if (config.backShowsClock) "show the clock" else "do nothing"}",
                        { update { it.copy(backShowsClock = !it.backShowsClock) } },
                    )
                }

                item { SectionTitle("Backup") }
                item {
                    SettingButton("Back up settings to a file", {
                        toast(if (Backup.save(context, config)) "Saved ${Backup.FILE_NAME}" else "Could not save the backup")
                    })
                }
                item {
                    SettingButton(
                        if (confirmRestore) "Press again to replace ALL settings" else "Restore settings from backup",
                        {
                            when {
                                !Backup.exists(context) -> toast("No ${Backup.FILE_NAME} found")
                                !confirmRestore -> confirmRestore = true
                                else -> {
                                    confirmRestore = false
                                    val restored = Backup.load(context)
                                    if (restored == null) {
                                        toast("The backup file is not valid. Nothing was changed.")
                                    } else {
                                        update { restored }
                                        toast("Settings restored")
                                    }
                                }
                            }
                        },
                    )
                }

                item { SettingButton("Done", onDone) }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfilePicker(config: Config, onPick: (String) -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        WallpaperBackground(config.activeProfile)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Who's watching?", fontSize = 32.sp, color = Color.White)
            config.profiles.forEachIndexed { index, p ->
                val modifier = if (index == 0) Modifier.focusRequester(first) else Modifier
                Button(onClick = { onPick(p.id) }, modifier = modifier) { Text(p.name) }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HiddenAppsScreen(profile: Profile, labelOf: (String) -> String, onUnhide: (String) -> Unit, onDone: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestFocusWhenReady() }
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Hidden apps (${profile.hidden.size})", fontSize = 28.sp, color = Color.White)
            Button(onClick = onDone, modifier = Modifier.focusRequester(first)) { Text("Done") }
            if (profile.hidden.isEmpty()) Text("No apps are hidden.", fontSize = 18.sp, color = Color(0xFFB0BEC5))
            val hidden = remember(profile.hidden) { profile.hidden.sortedBy { labelOf(it).lowercase() } }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hidden, key = { it }) { pkg ->
                    Button(onClick = { onUnhide(pkg) }) { Text("Unhide ${labelOf(pkg)}") }
                }
            }
        }
    }
}

/** A single-line text box with a visible focus border (the TV keyboard opens when it is focused). */
@Composable
fun NameField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    onDone: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier
            .width(460.dp)
            .onFocusChanged { focused = it.isFocused }
            .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
            .border(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color(0x66FFFFFF), RoundedCornerShape(8.dp))
            .padding(16.dp),
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 24.sp),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, color = Color(0x99FFFFFF), fontSize = 24.sp)
            inner()
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    repo: AppRepository,
    apps: List<AppEntry>,
    profile: Profile,
    style: String,
    onOpen: (AppEntry) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val field = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        field.requestFocusWhenReady()
        keyboard?.show()
    }
    val results = remember(query, apps, profile) {
        val needle = query.trim()
        apps.filter { it.packageName !in profile.hidden }
            .filter { profile.labelFor(it.packageName, it.label).contains(needle, ignoreCase = true) }
    }
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Search apps", fontSize = 28.sp, color = Color.White)
            NameField(query, { query = it }, Modifier.focusRequester(field), hint = "Type an app name", onDone = { keyboard?.hide() })
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(results, key = { it.packageName }) { app ->
                    AppCard(repo, app, profile.labelFor(app.packageName, app.label), style, onOpen = { onOpen(app) }, onMenu = { onOpen(app) })
                }
            }
            if (results.isEmpty()) Text("No matching apps.", fontSize = 18.sp, color = Color(0xFFB0BEC5))
        }
    }
}

/** Full-screen drifting clock. Any key returns to the home screen. */
@Composable
fun ClockScreen(profile: Profile, onExit: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusWhenReady() }
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val (fx, fy) = driftFraction(now.hour * 60 + now.minute)
    val time = now.format(DateTimeFormatter.ofPattern(if (profile.clock24h) "HH:mm" else "h:mm a"))
    val date = runCatching { now.format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("")
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onExit()
                    true
                } else {
                    false
                }
            }
            .focusRequester(focus)
            .focusable(),
        contentAlignment = BiasAlignment(fx * 2 - 1, fy * 2 - 1),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, fontSize = 96.sp, color = Color(0xFFC8C8C8))
            if (profile.showDate) Text(date, fontSize = 28.sp, color = Color(0xFF8C8C8C))
        }
    }
}

data class MenuActions(
    val onOpen: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onAddTo: (String) -> Unit,
    val onRemoveFromCategories: () -> Unit,
    val onMove: (Int) -> Unit,
    val onRename: (String) -> Unit,
    val onHide: () -> Unit,
    val onInfo: () -> Unit,
    val onClose: () -> Unit,
)

private enum class MenuStage { Main, Categories, Rename }

/** Long-press menu for one app. [row] is where it was opened from; moving only applies to favorites and categories. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppMenu(app: AppEntry, profile: Profile, row: RowSpec?, actions: MenuActions) {
    var stage by remember { mutableStateOf(MenuStage.Main) }
    var name by remember { mutableStateOf(profile.labelFor(app.packageName, app.label)) }
    val first = remember { FocusRequester() }
    val field = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(stage) {
        if (stage == MenuStage.Rename) {
            field.requestFocusWhenReady()
            keyboard?.show()
        } else {
            first.requestFocusWhenReady()
        }
    }
    val canMove = row != null && row.packages.size > 1 && (row.kind == RowKind.Favorites || row.kind == RowKind.Category)

    val entries: List<Pair<String, () -> Unit>> = when (stage) {
        MenuStage.Main -> buildList {
            add("Open" to actions.onOpen)
            add((if (profile.isFavorite(app.packageName)) "Remove from favorites" else "Add to favorites") to actions.onToggleFavorite)
            add("Add to category..." to { stage = MenuStage.Categories })
            if (profile.inAnyCategory(app.packageName)) add("Remove from categories" to actions.onRemoveFromCategories)
            if (canMove) {
                add("Move left" to { actions.onMove(-1) })
                add("Move right" to { actions.onMove(1) })
            }
            add("Rename..." to { stage = MenuStage.Rename })
            add("Hide from home" to actions.onHide)
            add("App info" to actions.onInfo)
            add("Cancel" to actions.onClose)
        }
        MenuStage.Categories -> buildList {
            (profile.categories.map { it.name } + CATEGORY_PRESETS).distinct().forEach { category ->
                add(category to { actions.onAddTo(category) })
            }
            add("Back" to { stage = MenuStage.Main })
        }
        MenuStage.Rename -> emptyList()
    }

    Box(Modifier.fillMaxSize().background(Color(0xCC000000)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(profile.labelFor(app.packageName, app.label), fontSize = 28.sp, color = Color.White)
            if (stage == MenuStage.Rename) {
                NameField(name, { name = it }, Modifier.focusRequester(field), hint = "App name", onDone = { actions.onRename(name) })
                Button(onClick = { actions.onRename(name) }) { Text("Save") }
                Button(onClick = { actions.onRename("") }) { Text("Use the original name") }
                Button(onClick = { stage = MenuStage.Main }) { Text("Back") }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemsIndexed(entries) { index, (text, action) ->
                        Button(onClick = action, modifier = if (index == 0) Modifier.focusRequester(first) else Modifier) { Text(text) }
                    }
                }
            }
        }
    }
}
