package dev.tvlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var repo: AppRepository
    private lateinit var store: ConfigStore
    private lateinit var inputs: TvInputs
    private var apps by mutableStateOf<List<AppEntry>>(emptyList())
    private var inputList by mutableStateOf<List<InputEntry>>(emptyList())
    private var tiles by mutableStateOf<List<QuickTile>>(emptyList())
    private var homePresses by mutableIntStateOf(0)
    private var backHolds by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AppRepository(this)
        store = ConfigStore(this)
        inputs = TvInputs(this)
        apps = repo.load()
        tiles = quickTiles(this)
        setContent { Home(repo, store, apps, inputList, tiles, homePresses, backHolds, this::start) }
    }

    // Holding Back on Home opens the clock; a short Back does nothing there. Tracking makes the key
    // report a long press, and a handled long press cancels the key-up so it is not also a short Back.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            event.startTracking()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backHolds++
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        // Cheap refresh so installs, removals and HDMI connection changes show up when returning home.
        apps = repo.load()
        inputList = inputs.load()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pressing Home while already running resets the launcher to its home screen.
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homePresses++
    }

    private fun start(intent: Intent) {
        runCatching { startActivity(intent) }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
            start(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

private enum class Screen { Home, Settings, Picker, Search, Hidden, RowOrder, Wallpaper, Clock }

private data class MenuTarget(val app: AppEntry, val row: RowSpec?)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Home(
    repo: AppRepository,
    store: ConfigStore,
    apps: List<AppEntry>,
    inputList: List<InputEntry>,
    tiles: List<QuickTile>,
    homePresses: Int,
    backHolds: Int,
    start: (Intent) -> Unit,
) {
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(store.loadResult()) }
    var config by remember { mutableStateOf((loaded as? ConfigLoad.Loaded)?.config ?: Config()) }
    var toast by remember { mutableStateOf<ToastMsg?>(null) }
    fun notify(text: String, error: Boolean = false) {
        toast = ToastMsg(text, error)
    }
    // A change only takes effect if it was saved, so what is on screen always matches what is on disk.
    fun update(transform: (Config) -> Config) {
        val next = transform(config)
        if (store.save(next)) {
            config = next
        } else {
            notify("Could not save changes. Try again.", error = true)
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(TOAST_MILLIS)
            toast = null
        }
    }
    if (loaded == ConfigLoad.Corrupt) {
        // The unreadable file is left alone until the user chooses what to do; nothing is saved meanwhile.
        InkTheme(false) {
            Box(Modifier.fillMaxSize()) {
                ConfigErrorScreen(
                    onRetry = {
                        loaded = store.loadResult()
                        (loaded as? ConfigLoad.Loaded)?.let { config = it.config }
                    },
                    onRestore = {
                        val backup = Backup.load(context)
                        when {
                            backup == null -> notify("No valid ${Backup.FILE_NAME} found.", error = true)
                            store.quarantineCorrupt() && store.save(backup) -> {
                                config = backup
                                loaded = ConfigLoad.Loaded(backup)
                            }
                            else -> notify("Could not restore the backup.", error = true)
                        }
                    },
                    onReset = {
                        if (store.quarantineCorrupt() && store.save(Config())) {
                            config = Config()
                            loaded = ConfigLoad.Loaded(config)
                        } else {
                            notify("Could not reset the settings.", error = true)
                        }
                    },
                )
                ToastHost(toast)
            }
        }
        return
    }

    val profile = config.activeProfile
    val style = config.focusStyle
    val byPackage = remember(apps) { apps.associateBy { it.packageName } }
    val rows = remember(apps, profile, inputList, tiles) {
        buildRows(apps.map { it.packageName }, profile, hasInputs = inputList.isNotEmpty(), hasQuickTiles = tiles.isNotEmpty())
    }
    var menu by remember { mutableStateOf<MenuTarget?>(null) }
    var reorder by remember { mutableStateOf<ReorderState?>(null) }
    var screen by remember { mutableStateOf(if (config.askProfileAtLaunch) Screen.Picker else Screen.Home) }
    // The long press opens the menu while OK is still held. Releasing OK must not also click
    // whatever has focus by then (the menu's first button), so the first release is swallowed.
    var swallowRelease by remember { mutableStateOf(false) }
    val dim = rememberDim(config.dimAtNight)
    val network = rememberNetworkState()

    val tvSettings = { start(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

    fun open(app: AppEntry) {
        update { it.updateActive { p -> p.recordLaunch(app.packageName) } }
        start(app.intent)
    }

    // A launcher must never exit on Back: it closes the menu, then leaves a sub-screen, and on Home does nothing.
    BackHandler(enabled = true) {
        when {
            reorder != null -> {
                reorder?.let { r -> update { r.original } }
                reorder = null
            }
            menu != null -> menu = null
            screen == Screen.Hidden || screen == Screen.RowOrder || screen == Screen.Wallpaper -> screen = Screen.Settings
            screen != Screen.Home -> screen = Screen.Home
        }
    }

    // Holding Back on Home opens the clock (see MainActivity.onKeyLongPress).
    LaunchedEffect(backHolds) {
        if (backHolds > 0 && screen == Screen.Home && menu == null) screen = Screen.Clock
    }

    // Home pressed while the launcher is already open: return to the home screen.
    LaunchedEffect(homePresses) {
        if (homePresses > 0) {
            menu = null
            screen = Screen.Home
        }
    }

    fun moveReordered(r: ReorderState, delta: Int) {
        when (r.row.kind) {
            RowKind.Favorites -> update { it.updateActive { p -> p.moveInFavorites(r.pkg, delta) } }
            RowKind.Category -> update { it.updateActive { p -> p.moveInCategory(r.pkg, r.row.title, delta) } }
            else -> Unit
        }
    }

    InkTheme(dim) {
        CompositionLocalProvider(LocalDim provides dim, LocalFocusSound provides profile.focusSound) {
          Box(Modifier.fillMaxSize()) {
            when (screen) {
                Screen.Picker -> ProfilePicker(
                    config,
                    onPick = { id ->
                        update { it.copy(activeProfileId = id) }
                        screen = Screen.Home
                    },
                    onCreate = { name ->
                        update { it.addProfile(name) }
                        screen = Screen.Home
                    },
                )

                Screen.Settings -> SettingsScreen(
                    config = config,
                    rows = rows,
                    onProfiles = { screen = Screen.Picker },
                    onSearch = { screen = Screen.Search },
                    onPreviewClock = { screen = Screen.Clock },
                    onTvSettings = tvSettings,
                    notify = { text, error -> notify(text, error) },
                    update = ::update,
                    onHidden = { screen = Screen.Hidden },
                    onWallpaper = { screen = Screen.Wallpaper },
                    onRowOrder = { screen = Screen.RowOrder },
                    onDone = { screen = Screen.Home },
                )

                Screen.Wallpaper -> WallpaperScreen(
                    profile = profile,
                    style = style,
                    onApply = { id ->
                        update { it.updateActive { p -> p.copy(wallpaper = id) } }
                        screen = Screen.Settings
                    },
                    onClose = { screen = Screen.Settings },
                )

                Screen.RowOrder -> RowOrderScreen(
                    profile = profile,
                    style = style,
                    onMove = { id, delta -> update { it.updateActive { p -> p.moveRow(id, delta) } } },
                    onReset = { update { it.updateActive { p -> p.resetRowOrder() } } },
                    onDone = { screen = Screen.Settings },
                )

                Screen.Hidden -> HiddenAppsScreen(
                    profile = profile,
                    style = style,
                    onUnhideAll = { update { it.updateActive { p -> p.copy(hidden = emptySet()) } } },
                    labelOf = { pkg -> byPackage[pkg]?.label ?: pkg },
                    onUnhide = { pkg -> update { it.updateActive { p -> p.unhide(pkg) } } },
                    onDone = { screen = Screen.Settings },
                )

                Screen.Search -> SearchScreen(repo, apps, profile, style) { app ->
                    screen = Screen.Home
                    open(app)
                }

                Screen.Clock -> ClockScreen(config) { screen = Screen.Home }

                Screen.Home -> Box(
                    Modifier.fillMaxSize().onPreviewKeyEvent { event ->
                        val isSelectKey = event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter
                        val r = reorder
                        when {
                            // Reorder mode: Left and Right move the app, OK saves, and the other arrows are held still.
                            r != null -> when {
                                event.key == Key.DirectionLeft || event.key == Key.DirectionRight -> {
                                    if (event.type == KeyEventType.KeyDown) moveReordered(r, if (event.key == Key.DirectionLeft) -1 else 1)
                                    true
                                }
                                event.key == Key.DirectionUp || event.key == Key.DirectionDown -> true
                                isSelectKey -> {
                                    if (event.type == KeyEventType.KeyUp) {
                                        reorder = null
                                        notify("${r.row.title} order saved")
                                    }
                                    true
                                }
                                else -> false
                            }
                            swallowRelease && event.type == KeyEventType.KeyUp && isSelectKey -> {
                                swallowRelease = false
                                true
                            }
                            else -> false
                        }
                    },
                ) {
                    WallpaperBackground(profile, WallpaperSurface.Reading)
                    HomeContent(
                        repo = repo,
                        byPackage = byPackage,
                        profile = profile,
                        rows = rows,
                        inputs = inputList,
                        tiles = tiles,
                        style = style,
                        network = network,
                        homePresses = homePresses,
                        reorder = reorder?.let { ReorderInfo(it.row.id, it.pkg, it.name) },
                        actions = HomeActions(
                            open = ::open,
                            menu = { app, row ->
                                swallowRelease = true
                                menu = MenuTarget(app, row)
                            },
                            start = start,
                            search = { screen = Screen.Search },
                            settings = { screen = Screen.Settings },
                            profiles = { screen = Screen.Picker },
                            hidden = { screen = Screen.Hidden },
                            tvSettings = tvSettings,
                            unhideAll = { update { it.updateActive { p -> p.copy(hidden = emptySet()) } } },
                        ),
                    )
                    menu?.let { target ->
                        val app = target.app
                        val pkg = app.packageName
                        AppMenu(
                            app = app,
                            profile = profile,
                            row = target.row,
                            actions = MenuActions(
                                onOpen = { menu = null; open(app) },
                                onToggleFavorite = {
                                    menu = null
                                    val adding = !profile.isFavorite(pkg)
                                    update { it.updateActive { p -> p.toggleFavorite(pkg) } }
                                    notify(if (adding) "Added to Favorites" else "Removed from Favorites")
                                },
                                onAddTo = { name ->
                                    menu = null
                                    update { it.updateActive { p -> p.withApp(pkg, name) } }
                                    notify("Added to $name")
                                },
                                onRemoveFromCategories = {
                                    menu = null
                                    update { it.updateActive { p -> p.withoutApp(pkg) } }
                                    notify("Removed from categories")
                                },
                                onReorder = {
                                    menu = null
                                    target.row?.let { reorder = ReorderState(pkg, profile.labelFor(pkg, app.label), it, config) }
                                },
                                onRename = { name ->
                                    menu = null
                                    update { it.updateActive { p -> p.rename(pkg, name) } }
                                },
                                onHide = {
                                    menu = null
                                    update { it.updateActive { p -> p.copy(hidden = p.hidden + pkg) } }
                                    notify("${profile.labelFor(pkg, app.label)} hidden from Home")
                                },
                                onInfo = {
                                    menu = null
                                    start(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                },
                                onClose = { menu = null },
                            ),
                        )
                    }
                }
            }
            if (screen == Screen.Home && reorder != null) {
                Text(
                    "Left / Right  Move     OK  Save     Back  Cancel",
                    style = InkType.body, color = Ink.colors.muted,
                    modifier = Modifier.padding(start = px(448), top = px(986)),
                )
            }
            ToastHost(toast)
          }
        }
    }
}

private data class ReorderState(val pkg: String, val name: String, val row: RowSpec, val original: Config)
