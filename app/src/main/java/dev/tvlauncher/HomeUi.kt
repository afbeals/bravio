package dev.tvlauncher

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** What Home needs to know and do. Everything that changes the saved config goes through these callbacks. */
/** The app being moved with Left and Right: Home labels its row heading and counter. */
class ReorderInfo(val rowId: String, val pkg: String, val name: String)

class HomeActions(
    val open: (AppEntry) -> Unit,
    val menu: (AppEntry, RowSpec) -> Unit,
    val start: (Intent) -> Unit,
    val search: () -> Unit,
    val settings: () -> Unit,
    val profiles: () -> Unit,
    val hidden: () -> Unit,
    val unhideAll: () -> Unit,
    val tvSettings: () -> Unit,
)

private val FOCUS_ROOM = px(24)

private fun rowIcon(row: RowSpec): Int = when (row.kind) {
    RowKind.Favorites -> R.drawable.ic_star
    RowKind.Recent -> R.drawable.ic_clock
    RowKind.MostUsed -> R.drawable.ic_upload
    RowKind.Other -> R.drawable.ic_hard_drive
    RowKind.Inputs -> R.drawable.ic_monitor
    RowKind.Quick -> R.drawable.ic_sliders_horizontal
    RowKind.Category -> when (row.title.lowercase()) {
        "video" -> R.drawable.ic_circle_play
        "games" -> R.drawable.ic_gamepad_2
        "music" -> R.drawable.ic_volume_2
        "kids", "family" -> R.drawable.ic_user
        else -> R.drawable.ic_hard_drive
    }
}

@Composable
fun HomeContent(
    repo: AppRepository,
    byPackage: Map<String, AppEntry>,
    profile: Profile,
    rows: List<RowSpec>,
    inputs: List<InputEntry>,
    tiles: List<QuickTile>,
    style: String,
    network: NetworkState,
    reorder: ReorderInfo?,
    homePresses: Int,
    actions: HomeActions,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val rowFocus = remember(rows.size) { List(rows.size) { FocusRequester() } }
    val emptyFocus = remember { FocusRequester() }
    var focusedRow by remember { mutableStateOf<String?>(null) }
    var focusedIndex by remember { mutableStateOf<Int?>(null) }
    var longName by remember { mutableStateOf<String?>(null) }

    // Also when Home is pressed while the launcher is already showing, so focus never ends up on a stray button.
    LaunchedEffect(homePresses) {
        if (rows.isEmpty()) emptyFocus.requestFocusWhenReady() else rowFocus.first().requestFocusWhenReady()
    }

    fun jumpTo(index: Int) {
        scope.launch {
            listState.scrollToItem(index)
            rowFocus.getOrNull(index)?.requestFocusWhenReady()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Sidebar(
            profileName = profile.name,
            rows = rows,
            style = style,
            onHome = { if (rows.isNotEmpty()) jumpTo(0) },
            onSearch = actions.search,
            onProfiles = actions.profiles,
            onRow = ::jumpTo,
            onSettings = actions.settings,
            onTvSettings = actions.tvSettings,
            modifier = Modifier.padding(start = Ink.safeHorizontal, top = Ink.safeVertical),
        )
        Column(Modifier.fillMaxSize().padding(start = px(448), top = px(64), end = Ink.safeHorizontal)) {
            Header(profile, network, homeSubtitle(profile.name, longName).takeIf { rows.isNotEmpty() } ?: profile.name)
            if (rows.isEmpty()) {
                EmptyHome(hasHidden = profile.hidden.isNotEmpty(), actions = actions, style = style, focus = emptyFocus)
            } else {
                LazyColumn(state = listState, modifier = Modifier.padding(end = px(40))) {
                    items(rows.size, key = { rows[it].id }) { rowIndex ->
                        val row = rows[rowIndex]
                        val tileCount = when (row.kind) {
                            RowKind.Inputs -> inputs.size
                            RowKind.Quick -> tiles.size
                            else -> 0
                        }
                        Column {
                            Row(Modifier.fillMaxWidth().height(px(44)), verticalAlignment = Alignment.CenterVertically) {
                                val moving = reorder?.takeIf { it.rowId == row.id }
                                Text(
                                    if (moving != null) "${row.title} · moving ${moving.name}" else row.title,
                                    style = InkType.section, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                                )
                                Text(
                                    if (moving != null) "${row.packages.indexOf(moving.pkg) + 1} / ${row.packages.size}"
                                    else rowCounter(row, tileCount, focusedIndex.takeIf { focusedRow == row.id }, profile.name),
                                    style = InkType.body,
                                    color = Ink.colors.muted,
                                    textAlign = TextAlign.End,
                                )
                            }
                            val first = Modifier.focusRequester(rowFocus[rowIndex])
                            // Room around the cards so a scaled or glowing focused card is not clipped by the list; the
                            // offset keeps the first card on the 448 px content edge.
                            LazyRow(
                                modifier = Modifier.offset(x = -FOCUS_ROOM),
                                contentPadding = PaddingValues(horizontal = FOCUS_ROOM, vertical = px(16)),
                                horizontalArrangement = Arrangement.spacedBy(Ink.gap24),
                            ) {
                                when (row.kind) {
                                    RowKind.Inputs -> items(inputs.size, key = { inputs[it].id }) { index ->
                                        val input = inputs[index]
                                        InputTile(
                                            input.label, input.status, style,
                                            onClick = { actions.start(TvInputs.switchIntent(input.id)) },
                                            modifier = if (index == 0) first else Modifier,
                                        )
                                    }

                                    RowKind.Quick -> items(tiles.size, key = { tiles[it].label }) { index ->
                                        val tile = tiles[index]
                                        QuickTileCard(
                                            tile.label, style,
                                            onClick = { actions.start(tile.intent) },
                                            modifier = if (index == 0) first else Modifier,
                                        )
                                    }

                                    else -> items(row.packages.size, key = { row.packages[it] }) { index ->
                                        val app = byPackage[row.packages[index]] ?: return@items
                                        val name = profile.labelFor(app.packageName, app.label)
                                        AppCard(
                                            repo = repo,
                                            app = app,
                                            label = name,
                                            favorite = profile.isFavorite(app.packageName),
                                            style = style,
                                            onOpen = { actions.open(app) },
                                            onMenu = { actions.menu(app, row) },
                                            onFocus = { focused, cutOff ->
                                                if (focused) {
                                                    focusedRow = row.id
                                                    focusedIndex = index
                                                    longName = name.takeIf { cutOff }
                                                }
                                            },
                                            modifier = if (index == 0) first else Modifier,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (profile.hidden.isNotEmpty()) {
                        item(key = "footer") {
                            Row(Modifier.padding(top = px(32)), horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                                InkButton("Unhide all (${profile.hidden.size})", actions.unhideAll, style = style)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHome(hasHidden: Boolean, actions: HomeActions, style: String, focus: FocusRequester) {
    Column(Modifier.padding(top = px(58))) {
        Column(Modifier.size(px(1100), px(400)).padding(px(32)), verticalArrangement = Arrangement.Center) {
            Text("A clean start", style = InkType.title)
            Spacer(Modifier.height(px(24)))
            Text(
                "Add apps to a category or unhide an app to fill this profile.",
                style = InkType.body,
                color = Ink.colors.muted,
            )
        }
        Row(Modifier.padding(top = px(60)), horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
            InkButton("Hidden apps", actions.hidden, Modifier.focusRequester(focus), primary = true, style = style)
            InkButton("Switch profile", actions.profiles, style = style)
        }
    }
}

@Composable
fun Sidebar(
    profileName: String,
    rows: List<RowSpec>,
    style: String,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onProfiles: () -> Unit,
    onRow: (Int) -> Unit,
    onSettings: () -> Unit,
    onTvSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .size(px(296), px(972))
            .background(Ink.colors.sidebar, RoundedCornerShape(Ink.radius16))
            .padding(Ink.gap24),
        verticalArrangement = Arrangement.spacedBy(Ink.gap16),
    ) {
        Text("LIBRARY", style = InkType.section, color = Ink.colors.focus, modifier = Modifier.height(px(40)))
        NavItem(R.drawable.ic_user, profileName, selected = false, style, onProfiles)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Ink.gap12)) {
            item { NavItem(R.drawable.ic_tv, "Home", selected = true, style, onHome) }
            item { NavItem(R.drawable.ic_search, "Search", selected = false, style, onSearch) }
            items(rows.size, key = { rows[it].id }) { index ->
                NavItem(rowIcon(rows[index]), rows[index].title, selected = false, style) { onRow(index) }
            }
        }
        InkButton("Launcher settings", onSettings, Modifier.fillMaxWidth(), style = style)
        InkButton("TV settings", onTvSettings, Modifier.fillMaxWidth(), style = style)
    }
}

@Composable
private fun NavItem(icon: Int, label: String, selected: Boolean, style: String, onClick: () -> Unit) {
    val c = Ink.colors
    InkSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(px(60)),
        style = style,
        fill = if (selected) c.panel else Color.Transparent,
    ) { _ ->
        Row(Modifier.fillMaxSize().padding(px(16)), verticalAlignment = Alignment.CenterVertically) {
            InkIcon(icon, tint = if (selected) c.text else c.muted)
            Spacer(Modifier.width(px(12)))
            Text(label, style = InkType.label, color = if (selected) c.text else c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Header(profile: Profile, network: NetworkState, subtitle: String) {
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
    val online = network != NetworkState.Offline && network != NetworkState.NoInternet
    Row(Modifier.fillMaxWidth().height(px(148))) {
        Column(Modifier.weight(1f)) {
            Text("Your library", style = InkType.title, maxLines = 1)
            Spacer(Modifier.height(px(8)))
            Text(subtitle, style = InkType.body, color = Ink.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(Modifier.width(px(360)).fillMaxHeight(), horizontalAlignment = Alignment.End) {
            if (profile.showClock) {
                Text(now.format(DateTimeFormatter.ofPattern(timeFormat)), style = InkType.title, maxLines = 1)
            }
            // Offline is always shown (icon and words); the date only appears while online.
            if (!online || profile.showDate) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Ink.gap12)) {
                    InkIcon(if (online) R.drawable.ic_wifi else R.drawable.ic_wifi_off)
                    Text(if (online) dateText else "No internet", style = InkType.body, color = Ink.colors.muted, maxLines = 1)
                }
            }
        }
    }
}
