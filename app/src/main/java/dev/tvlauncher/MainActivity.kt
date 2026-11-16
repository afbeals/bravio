package dev.tvlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AppRepository(this)
        store = ConfigStore(this)
        inputs = TvInputs(this)
        apps = repo.load()
        tiles = quickTiles(this)
        setContent { Home(repo, store, apps, inputList, tiles, homePresses, this::start) }
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

private enum class Screen { Home, Settings, Picker, Search, Hidden, Clock }

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
    start: (Intent) -> Unit,
) {
    var config by remember { mutableStateOf(store.load()) }
    fun update(transform: (Config) -> Config) {
        config = transform(config)
        store.save(config)
    }

    val profile = config.activeProfile
    val style = config.focusStyle
    val byPackage = remember(apps) { apps.associateBy { it.packageName } }
    val rows = remember(apps, profile) { buildRows(apps.map { it.packageName }, profile) }
    var menu by remember { mutableStateOf<MenuTarget?>(null) }
    var screen by remember { mutableStateOf(if (config.askProfileAtLaunch) Screen.Picker else Screen.Home) }
    // The long press opens the menu while OK is still held. Releasing OK must not also click
    // whatever has focus by then (the menu's first button), so the first release is swallowed.
    var swallowRelease by remember { mutableStateOf(false) }
    val firstCard = remember { FocusRequester() }
    val dim = rememberDim(config.dimAtNight)
    val network = rememberNetworkState()

    fun open(app: AppEntry) {
        update { it.updateActive { p -> p.recordLaunch(app.packageName) } }
        start(app.intent)
    }

    // A launcher must never exit on Back: it closes the menu, then leaves a sub-screen, then shows the clock.
    BackHandler(enabled = true) {
        when {
            menu != null -> menu = null
            screen != Screen.Home -> screen = Screen.Home
            config.backShowsClock -> screen = Screen.Clock
        }
    }

    // Home pressed while the launcher is already open: return to the home screen.
    LaunchedEffect(homePresses) {
        if (homePresses > 0) {
            menu = null
            screen = Screen.Home
        }
    }
    LaunchedEffect(screen) {
        if (screen == Screen.Home) firstCard.requestFocusWhenReady()
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        CompositionLocalProvider(LocalDim provides dim) {
            when (screen) {
                Screen.Picker -> ProfilePicker(config) { id ->
                    update { it.copy(activeProfileId = id) }
                    screen = Screen.Home
                }

                Screen.Settings -> SettingsScreen(
                    config = config,
                    update = ::update,
                    onHidden = { screen = Screen.Hidden },
                    onDone = { screen = Screen.Home },
                )

                Screen.Hidden -> HiddenAppsScreen(
                    profile = profile,
                    labelOf = { pkg -> byPackage[pkg]?.label ?: pkg },
                    onUnhide = { pkg -> update { it.updateActive { p -> p.unhide(pkg) } } },
                    onDone = { screen = Screen.Settings },
                )

                Screen.Search -> SearchScreen(repo, apps, profile, style) { app ->
                    screen = Screen.Home
                    open(app)
                }

                Screen.Clock -> ClockScreen(profile) { screen = Screen.Home }

                Screen.Home -> Box(
                    Modifier.fillMaxSize().onPreviewKeyEvent { event ->
                        val isSelectKey = event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter
                        if (swallowRelease && event.type == KeyEventType.KeyUp && isSelectKey) {
                            swallowRelease = false
                            true
                        } else {
                            false
                        }
                    },
                ) {
                    WallpaperBackground(profile)
                    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 32.dp)) {
                        Header(profile, network, onSearch = { screen = Screen.Search })
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            items(rows.size, key = { "${rows[it].kind}:${rows[it].title}" }) { rowIndex ->
                                val row = rows[rowIndex]
                                Text(row.title, fontSize = 22.sp, color = Color(0xFFB0BEC5))
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(row.packages.size, key = { row.packages[it] }) { index ->
                                        val app = byPackage[row.packages[index]] ?: return@items
                                        val isFirst = rowIndex == 0 && index == 0
                                        AppCard(
                                            repo = repo,
                                            app = app,
                                            label = profile.labelFor(app.packageName, app.label),
                                            style = style,
                                            onOpen = { open(app) },
                                            onMenu = {
                                                swallowRelease = true
                                                menu = MenuTarget(app, row)
                                            },
                                            modifier = if (isFirst) Modifier.focusRequester(firstCard) else Modifier,
                                        )
                                    }
                                }
                            }
                            if (profile.showHdmi && inputList.isNotEmpty()) {
                                item(key = "hdmi") {
                                    Text("Inputs", fontSize = 22.sp, color = Color(0xFFB0BEC5))
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(inputList, key = { it.id }) { input ->
                                            TileCard(input.label, input.status, style, onClick = { start(TvInputs.switchIntent(input.id)) })
                                        }
                                    }
                                }
                            }
                            if (profile.showQuickSettings && tiles.isNotEmpty()) {
                                item(key = "quick") {
                                    Text("Quick settings", fontSize = 22.sp, color = Color(0xFFB0BEC5))
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(tiles, key = { it.label }) { tile ->
                                            TileCard(tile.label, null, style, onClick = { start(tile.intent) })
                                        }
                                    }
                                }
                            }
                            item(key = "settings") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { screen = Screen.Settings }) { Text("Launcher settings") }
                                    if (profile.hidden.isNotEmpty()) {
                                        Button(onClick = { screen = Screen.Hidden }) { Text("Hidden apps (${profile.hidden.size})") }
                                    }
                                }
                            }
                        }
                    }
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
                                    update { it.updateActive { p -> p.toggleFavorite(pkg) } }
                                },
                                onAddTo = { name ->
                                    menu = null
                                    update { it.updateActive { p -> p.withApp(pkg, name) } }
                                },
                                onRemoveFromCategories = {
                                    menu = null
                                    update { it.updateActive { p -> p.withoutApp(pkg) } }
                                },
                                // Stays open so an app can be nudged several places in a row.
                                onMove = { delta ->
                                    val origin = target.row
                                    if (origin != null) {
                                        when (origin.kind) {
                                            RowKind.Favorites -> update { it.updateActive { p -> p.moveInFavorites(pkg, delta) } }
                                            RowKind.Category -> update { it.updateActive { p -> p.moveInCategory(pkg, origin.title, delta) } }
                                            else -> Unit
                                        }
                                    }
                                },
                                onRename = { name ->
                                    menu = null
                                    update { it.updateActive { p -> p.rename(pkg, name) } }
                                },
                                onHide = {
                                    menu = null
                                    update { it.updateActive { p -> p.copy(hidden = p.hidden + pkg) } }
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
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Header(profile: Profile, network: NetworkState, onSearch: () -> Unit) {
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val timeFormat = if (profile.clock24h) "HH:mm" else "h:mm a"
    val dateText = remember(now, profile.datePattern) {
        runCatching { now.format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (profile.showClock) {
                Text(now.format(DateTimeFormatter.ofPattern(timeFormat)), fontSize = 48.sp, color = Color.White)
            }
            if (profile.showClock && profile.showDate) Spacer(Modifier.width(24.dp))
            if (profile.showDate) {
                Text(dateText, fontSize = 22.sp, color = Color(0xFFB0BEC5), modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            NetworkBadge(network)
            Button(onClick = onSearch) { Text("Search") }
        }
    }
}

@Composable
private fun NetworkBadge(state: NetworkState) {
    val (label, color) = when (state) {
        NetworkState.WiFi -> "Wi-Fi" to Color(0xFF66BB6A)
        NetworkState.Ethernet -> "Ethernet" to Color(0xFF66BB6A)
        NetworkState.Other -> "Online" to Color(0xFF66BB6A)
        NetworkState.NoInternet -> "No internet" to Color(0xFFFFB300)
        NetworkState.Offline -> "Offline" to Color(0xFFEF5350)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("●", color = color, fontSize = 16.sp)
        Text(label, color = Color(0xFFB0BEC5), fontSize = 18.sp)
    }
}
