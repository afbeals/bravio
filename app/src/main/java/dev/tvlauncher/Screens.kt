package dev.tvlauncher

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextOverflow
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
fun RenameProfileDialog(currentName: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    val field = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        field.requestFocusWhenReady()
        keyboard?.show()
    }
    // Back closes only this dialog, not the Settings screen behind it.
    BackHandler(enabled = true) { onCancel() }
    Box(Modifier.fillMaxSize().background(Color(0xCC000000)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rename profile", fontSize = 28.sp, color = Ink.colors.text)
            NameField(name, { name = it }, Modifier.focusRequester(field), hint = "Profile name", onDone = { onSave(name) })
            Row(horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                InkButton("Cancel", onCancel)
                InkButton("Save name", { onSave(name) }, primary = true)
            }
        }
    }
}

@Composable
fun ProfilePicker(config: Config, onPick: (String) -> Unit, onCreate: (String) -> Unit) {
    val first = remember { FocusRequester() }
    var creating by remember { mutableStateOf(false) }
    LaunchedEffect(creating) { if (!creating) first.requestFocusWhenReady() }
    val style = config.focusStyle
    // One to four tiles share a row; five or six wrap into rows of three.
    val tiles: List<ProfileTile> = config.profiles.map { ProfileTile(it.id, it.name.take(1).uppercase(), it.name, it.id == config.activeProfile.id) } +
        if (config.canAddProfile()) listOf(ProfileTile(null, "+", "Create new", false)) else emptyList()
    val rows = if (tiles.size <= 4) listOf(tiles) else tiles.chunked(3)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        WallpaperBackground(config.activeProfile)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Ink.gap32)) {
            Text("Who’s watching?", style = InkType.title)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(px(40))) {
                    row.forEach { tile ->
                        val isFirst = tile === tiles.first()
                        ProfileTileCard(
                            tile, style,
                            onClick = { if (tile.id == null) creating = true else onPick(tile.id) },
                            modifier = if (isFirst) Modifier.focusRequester(first) else Modifier,
                        )
                    }
                }
            }
        }
        if (creating) {
            CreateProfileDialog(config, onCreate = { name -> creating = false; onCreate(name) }, onCancel = { creating = false })
        }
    }
}

private data class ProfileTile(val id: String?, val initial: String, val name: String, val current: Boolean)

@Composable
private fun ProfileTileCard(tile: ProfileTile, style: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Ink.colors
    var focused by remember { mutableStateOf(false) }
    InkSurface(onClick = onClick, modifier = modifier.size(px(244), px(272)), style = style, onFocusChange = { focused = it }) { _ ->
        Column(
            Modifier.fillMaxSize().padding(px(24)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Ink.gap16),
        ) {
            Box(
                Modifier.size(px(112)).background(c.raised, RoundedCornerShape(Ink.radius24)),
                contentAlignment = Alignment.Center,
            ) {
                if (tile.id == null) {
                    InkIcon(R.drawable.ic_plus, size = px(48), tint = c.focus)
                } else {
                    Text(tile.initial, style = InkType.title, color = c.focus)
                }
            }
            // The focused name turns iris, matching the outline.
            Text(tile.name, style = InkType.section, color = if (focused) c.focus else c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (tile.current) InkIcon(R.drawable.ic_check, size = px(24), tint = c.focus)
        }
    }
}

@Composable
fun CreateProfileDialog(config: Config, onCreate: (String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(config.suggestProfileName()) }
    val field = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        field.requestFocusWhenReady()
        keyboard?.show()
    }
    val problem = validateProfileName(name, config.profiles.map { it.name })
    BackHandler(enabled = true) { onCancel() }
    Box(Modifier.fillMaxSize().background(Color(0xD1000000)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Ink.gap16)) {
            Text("Create profile", style = InkType.title)
            NameField(name, { name = it.take(MAX_PROFILE_NAME_LENGTH) }, Modifier.focusRequester(field), hint = "Profile name", onDone = { if (problem == null) onCreate(name) })
            Text(
                when (problem) {
                    NameProblem.Empty -> "Enter a profile name to continue."
                    NameProblem.Taken -> "That name is already used by another profile."
                    null -> " "
                },
                style = InkType.body,
                color = Ink.colors.muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Ink.gap24)) {
                InkButton("Cancel", onCancel)
                InkButton("Create", { onCreate(name) }, primary = true, enabled = problem == null)
            }
        }
    }
}

/**
 * Row order for the active profile. Every row is listed (even ones that are off or empty) so the order
 * is kept for when they come back. Focus stays on the row being moved, so OK can be pressed repeatedly.
 */


/** A single-line text box with the design's 6 px iris outline when focused (the TV keyboard opens then). */
@Composable
fun NameField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    onDone: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val c = Ink.colors
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier
            .width(px(1000))
            .onFocusChanged { focused = it.isFocused }
            .background(c.panel, RoundedCornerShape(Ink.radius8))
            .border(if (focused) Ink.focusWidth else 0.dp, c.focus, RoundedCornerShape(Ink.radius8))
            .padding(px(24)),
        singleLine = true,
        textStyle = InkType.label.copy(color = c.text),
        cursorBrush = SolidColor(c.focus),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, style = InkType.label, color = c.muted)
            inner()
        },
    )
}

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
    val searching = query.isNotBlank()
    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(profile)
        Column(Modifier.fillMaxSize().padding(start = Ink.safeHorizontal, top = px(64), end = Ink.safeHorizontal)) {
            Text("Search", style = InkType.title)
            Spacer(Modifier.height(px(8)))
            Text(
                when {
                    !searching -> "Search app names"
                    results.isEmpty() -> "No apps found"
                    else -> "${countOf(results.size, "app", "apps")} found"
                },
                style = InkType.body, color = Ink.colors.muted,
            )
            Spacer(Modifier.height(px(28)))
            NameField(query, { query = it }, Modifier.focusRequester(field), hint = "Search app names", onDone = { keyboard?.hide() })
            Spacer(Modifier.height(px(32)))
            if (searching && results.isEmpty()) {
                InkButton("Edit search", { field.requestFocus(); keyboard?.show() }, style = style)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(px(244)),
                    contentPadding = PaddingValues(px(16)),
                    horizontalArrangement = Arrangement.spacedBy(Ink.gap24),
                    verticalArrangement = Arrangement.spacedBy(Ink.gap24),
                ) {
                    items(results, key = { it.packageName }) { app ->
                        AppCard(
                            repo, app, profile.labelFor(app.packageName, app.label), profile.isFavorite(app.packageName), style,
                            onOpen = { onOpen(app) }, onMenu = { onOpen(app) }, onFocus = { _, _ -> },
                        )
                    }
                }
            }
        }
    }
}


data class MenuActions(
    val onOpen: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onAddTo: (String) -> Unit,
    val onRemoveFromCategories: () -> Unit,
    val onReorder: () -> Unit,
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
            if (canMove) add("Reorder in row" to actions.onReorder)
            add("Rename..." to { stage = MenuStage.Rename })
            add("Hide from home" to actions.onHide)
            add("App info" to actions.onInfo)
            add("Cancel" to actions.onClose)
        }
        MenuStage.Categories -> buildList {
            (profile.categories.map { it.name } + CATEGORY_PRESETS).distinct().forEach { category ->
                val member = profile.categories.any { it.name == category && app.packageName in it.packages }
                add((if (member) "$category   ✓" else category) to { actions.onAddTo(category) })
            }
            add("Back" to { stage = MenuStage.Main })
        }
        MenuStage.Rename -> emptyList()
    }

    val title = when (stage) {
        MenuStage.Categories -> "Add ${profile.labelFor(app.packageName, app.label)} to…"
        else -> profile.labelFor(app.packageName, app.label)
    }
    BackHandler(enabled = stage != MenuStage.Main) { stage = MenuStage.Main }
    Box(Modifier.fillMaxSize().background(Color(0xD1000000))) {
        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = px(70), end = Ink.safeHorizontal)
                .size(px(600), px(940))
                .background(Ink.colors.panel, RoundedCornerShape(Ink.radius16))
                .padding(px(32)),
            verticalArrangement = Arrangement.spacedBy(Ink.gap16),
        ) {
            Text(title, style = InkType.section, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (stage == MenuStage.Rename) {
                NameField(name, { name = it }, Modifier.focusRequester(field), hint = "App name", onDone = { actions.onRename(name) })
                InkButton("Save name", { actions.onRename(name) }, primary = true)
                InkButton("Use original name", { actions.onRename("") })
                InkButton("Back", { stage = MenuStage.Main })
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Ink.gap8)) {
                    itemsIndexed(entries) { index, (text, action) ->
                        InkSurface(
                            onClick = action,
                            modifier = Modifier.fillMaxWidth().height(px(68)).then(if (index == 0) Modifier.focusRequester(first) else Modifier),
                        ) { _ ->
                            Row(Modifier.fillMaxSize().padding(px(16)), verticalAlignment = Alignment.CenterVertically) {
                                Text(text, style = InkType.label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
