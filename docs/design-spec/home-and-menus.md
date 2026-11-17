# Home and app actions (Figma page "04 - Home & app actions", node 11:46)

File `odpqy4Zqf0oNJBfsQCrT9l`. All coordinates are design px relative to the 1920x1080 screen frame (1 design px = 0.5 dp). Palette "Ink & Iris". Font: Inter everywhere.
Extraction method: Plugin-API dump of node geometry/text/fills (`use_figma`, read-only) plus `get_metadata` and one `get_design_context` (H01). No screenshots were taken, so anything visual that is not a node property (wallpaper image content, icon shapes) is not described. The `figma-design-to-code` guidance resource could not be read (resource tool unavailable); this does not affect the numbers.

## 0. Tokens seen

| Token | Value | Use |
|---|---|---|
| bg (Ink) | #14111c | screen background, opaque reading surface, dark text on focused primary button |
| panel | #282234 | cards' artwork container, nav item (selected), buttons, menu panel, menu items, HDMI/quick tiles |
| text | #f4f0fa | primary text |
| muted | #bdb0d0 | secondary text, unfocused card names, counters, hint line, captions |
| focus | #c2acf4 | focus ring (6 px stroke), "LIBRARY" label, primary button fill |
| black | #000000 | sidebar fill, favorite badge fill |
| toast | #332c42 | toast fill |
| radius | 8 (cards, buttons, items), 16 (sidebar, menu panel) | |
| gaps | 12, 16, 24 | |
| focus width | 6 px | |

Type styles (line-height 1.25, letter-spacing 0): title 48 Semi Bold; section 30 Semi Bold; label 24 Medium; body 24 Regular.

## 1. Shared Home layout

### Sidebar ("Library sidebar")
- Frame x96 y54, 296x972, fill #000000, radius 16, padding 24, vertical gap 16.
- Items, in order (children inside at x120, width 248):
  1. "LIBRARY" text, 30 Semi Bold, color focus #c2acf4, y78, 248x40.
  2. Profile item "Allan" (icon 28x28 + label), y134, 248x60, color muted. This is the profile switcher entry (name = current profile).
  3. Primary navigation group (gap 12, each item 248x60, padding 16, radius 8, icon 28 at x136, label x176 w176 24 Medium): Home y210 (selected: fill #282234, text #f4f0fa, tv icon), Search y282, Favorites y354, Video y426, Games y498 (muted text, no fill).
  4. Flexible space (158 tall) y662.
  5. Button "Launcher settings" y836, 248x72, fill #282234, text 24 Medium #f4f0fa centered (label box x136 w216).
- Nav labels: Home, Search, Favorites, Video, Games. Note Favorites/Video/Games are category shortcuts; the set of nav categories appears to mirror Home rows (not stated).
- Sidebar is identical in every frame of this page; only Home is highlighted.

### Header
| Element | x,y | size | style |
|---|---|---|---|
| Title "Your library" | 448,64 | 1310x68 | 48 Semi Bold #f4f0fa |
| Subtitle | 448,140 | 1310x60 | 24 Regular muted. Default text: `Allan’s apps, exactly where you left them.` (profile name interpolated; typographic apostrophe) |
| Clock block | 1464,64 | 360x118 | gap 4, right aligned |
| Clock "20:41" | 1464,64 | 360x60 | 48 Semi Bold, right aligned. 24h format in sample |
| Network status row | 1464,128 | 360x38 | gap 12 |
| Wi-Fi icon | 1464,128 | 28x28 | stroke #f4f0fa 2 px |
| Date "Mon, Nov 16" | 1504,128 | 320x36 | 24 Regular muted, right aligned |

- Offline (H10): icon becomes wifi-off (crossed Wi-Fi), the date text is replaced by `No internet`. Clock stays. So the second line is "network status text", date only shown when online.
- "Date off" variant is NOT drawn in this page. Inference: with date off, the second line would show only the network icon+status or be absent; needs a decision (see notes).
- Content column x = 448, width 1336 (right edge 1784). Clock right edge = 1824 (extends 40 beyond content column).

### Rows
- Row frame: x448, width 1336, height 261 (heading 44 + gap 12 + track 205). Row pitch = 264 (y212, 476, 740; 3 gap of 3 px between 261 frames, i.e. visual gap 3). Rows start at y212 and a 4th row would start y1004 (off screen; screen height 1080 minus hint line at y986: Home shows max 3 rows at once and scrolls vertically).
- Row heading: 1336x44, gap 24. Title 1100x44, 30 Semi Bold #f4f0fa. Right-aligned counter 212x36 at x1572, 24 Regular muted, text-align right.
- Counter text is `N apps` (e.g. `5 apps`, `2 apps`; the design also shows `1 apps` for a one-item row in H04/H05/H11, a pluralization defect, should be `1 app`).
- Card track: y = row y + 56, height 205, gap 24, horizontal.
- App card: 244 x 209 (artwork container 244x137 + gap 12 + label 60). Card pitch 268 (x448, 716, 984, 1252, 1520). Five cards fit exactly (5*244 + 4*24 = 1316; track 1336, 20 px slack on the right).
- Artwork container: 244x137 (16:9 banner slot), fill #282234, radius 8. Default icon artwork 72x72 centered (x+86, y+32). Native artwork keeps its own colors; square artwork is 137x137 centered (x+53), not cropped.
- Card label: 244x60 box, 24 Medium, center aligned (cards `items-center`), muted #bdb0d0 unfocused, #f4f0fa focused. Max 2 lines then ellipsis.
- Favorite badge: 24x24 at container offset (204, 4) i.e. top-right (x+204, y+4 of the container), fill #000000, radius 8, star icon. Shown on every favorited app card (Plex, Netflix in samples), whether focused or not.
- Focus: artwork container gets a 6 px stroke #c2acf4 (inside, layout box unchanged), label turns #f4f0fa. No scale or glow drawn.
- Rows in H01-H03 order: Favorites, Video, Games.

### Key hint line
- Text 1376x36 at x448,y986, 24 Regular muted, single line, two groups separated by 5 spaces: e.g. `OK  Open     Hold OK  App options`. Key name then two spaces then action.
- It is drawn only in H07, H08, H11, H13 (see each). It is absent from H01-H06, H09, H10 and from the menu frames A01-A06 (H09 has a different text, see below). Which frames show it is unresolved (notes).

### Backgrounds
- H01 "solid Ink": screen fill #14111c, no wallpaper layers.
- Photo wallpaper (H02 dark, H03 bright), layer order bottom to top:
  1. `Wallpaper / dark|light` image rectangle 0,0 1920x1080.
  2. `Wallpaper scrim / 70 percent` rectangle 0,0 1920x1080, fill #000000 at 70% opacity.
  3. `Opaque reading surface` rectangle x416 y54 1408x972, fill #14111c, 100% opaque, no radius drawn. It spans from the sidebar's inner side (sidebar x96-392, surface starts at 416, gap 24) to x1824, y54-1026 (same vertical extent as sidebar). So the wallpaper shows only in the outer margin (54 px top/bottom, 96 px left, 96 px right) and in the 24 px gap next to the sidebar.
  4. Sidebar (#000000, radius 16), then header and rows on top.
- The clock block (right edge 1824) sits inside the surface.

### End of Home
Hidden-apps controls appear after the last visible row (H11, section 12). Not drawn when nothing is hidden (inferred from caption).

## 2. Frame sections

Each section: wrapper caption at wrapper y1180 (screen y1100), 1920x120, 24 Regular muted. Caption text verbatim in a quote.

### H01 Home, solid Ink (13:95)
Default Home: layout from section 1, background #14111c, no hint line.
- Row Favorites (5 apps): Netflix, Plex (focused, favorited), YouTube, Crunchyroll, Twitch.
- Row Video (5 apps): Plex (favorited, no badge drawn), Netflix, YouTube, Crunchyroll, Sony TV.
- Row Games (2 apps): Steam Link, Twitch.
- Focus: Favorites row, card 2 (Plex) at x716 y268. Subtitle default.
- Badge note: in H01 Plex shows the badge only in the Favorites row (focused card); in later frames the badge shows on Plex/Netflix in Favorites row but not in Video row, so the badge is drawn only in the Favorites row, or not at all in other rows (inconsistent, see notes).
- Caption: "Default Home. One focused card. Native app artwork stays unchanged; banner slots are 16:9, and square artwork fits without cropping."

### H02 Home, dark wallpaper (13:325)
Same content and geometry as H01 plus the three background layers (section 1). Focus Plex row 1. Caption: "Dark image with an opaque reading surface behind text. Header and rows keep their contrast on any wallpaper."

### H03 Home, bright wallpaper (13:554)
Same as H02 with the light wallpaper. Identical scrim (70% black) and opaque surface. Artwork keeps its own color. Caption: "Bright image uses the same 70% black scrim plus opaque Ink reading surface. Artwork keeps its own color."

### H04 Row start, 42 apps (13:783)
- Subtitle default. First row is "Other apps" (the long row) with counter `1 / 42`. Second row Music (`1 apps`, myTuner), third Games (`2 apps`: Steam Link, Twitch). No Favorites/Video rows here (only these three rows show).
- Visible window: Netflix (focused, x448), Plex (favorited), Crunchyroll, YouTube, Steam Link. Cards at the standard pitch, no partial next card drawn.
- Counter text format for long rows: `<focused index> / <total>` replaces `N apps`; the index reports the focused item, not the viewport ("1 / 42").
- Scroll: focus on first card; Left from first card enters the sidebar (navigation).
- Caption: "Start of a long row. Left from the first card enters navigation; right follows focus. The counter reports the focused item, not the loaded viewport."

### H05 Row end, 42 apps (13:966)
- Same rows. "Other apps" counter `42 / 42`. Visible window shows the final five items: Google Play Store, myTuner (favorited), Settings, Sony TV, Input (focused, rightmost slot x1520).
- The window scrolls so the focused last item occupies the 5th slot; no trailing empty card or wrap. Right does nothing.
- Caption: "End of the same long row. The visible window has scrolled to its final five items; Right stops here. No wrapping or empty trailing card."
- Scroll behaviour is not animated in the file; the pattern is: focus moves within the 5 visible slots, window shifts when focus would leave (inferred from H04 vs H05). Exact shift step (1 card vs 5) is not defined (notes).

### H06 Mixed artwork, long name, fallback (13:1155)
- Subtitle becomes `Focused: An exceptionally long application name` (24 Regular muted, same position 448,140). Rule from caption: the full name goes into the subtitle only when the name exceeds two lines on the card.
- Favorites row cards: Netflix (banner), Plex (square artwork 137x137 at x769, favorite badge at 872,278, i.e. 4 px inside the square's top-right corner x+103, y+10 from the square's origin; banner badge is at +204,+10 from the container origin), focused card "An exceptionally long application name" (banner, focused ring, label `An exceptionally\nlong application…` two lines, ellipsis on line 2, color #f4f0fa), Crunchyroll "no artwork fallback" (137x137 square tile at x1305), Twitch (banner).
- Square tile: 137x137 centered in the 244 slot (offset 53.5), fill #282234. Fallback tile for no artwork is the same square (card name Crunchyroll; the tile content was not extracted, assume monogram/generic icon).
- Video row mixes squares (Netflix, Crunchyroll) and banners; Games row Twitch square. All slots still 244 wide, name box 244x60.
- Card label: max 2 lines, then ellipsis.
- Caption: "Square icons and banners share a 244 px slot and 137 px artwork height. Names allow two lines; focus reveals the full name above the row if it exceeds two lines."
- Conflict: the caption says "above the row" but the frame puts the full name in the subtitle line (y140), which is above the first row. Treat subtitle as that "above the row" slot.

### H07 Only Apps, empty rows omitted (13:1384)
- One row titled `Apps` (counter `5 apps`) at y212: Netflix (focused), Plex (favorited), myTuner, Settings, Input. Row sits at the same y as the first row; no empty gaps below.
- Hint line shown at 448,986: `OK  Open     Hold OK  App options`.
- Caption: "Empty categories never render a title or gap. When uncategorized apps form the only row, the title becomes Apps."
- (Elsewhere uncategorized apps are titled "Other apps"; "Apps" only when it is the sole row.)

### H08 No apps in this profile (13:1528)
- Header: title `Your library`; subtitle replaced by the profile name `Allan` (only). No clock block drawn.
- Empty-state panel "Empty or error" at x448 y270, 1100x400, radius 8 (fill not set / transparent). Title `A clean start` (48 Semi Bold #f4f0fa) at x480 y390, 1036x70. Body `Add apps to a category or unhide an app to fill this profile.` (24 Regular muted) at x480 y484, 1036x72.
- Actions row at x448 y730 (1000x72, gap 24): `Hidden apps` (focused: fill #c2acf4, stroke 6 #f4f0fa, text #14111c, 288x72, x448) and `Switch profile` (fill #282234, text #f4f0fa, 288x72, x760). Button text 24 Medium centered.
- Hint: `OK  Select     Home  Stay on Home`.
- Initial focus: Hidden apps.
- Caption: "No empty rows render. Open hidden apps or switch profile. Short Back remains on Home."

### H09 Configuration error (13:1609)
- Header: title only, no subtitle, no clock.
- Panel at x448 y250 (20 px higher than H08), 1100x400. Title `Configuration could not be read` (48 Semi Bold) at x480 y370, 1036x70. Body `Your saved configuration is unchanged. Try again or restore a backup.` at x480 y464.
- Buttons y730: `Try again` (focused, primary style as H08) x448; `Restore backup` x760 (secondary).
- Hint line (at 448,986): `Home and Back stay in the launcher.` (not a key/action pair; a plain statement).
- Caption: "Read failure does not overwrite the saved configuration. Retry first; restore from a backup as a secondary route."

### H10 No internet (13:1688)
- Standard Home (as H01 layout, solid bg) with network row = wifi-off icon + `No internet` (replaces date). Focus Plex (favorited badge shown, row 1).
- No banner, no blocking dialog.
- Caption: "The crossed Wi-Fi icon and text both convey offline status. Installed apps remain launchable; no blocking dialog."

### H11 End of Home, hidden apps controls (13:1917)
- Rows: Music (`1 apps`, myTuner) y212, Other apps (`4 apps`: Google Play Store, Settings, Sony TV, Input) y476. No Favorites/Video/Games rows (so rows start at y212; rows are those with visible apps).
- Footer controls "End-of-list controls" at x448 y818 (immediately after the last row: row end 476+261=737, plus 81), 1336x72, gap 24: `Unhide all (3)` (focused: fill #282234 + 6 px stroke #c2acf4, text #f4f0fa, 288x72, x448) and `Launcher settings` (288x72, x760, panel fill).
- `Unhide all (N)`: N = number of hidden apps (3 here); present only while apps are hidden; count updates immediately. Pressing it unhides all.
- Hint line: `OK  Select     Back  Stay on Home`.
- Caption: "Footer follows the final visible row. Unhide all is present only while apps are hidden; its count updates immediately."
- Note: the footer follows the last row; with more rows it scrolls into view, so it is not pinned at y818.

### H12 Most used and recently launched (13:2072)
- Smart rows come first, in order: `Most used` (counter text `On this TV`), `Recently launched` (counter text `Allan`, the profile name), then `Favorites` (`4 apps`), more rows below (not drawn).
- Most used: Plex (focused), Netflix (badge), YouTube, myTuner, Steam Link. Recently launched: YouTube, Plex, myTuner, Netflix, Steam Link. Favorites: Netflix, Plex, Crunchyroll, Twitch.
- Counter slot of smart rows holds a scope label instead of a count: `On this TV` (usage counts never leave the TV) and the profile name for per-profile recents.
- Rows y212, 476, 740 as standard.
- Caption: "Optional rows are per profile. Usage counts stay on the TV. Empty smart rows are omitted, just like category rows."

### H13 HDMI and quick settings (13:2323)
- Subtitle replaced by `TV controls`. This Home variant (a dedicated TV controls area) has tighter vertical rhythm: Favorites row y222 (height 246: heading 44 + 12 + track 205 where card height 209 overlaps), track y278.
- Row `HDMI inputs`, counter `4 inputs`, heading y476, track y532, tile track height 160.
  - Tile: 310x160, pitch 334 (x448, 782, 1116, 1450), gap 24, fill #282234, radius 8. Focused tile (HDMI 1) has 6 px stroke #c2acf4 and is vertically offset: content x474 y558.
  - Tile content: name line 36 tall with 24x? icon (name text offset +44, text 226 wide) 24 Medium #f4f0fa: `HDMI 1`, `HDMI 2`, `HDMI 3`, `HDMI 4`; below it status text 270x60, 24 Regular muted, y+48 after name.
  - Status strings: HDMI 1 `Connected · Game console`; HDMI 2 `No device`; HDMI 3 `Connected · Soundbar`; HDMI 4 `No device`. (Middle dot U+00B7 with spaces.) Device names are sample content.
  - Focused tile text is inset by +6 (stroke width): name x474 vs unfocused x802-782=20 offset... i.e. padding 20 unfocused, 26 focused. Treat as padding 20 with the stroke drawn inside the 310x160 box (ring counted in layout).
- Row `Quick settings`, counter `6 controls`, heading y724, track y780, tile 204x148, pitch 224 (x448, 672, 896, 1120, 1344, 1568), fill #282234, radius 8, label 172x60 at +16,+64 (24 Medium #f4f0fa, label bottom-aligned/centered vertically around y844 in tile, icon above, not extracted): `Sleep timer`, `Picture`, `Sound`, `Bravia Sync`, `Notifications`, `Wi-Fi`. Last tile ends at x1772.
- Hint line: `OK  Switch input     Back  Stay on Home`.
- Focus here: HDMI 1 (first tile of HDMI row). Quick tiles have no focus drawn.
- Caption: "HDMI shows connection status in words and icons. Device names are sample content. Quick settings open the matching TV/system surface."
- Not in the design: HDMI card behaviour when connected tile is activated beyond hint "Switch input". "Bravia Sync" is Sony-specific (sample).
- Note: the Favorites row in this frame is above the HDMI row but the frame subtitle is `TV controls`; unclear whether Favorites/HDMI/Quick settings are rows of the Home page (probably yes: sections of one scroll).

### A01 App menu, all actions (13:2556)
- Background: standard Home (H01 layout, solid) with a `Dim overlay` over the whole screen: 0,0 1920x1080 #000000 at 82% opacity. Card behind has no ring drawn (Plex favorited badge visible at row 1).
- Menu panel (`Overlay menu`): x1224 y70, 600x940, fill #282234, radius 16, right edge 1824 (aligned to clock block), 54... top 70, bottom 1010. Padding 32.
- Title region: x1256 y102, 536x64: app icon (64x64) at x1256 then title text at x1344 y102, 448x52, 30 Semi Bold #f4f0fa = app name `Plex` (icon 64 + gap 24).
- Actions list: x1256 y182, 536x720; items 520x68 (24 Medium text, 488 wide, padding 16) pitch 76 (gap 8), fill #282234 (same as panel; delineation is the focus ring only), radius 8.
- Items in order: `Open`, `Add to Favorites`, `Add to category…`, `Remove from categories`, `Rename`, `Reorder in row`, `Hide from home`, `App info`, `Cancel`.
- Focus: `Open` (ring 6 px #c2acf4; text offset +6).
- Caption: "Long-press OK opens this side panel. Initial focus is Open. Back closes it and returns focus to the originating card."
- Notes: the item `Add to Favorites` appears although Plex shows a favorite badge (see conflicts).

### A02 App menu, uncategorized app (13:2806)
- Same panel and overlay as A01, titled `Plex`. Items: `Open`, `Remove from Favorites`, `Add to category…` (focused), `Rename`, `Reorder in row`, `Hide from home`, `App info`, `Cancel` (8 items; y182 + 76*n). Initial focus here is on `Add to category…` (y334) (a state example, not the default).
- Conditional items:
  - `Remove from categories` shown only if the app belongs to at least one category.
  - `Add to Favorites` vs `Remove from Favorites` depending on favorite state.
- Caption: "Remove from categories is omitted when the app belongs to none. Favorited apps show Remove from Favorites instead of Add."
- Conflict: A01 shows both `Add to Favorites` and `Remove from categories` for an app that has the favorite badge (see notes).

### A03 Category picker (13:3054)
- Same panel. Title text `Add Plex to…` (30 Semi Bold, at x1344, which for this view still leaves the 88 px indent, no app icon needed but title keeps x).
- Items: `Favorites   ✓` (3 spaces then check mark), `Video   ✓` (focused), `Music`, `Games`, `Kids`, `Tools`, `Evening`, `Back`. Seven categories then Back. Check marks = current memberships. Same 520x68 item geometry and pitch 76.
- Source of items: "profile categories plus presets, deduplicated" (presets such as Kids, Tools, Evening appear).
- Choosing an item toggles membership (inferred) and shows the A04 toast.
- Caption: "Shows profile categories plus presets, deduplicated. Check marks mark current memberships. Back returns to the app menu."

### A04 Category saved, confirmation (13:3304)
- Menu closed. Home with Plex focused (Favorites row 2nd card, x716 y268), rows unchanged (Favorites 5, Video 5, Games 2).
- Toast `Toast / Added to Video`: x832 y914, 760x88, fill #332c42, radius 8, centered horizontally on the screen (832+380=1212, i.e. centered on x1212, not 960: aligned to the content area between x448 and 1976? see notes). Icon: check, 32x32 at x856 y942 (padding 24, vertically centered); text at x904 y940, 664x36, 24 Medium #f4f0fa: `Added to Video`. Component variant Kind=Success.
- Toast bottom edge y1002 (78 px above screen bottom); it overlaps the hint-line zone (y986-1022); no focus.
- Caption: "A confirmation toast appears without stealing focus. Menu closes, and focus returns to the same app. Fade after 3 seconds."
- Text format: `Added to <category>` (inferred removal `Removed from <category>`, not drawn).

### A05 App hidden, confirmation (13:3535)
- Toast `Plex hidden from Home` (same toast geometry, Kind success, check icon).
- After hide: Plex is removed from all rows (Favorites 4 apps: Netflix, YouTube, Crunchyroll, Twitch; Video 4 apps: Netflix, YouTube, Crunchyroll, Sony TV; Games unchanged). Focus lands on the next app in the row (YouTube at x716 y268).
- Caption: "After Hide, focus moves to the next app, then previous if last. Empty categories collapse. Hidden apps remain launchable from Android system screens."

### A06 Save error, focus retained (13:3745)
- Toast `Could not save changes. Try again.`; Kind=Error variant; icon `x` 32x32 at x856 y942 (text colour #f4f0fa; the toast has no red fill, only icon + text distinguish it).
- Home unchanged (Plex still focused, x716 y268), menu already closed, no new focus target.
- Caption: "A failed operation keeps the app and current focus in place. Error feedback uses an icon and text, with no new focus target."

## 3. Implementation notes: ambiguities and conflicts

1. Hint line visibility is inconsistent: shown in H07, H08, H09 (as a statement), H11, H13 only. H01-H06, H10, and all menu frames omit it. Decide whether it is global (most likely, optionally hidden on photo/mixed frames) or contextual. Strings used: `OK  Open     Hold OK  App options`, `OK  Select     Home  Stay on Home`, `Home and Back stay in the launcher.`, `OK  Select     Back  Stay on Home`, `OK  Switch input     Back  Stay on Home`.
2. Row counter semantic varies: `N apps` (category), `i / N` (long row, 42), scope labels `On this TV` / profile name (smart rows), `N inputs`, `N controls`. `1 apps` appears three times (H04, H05, H11, H12 none) and is a plural bug; treat as `1 app`.
3. Long-row threshold for switching `N apps` to `i / N` is not defined (42 apps in H04/H05; the caption only says the counter reports the focused item). Whether rows with 6-N apps already use `i / N` is unknown; H01 `5 apps` row fits all 5.
4. Scrolling in long rows: no overscroll or partial next card drawn; the card pitch and 5 slots are fixed. Shift step is unspecified (assume follow-focus by one card).
5. Home vertical scrolling: three 264 px rows fill y212-1004 but the hint line y986 overlaps the third row's bottom (row 3 ends y1001). Hint line only appears on frames with fewer rows, except H13, which compresses rows. Confirm that the hint overlay does not cover row 3.
6. Header variations: H08 replaces subtitle by profile name and drops the clock; H09 drops subtitle and clock; H13 subtitle `TV controls`; H06 subtitle `Focused: <name>`. Clock block missing on H08/H09 may be an omission of those mocks rather than a rule.
7. Date-off layout is not drawn. Offline replaces the date text with `No internet` plus wifi-off.
8. Favorite badge: drawn on Plex in H01 (focused, Favorites row), but Plex in the Video row has no badge in H01, while in H12 Netflix (second card, Most used row) has a badge but Netflix in the Favorites row below does not. Rule is unclear; safest is badge on any card of a favorited app except where in the Favorites row itself it is redundant (conflicts with H01 Plex). Treat as design inconsistency.
9. A01 vs A02: A01 offers `Add to Favorites` and `Remove from categories` for Plex while the card shows the favorite badge and A02 offers `Remove from Favorites` for the same Plex. Follow the A02 caption (menu depends on state); A01 is the "all actions" superset sample.
10. A02 title "Plex" and focus on item 3 is only a state sample; initial focus in menu is always `Open` (A01 caption).
11. Toast position: x832-1592 (centre x1212), not the screen centre (960) and not the reading-surface centre (1120). Use the literal x832 y914 760x88; it overlaps the hint-line zone (y986).
12. Toast fill #332c42 differs from panel #282234 and from error red: no error colour exists; error is conveyed by icon `x` and text.
13. H13 vertical metrics differ from standard rows (Favorites row y222 vs 212, rows 8 px apart): this page has a different row rhythm (HDMI row 220 tall, quick row 210 tall). The first row y222 also pushes the standard subtitle at y140 unchanged.
14. HDMI tile focus ring adds +6 inset to text (x474 vs 458 expected with padding 16 and 26 vs 20 for non-focused). Implement as a ring drawn inside the box; tile content must not shift.
15. Menu panel items have the same fill as the panel (#282234 on #282234); only the focus ring distinguishes the focused item. Pressed/disabled states not shown on this page.
16. Overlay dim is 82% black over everything including sidebar and clock; the panel sits at y70-1010 and its right edge matches the clock right edge (1824).
17. Menu item text has a +6 px x offset when focused (1278 vs 1272), consistent with a 6 px stroke inside the item rect; implement ring inside.
18. Back behaviour: menu Back returns focus to originating card; Back on Home "stays on Home" (short Back remains on Home, H08 caption; hint lines).
19. Wi-Fi/home variants H02/H03: the reading surface is rectangular with no radius; the sidebar (radius 16) sits partly over the gap. Reading surface x416 starts 24 px right of the sidebar edge (392), so the wallpaper is visible in that 24 px gap and in the 96 px side margins.
20. Dimension note: text boxes (e.g. title 1310x68) are wider than needed; only left alignment matters, except counter and clock, which are right aligned.
