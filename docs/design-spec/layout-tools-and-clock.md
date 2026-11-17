# Layout tools and clock (Figma page "07 - Layout tools & clock", node 11:49)

File key `odpqy4Zqf0oNJBfsQCrT9l`. Palette "Ink & Iris". Screens are 1920x1080 design px (wrapper 1920x1320; screen at wrapper y=80; caption text at wrapper y=1180, i.e. below the screen). 2x density: 1 design px = 0.5 dp. All coordinates below are relative to the 1920x1080 screen unless stated. Frame node ids in parentheses.

Method note: read with get_design_context / get_metadata / screenshots (B01, C03, T05). The `figma-design-to-code` guidance resource could not be read (ReadMcpResourceTool was not available), so get_design_context was called without it. Nothing was modified in Figma.

## Shared tokens (from the generated code)

| Token | Value |
| --- | --- |
| bg | #14111c |
| panel | #282234 |
| raised (toast) | #332c42 |
| black | #000000 |
| text | #f4f0fa |
| muted | #bdb0d0 |
| focus (accent) | #c2acf4 |
| onfocus (text on accent) | #14111c |
| focuswidth | 6 px (focus ring, border drawn inside the box) |
| radius8 / radius16 / radius24 | 8 / 16 / 24 |
| gap12 / 16 / 24 / 32 | 12 / 16 / 24 / 32 |
| dim overlay | black, opacity 0.82, full screen 1920x1080 |

Fonts: Inter. Styles: Library/title 48 SemiBold (600) lh 1.25; Library/section 30 SemiBold; Library/label 24 Medium (500); Library/body 24 Regular (400); Library/saver 180 Light (300). Letter spacing 0 everywhere.

## Shared Library layout (B01-B09, T01-T04, C01 all use it; T05/T06 use the sidebar too)

- Library sidebar: x96 y54, 296x972, black, radius16, padding 24, column gap 16. Contents: "LIBRARY" (30 SemiBold, color focus #c2acf4, 248x40); profile nav item "Allan" (user icon); primary nav (gap 12, 248x436): Home (tv icon), Search, Favorites (star), Video (circle-play), Games (gamepad-2); flexible space 158; bottom Secondary button "Launcher settings" (248x72, panel bg). Nav item: 248x60, padding 16, gap 12, icon 28, label 24 Medium, color muted when not focused/selected. In every B/T/C01 frame all nav items are in default (muted) state, "Launcher settings" is default, so focus is NOT in the sidebar; "Launcher settings" button is the entry point of this page (page sits "under" Launcher settings).
- Page title: x448 y64, 1310x68, 48 SemiBold, text color.
- Subtitle: x448 y140, 1310x60, 24 Regular, muted.
- Settings list: x448 y236, 1000x728, column gap 12. Settings row: 1000x76, panel bg, radius8, padding 16, gap 16; label 24 Medium text color, width 570; value 24 Regular right aligned width 270; chevron-right icon 32. Focused row: 6 px focus-colored border, same panel bg (no fill change). Value color on action rows is declared `muted` with fallback black in code; treat as muted #bdb0d0 (see notes).
- Context help panel: x1496 y236, 328x620, panel bg, radius16, padding 24, column gap 20. Heading 30 SemiBold text color (box 280x90), body 24 Regular muted (box 280x400); blank paragraph between each paragraph (a blank line = one empty 24 px line).
- Key-hint line: x448 y986, 1376x36, 24 Regular muted; segments separated by 5 spaces; key name and action separated by 2 spaces. List screens: `OK  Select     Back  Return`.
- Buttons: 288x72 (dialog and entry actions), radius8, label 24 Medium centered. Primary Focused: bg #c2acf4, 6 px border #f4f0fa, label #14111c. Primary Default: bg #c2acf4, no border, label #14111c. Secondary Default: panel bg, label #f4f0fa. Secondary Focused: panel bg, 6 px border #c2acf4, label #f4f0fa.
- Dialog (B02-B04, B06-B09): dim overlay over whole screen incl. sidebar (black 0.82), dialog box x460 y260, 1000x560, panel bg, radius16, padding 40, column gap 24. Children: icon 48x48 (x40 y40), title (48 SemiBold, 920x80, y112), body (24 Regular muted, 920x136, y216), actions row (gap 24, 920x72, y376). Dialog is centered horizontally (460+1000+460).

---

## B01 Backup and restore (19:3172)

Shown: Library > Backup and restore landing, two action rows. Sidebar as shared.
- Title: `Backup and restore`. Subtitle: `All profiles and global settings`.
- Rows (list at 448,236):
  | # | Label | Value | State |
  | --- | --- | --- | --- |
  | 1 | Export configuration | Save JSON file | Focused (y236) |
  | 2 | Import configuration | Choose JSON file | Default (y324) |
  Each followed by chevron-right.
- Context help (heading `For Allan`; the heading is the profile name):
  - `Includes profiles, rows, hidden apps, wallpaper references, launch behavior, focus style, and screensaver settings.`
  - (blank line)
  - `Image files remain on the TV.`
- Key hint: `OK  Select     Back  Return`
- Focus: row 1 (Export configuration).
- Caption verbatim: `Global operation: export/import contains every profile and global setting. Counts describe the sample configuration.`

## B02 Export, confirm (19:3257)

Shown: B01 screen dimmed under a dialog. Icon: download (48). 
- Title: `Export configuration?`
- Body: `Save all 4 profiles and global settings to a JSON file.` / blank / `Suggested name: tv-launcher-2026-11-16.json` (file name pattern `tv-launcher-YYYY-MM-DD.json`, date = today; sample date 2026-11-16; "4" = profile count).
- Buttons (left to right): `Cancel` (Secondary, Default), `Choose destination` (Primary, Focused).
- Focus: `Choose destination`.
- Caption verbatim: `Confirm destination through Android’s document picker. Export leaves the active configuration unchanged.`

## B03 Export, success (19:3337)

Icon: check (48).
- Title: `Backup saved`
- Body: `Your configuration was exported successfully.` / blank / `tv-launcher-2026-11-16.json` (the saved file name).
- Button: `Done` (Primary, Focused), single, at x40 of dialog.
- Caption verbatim: `Success follows a confirmed write. Done returns to Backup and restore.`

## B04 Export, error (19:3413)

Icon: x (48).
- Title: `Backup could not be saved`
- Body (single paragraph): `The selected location could not be written. Choose another location or try again. Your settings are unchanged.`
- Buttons: `Cancel` (Secondary, Default), `Try again` (Primary, Focused).
- Caption verbatim: `A write failure keeps all settings unchanged. Retry returns to the destination picker.`

## B05 Import, choose backup (19:3492) - the "file list"

No dialog. Full page, sidebar as shared.
- Title: `Choose a backup`. Subtitle: `TV storage · Downloads` (middle dot U+00B7).
- Settings list (same row component, 1000x76, gap 12):
  | Label | Value | State |
  | --- | --- | --- |
  | tv-launcher-2026-11-16.json | 4 profiles | Focused |
  | tv-launcher-2026-10-30.json | 3 profiles | Default |
  | Cancel | (none) | Default |
  Row format: file name as label, `N profiles` as right-aligned value, chevron. `Cancel` is the last row in the list (not a button), no value but still shows a chevron in the component. Newest first (sample).
- Context help heading `For Allan`; body: `Validate the file before changing settings.` / blank / `Check format, version and required fields.` / blank / `Back cancels without changes.`
- Key hint: `OK  Select     Back  Return`
- Focus: first file row.
- Caption verbatim: `Use Android’s document picker with a JSON filter. Sample filenames show selection; permissions remain Android-owned.`
- Note: the sample list is a design mock of what the picker returns; the real flow uses the Android document picker (SAF), so this list is representative.

## B06 Import, replace confirmation (19:3583)

Icon: upload (48).
- Title: `Replace this configuration?`
- Body: `The backup contains 4 profiles and global settings. Importing replaces the current configuration on this TV.` / blank / `Export a backup first if you want to keep the current setup.`
- Buttons: `Cancel` (Secondary, **Focused**: panel bg + accent border), `Replace configuration` (Primary, Default: accent fill, no border). Note the label is wider than other labels but button box is the same 288x72.
- Focus: `Cancel` (initial focus).
- Caption verbatim: `Cancel receives initial focus. Only explicit Replace writes the validated backup.`

## B07 Import, success (19:3663)

Icon: check.
- Title: `Configuration restored`
- Body: `All 4 profiles and global settings have been restored.` / blank / `Unavailable wallpaper references use solid Ink.` ("Ink" = bg #14111c).
- Button: `Open Home` (Primary, Focused).
- Caption verbatim: `Reload atomically. Return to current profile if it exists in the backup; otherwise open Default.`

## B08 Import, invalid file (19:3739)

Icon: x. Dialog frame name "This backup could not be read dialog".
- Title: `This backup could not be read`
- Body: `The file is invalid or uses an unsupported version. Choose a different backup. Your current configuration is unchanged.`
- Buttons: `Cancel` (Secondary Default, x0), `Choose another file` (Primary Focused, x312).
- Caption verbatim: `Validate before writing. Current configuration stays unchanged. Unsupported versions use this layout with a specific explanation.`
- Button state styling is taken from the layer names (`FOCUS — Button / Choose another file`) and the same pattern in B04; not separately inspected.

## B09 Import, restoring (19:3818)

Icon: upload. Dialog frame name "Restoring configuration… dialog".
- Title: `Restoring configuration…` (ellipsis character U+2026)
- Body: `Please wait while the validated configuration is written.` / blank / `Do not turn off the TV.`
- No buttons: the Dialog actions frame (y376, 920x72) exists but is empty. No progress bar or spinner is drawn.
- Caption verbatim: `Transient progress state. Retain focus on status; repeated OK is ignored. Then announce success or error.`

### Backup/restore flow summary

Export: B01 row 1 -> B02 confirm -> Android document picker (Create document) -> B03 success (Done -> B01) or B04 error (Try again -> picker again, Cancel -> B01).
Import: B01 row 2 -> B05 (picker-like list, Cancel row or Back = no change) -> validate -> B08 invalid (Choose another file -> B05, Cancel -> B01) or B06 confirm (Cancel focused by default) -> B09 restoring -> B07 success (Open Home) or an error (not drawn; reuse B08/B04 style, see notes).

---

## T01 Rename app, keyboard (19:3894)

Title `Rename Plex` (title = `Rename <app name>`). Subtitle: `Use the D-pad to type. Back closes the keyboard.`
- Text entry field: x448 y236, 1000x92, panel bg, radius8, padding 24; text 30 SemiBold (box 952x44). Current value `Plex`, color text #f4f0fa. No visible caret/underline drawn.
- Android TV keyboard (representative layout): container x448 y370, 1000x460, **black bg**, padding 24, column gap 12.
  - Row 0 (y24, 952x64): Q W E R T Y U I O P. Row 1 (y100): A S D F G H J K L. Row 2 (y176): Z X C V B N M. Keys 72x64, gap 12 (x step 84), panel bg, radius8, letter 30 SemiBold centered. Focused key: 6 px accent border.
  - Editing row (y252, 952x64, gap 16): `Space`, `Delete`, `123` as Secondary buttons 280x64 (label 24 Medium).
  - Container is 460 tall but content ends at y316; remaining space empty (representative; the real Android TV IME draws its own keyboard).
- Entry actions: x448 y872, 1000x72, gap 24: `Cancel` (Secondary Default 288x72), `Save name` (Primary Default: accent fill, no border, 288x72, x312).
- Key hint (x448 y986): `Back  Close keyboard     OK  Enter character`
- Context help heading `Launcher label`; body: `The new name appears on Home and in search.` / blank / `Android’s installed app name stays unchanged.`
- Focus: key `Q` (first key, top-left).
- Caption verbatim: `Only the launcher label changes. Package identity and original artwork are retained.`

## T02 Rename app, empty value (19:4036)

Same layout as T01 with differences:
- Subtitle: `Enter an app name to continue.`
- Text entry field shows placeholder `Enter a name` in **muted** (#bdb0d0), 30 SemiBold. No red/error color and no inline error text drawn.
- `Save name` is disabled: opacity 0.45, panel-colored background, label color #14111c (dark label on dark panel at 45%). `Cancel` normal. (Disabled style spec: opacity 0.45.)
- Context help heading `Name required`; body: `Enter at least one non-space character.` / blank / `Long names wrap to two lines. The focused name is shown in full above the row.` (the long-names line refers to app rows elsewhere, e.g. Home app cards, 2 line labels.)
- Key hint: `Back  Close keyboard     OK  Enter character`
- Focus: key `Q`.
- Caption verbatim: `Reject whitespace-only names inline. Save is unavailable until valid; Cancel preserves the previous label.`
- The "empty-value error copy" is: subtitle `Enter an app name to continue.` + help heading `Name required` + body `Enter at least one non-space character.` There is no separate red error string.

## T03 Hidden apps, individual unhide (19:4178)

- Title `Hidden apps`. Subtitle `Allan · 3 hidden apps` (`<profile> · <N> hidden apps`).
- Settings list (x448 y236), rows 1000x76, gap 12:
  | Label | Value | State |
  | --- | --- | --- |
  | YouTube | Unhide | Focused |
  | myTuner | Unhide | Default |
  | Google Play Store | Unhide | Default |
  | Unhide all (3) | (none) | Default |
  Row format: app name label, right aligned value `Unhide`, chevron. `Unhide all (N)` is the last row, no value, count in parentheses. Row has no app icon.
- Context help heading `For Allan`; body: `Hidden apps are specific to Allan.` / blank / `They remain launchable in Android system screens.` / blank / `Unhiding restores categories or Other apps.`
- Key hint: `OK  Select     Back  Return`
- Focus: first row (YouTube).
- Caption verbatim: `OK unhides the selected app and removes it from this list. Move focus to the next item or previous at the end.`

## T04 Hidden apps, none (19:4275)

- Title `Hidden apps`. Subtitle `Allan` (profile name only).
- Empty component "Empty or error": x448 y260, 1100x400, padding 32, column gap 24, no fill (transparent, radius8). Contents: tv icon 64, title `Nothing is hidden` (48 SemiBold, text color, box 1036x70), body `All installed apps are available in this profile’s Home rows.` (24 Regular muted, 1036x72).
- Button: `Done` (Primary Focused) at x448 y736, 288x72.
- No context help panel in this frame. Key hint: `OK  Select     Back  Return`.
- Focus: `Done`.
- Caption verbatim: `After the final unhide, show this state. Unhide all is omitted from Home.`
- Wording quirk: "Unhide all is omitted from Home" is presumably meant to say the Unhide all row is omitted (it is not shown when the list is empty / only one item).

## T05 Reorder, move app (19:4353)

Home-style screen in "move mode". Sidebar as shared (Home item shows selected panel highlight in the screenshot).
- Title `Your library`; subtitle `Allan’s apps, exactly where you left them.` (typographic apostrophe U+2019).
- Clock and connection block: x1464 y64, 360x118, column gap 4. Clock text `20:41` 48 SemiBold, right aligned, 360x60. Below it (y64, 360x38, gap 12): wifi icon 28 at x0 and date `Mon, Nov 16` (24 Regular muted, right aligned, box x40 w320). The wifi icon sits at the left edge of the block, i.e. visually detached from the right-aligned date (see notes).
- Row (frame x448 y212, 1336x261): heading `Favorites · moving Plex` (30 SemiBold) with right value `2 / 5` (24 Regular muted, right aligned, 212 wide; heading box 1100 + gap 24). Counter format `<position> / <total>`, 1-based. Heading is normally `Favorites` with value `5 apps` (default Row heading state).
- Track (y56 inside row, 1336x205): five App cards 244x209 at x = 0, 268, 536, 804, 1072 (pitch 268, gap 24). Cards: Netflix, **Plex (focused, 2nd)**, YouTube, Crunchyroll, Twitch. Card: artwork container 244x137 panel bg radius8, brand icon 72, label below 24 Medium (box 244x60). Focused card = 6 px accent border on the artwork container. Plex shows a star "favorite" badge (24x24, black bg, top-right at x204 y4).
- Move instructions banner: x448 y570, 1336x120, panel bg (square corners, no radius), padding 24, gap 20. Icon move-horizontal 40 at x24 y24; text (x84, 1180x60) 30 SemiBold text color: `Left / Right  Move     OK  Save     Back  Cancel`.
- Key hint (x448 y986, standard Home hint): `OK  Open     Hold OK  App options` (unchanged from Home; not switched to move-mode hints).
- No context help panel.
- Key behavior (banner + caption): Left/Right moves the app one position within the row; OK saves; Back restores the original order (cancel); Home cancels and returns Home.
- Caption verbatim: `Proposed entry: App menu → Reorder in row. Left/Right moves; OK saves; Back restores the original order. Home cancels and returns Home.`
- Position counter: the moved item is Plex at `2 / 5`; Plex moves among Netflix (1), YouTube, Crunchyroll, Twitch.

## T06 Reorder, saved (19:4499)

- Same Home screen, default (not move mode): title `Your library`, subtitle, clock block as T05, row heading `Favorites` (no counter value in the frame; the default heading value would be `5 apps`, but T06 draws only the heading instance named "Row heading / Favorites" - not separately inspected).
- Order after save: Netflix, YouTube, Crunchyroll, **Plex (focused, 4th)**, Twitch. Card x: 0, 268, 536, 804, 1072. (T05 had Plex 2nd, so the demo moved it two places right.)
- Move instructions banner gone.
- Toast: x832 y914, 760x88, raised bg #332c42, radius8, padding 24, gap 16; check icon 32 + text `Favorites order saved` (24 Medium, text color, box 664x36). Sits above the key hint (hint y986-1022, toast y914-1002 overlaps hint's horizontal span x832-1592 vertically; hint text is short and ends well left of x832 so no collision visible).
- Key hint: `OK  Open     Hold OK  App options`.
- Focus: Plex card at its new position.
- Caption verbatim: `The moved app stays focused at its new position. The toast does not take focus.`

---

## C01 Screensaver settings (19:4645)

- Title `Screensaver`. Subtitle `Applies to every profile`.
- Settings list (x448 y236, rows 1000x76, gap 12):
  | y | Label | Type | Value shown | State |
  | --- | --- | --- | --- | --- |
  | 236 | Clock mode | Cycle | `Hold Back` | Default |
  | 324 | Idle start | Cycle | `After 5 minutes` | Default |
  | 412 | Use current wallpaper | Toggle | `On` + toggle (on) | Default |
  | 500 | Dim clock at night | Toggle | `On` + toggle (on) | Default |
  | 588 | System screensaver | Action | `Android settings` | Default |
  | 676 | Preview | Action | (no value) | Focused |
  Row types: Cycle = muted value (270 wide, right aligned) + chevron-right 32. Toggle = `On`/`Off` text in muted + toggle (track 64x32 radius16 accent #c2acf4; thumb 24x24 #14111c at x36 y4 on the "on" side; off state not drawn). Action = value + chevron (Preview has chevron, no value text).
  `Clock mode` value `Hold Back` means the clock mode is entered by holding Back (see caption); it is a cycle row so other values exist but are not drawn (likely Off / Hold Back; unspecified). `Idle start` cycle values other than `After 5 minutes` not drawn.
- Context help heading `For Allan` (note: help heading shows profile name though the subtitle says settings apply to every profile); body: `Manual clock mode and idle screensaver share this clock design.` / blank / `The clock drifts slowly.` / blank / `Any navigation key wakes it.`
- Key hint: `OK  Select     Back  Return`
- Focus: `Preview` (last row).
- Caption verbatim: `Global settings. Short Back on Home stays a no-op; Hold Back is the proposed manual clock entry.`

## C02 Manual clock mode (19:4752)

- Full screen bg #14111c. No sidebar, no hints, no wifi/status element, no controls.
- Time `20:41`: text box 960x234 centered on x=960 (x480-1440), top y390; Inter **Light 300**, **180 px**, line height 1.25 (225, box 234), color text #f4f0fa, centered. 24h format `HH:mm` shown (20:41; no AM/PM).
- Date `Monday, November 16`: box 960x44 centered on x=960, top y626; Inter **SemiBold 600**, **30 px**, muted #bdb0d0, centered. Format: full weekday, comma, full month name, day number (no year): `EEEE, MMMM d`.
- Time + date block center: roughly y=390 to 670; block is vertically centered (center ~y530, screen center 540).
- Caption verbatim: `Clock drifts within ±24 px. No interactive controls while idle; a navigation key wakes Home without launching an app.`
- Entry: Hold Back on Home (per C01 caption "proposed manual clock entry"; Clock mode row value `Hold Back`).

## C03 Idle screensaver, wallpaper (19:4758)

- Layer stack (bottom to top): `Wallpaper / dark` full-bleed image 1920x1080, object-cover (sample is a dark starry-sky/mountain photo, asset in the Figma export); `Clock wallpaper scrim` black, opacity 0.80, full screen; `Clock reading surface` x440 y330, 1040x360, bg #14111c (fully opaque in the render), radius24; then clock text.
- Time `20:41` and date `Monday, November 16`: identical geometry, fonts, sizes, colors to C02 (box x480 y390 and y626, 960 wide). Time spans y390-624 and date y626-670 sit inside the surface (y330-690).
- Used when `Use current wallpaper` = On: the profile's wallpaper is the backdrop. Off presumably = solid Ink bg (C02 look); not drawn.
- No controls, no wifi/status.
- Caption verbatim: `Clock drifts within ±24 px. No interactive controls while idle; a navigation key wakes Home without launching an app.`

## C04 Clock, dim night variant (19:4767)

- Screen bg **#0b0910** (darker than Ink).
- Time `20:41`: color **#c6bbd7**. Date `Monday, November 16`: color **#b2a7c4**. Same fonts/sizes/positions as C02 (180 Light / 30 SemiBold; x480/y390 and y626).
- Used when `Dim clock at night` = On during night hours (night window not specified in the design).
- Caption verbatim: `Clock drifts within ±24 px. No interactive controls while idle; a navigation key wakes Home without launching an app.`

## C05 Clock, drift position (19:4773)

- Same as C02 (bg #14111c, normal colors) but offset: time box left edge x504, top y374 (centered at x=984); date box x504, y610. Offset vs C02 = **+24 px x, -16 px y**.
- Caption verbatim: `Second drift position: +24 px x, −16 px y. Move gradually over about 60 seconds. Keep the clock inside the 5% safe area.`
- Drift rule: clock stays within ±24 px of its centered rest position (C02 = (0,0), C05 = (+24,-16)); movement is gradual over about 60 seconds between positions; always inside the 5% safe area (96 px horizontally, 54 px vertically on 1920x1080; the clock block is nowhere near it, so the constraint matters mainly for the wallpaper reading surface (440-1480 x 330-690) and any future larger layouts). In dp: ±12 dp, and (+12, -8) dp for position 2.

---

## Implementation notes: ambiguities and conflicts

1. Design guidance resource not read: ReadMcpResourceTool was unavailable, so get_design_context ran without the figma-design-to-code guidance. Only numbers/strings were extracted, which is what was asked.
2. Sample data: "4 profiles", "3 profiles", `2026-11-16`, `Allan`, `20:41`, `Mon, Nov 16` are samples. Dialog and help copy include the count and the profile name, so these must be interpolated. Captions say "Counts describe the sample configuration."
3. Import error besides invalid file: only B08 exists. A read/permission failure or write failure during B09 is not drawn (no "restore failed" frame). Suggest reusing B08 layout with a specific explanation (caption says "Unsupported versions use this layout with a specific explanation").
4. B05 list shows `tv-launcher-*.json` file names with `N profiles` values, which implies the app reads each file's metadata, yet the caption says the Android document picker is used (permissions Android-owned). These are conflicting; implement the SAF picker (OpenDocument, `application/json`) and treat B05 as illustrative, or if building an in-app list, scan Downloads (subtitle `TV storage · Downloads`) and read the profile count. Decision needed.
5. B02 "Choose destination" opens the system document picker (CreateDocument) with the suggested file name; "Export leaves the active configuration unchanged".
6. B01/C01/T03 context help heading `For Allan` is the profile name even on global settings pages (B01, C01 are explicitly global). Probably a mock artifact; consider a neutral heading for global pages.
7. Settings row value color: code shows `var(--muted, black)` for Action-row values (fallback black) vs `var(--muted,#bdb0d0)` for Cycle/Toggle rows. In the screenshot (B01) values render muted lavender, so use #bdb0d0 everywhere.
8. Settings row focus style: Focused row = 6 px accent border and unchanged panel fill; Primary buttons Focused = accent fill + 6 px #f4f0fa border (different from rows). Secondary focused (B06 Cancel) = accent border on panel fill. Keep the three distinct.
9. Disabled `Save name` (T02): bg panel, label #14111c, opacity 0.45; the label color is the on-accent dark so the disabled button looks nearly invisible. Likely intended as a dimmed primary; consider accent fill at 45% instead. Flag to design.
10. T02 "empty value error": there is no error text/color; only placeholder `Enter a name`, subtitle `Enter an app name to continue.`, help heading `Name required`, and disabled Save. Caption says "Reject whitespace-only names inline".
11. T01 keyboard is a "representative layout" (4 rows, uppercase letters, `Space`/`Delete`/`123` row). On Android TV the system IME is used; Figma keyboard is illustrative (digits/symbol layout for `123` not drawn). Max name length not specified. Focus starts on Q.
12. T02 context help says "Long names wrap to two lines. The focused name is shown in full above the row." That behavior refers to app cards elsewhere (label box 60 tall = 2 lines at 24 px / lh 1.25 = 30 each), not this screen.
13. T03: `Unhide all (3)` row has no value but shares the chevron; per T04 caption it disappears at the end. Also "Move focus to the next item or previous at the end" (if the last item is unhidden, focus goes to the previous one). When the list becomes empty, switch to T04. T03 subtitle `Allan · 3 hidden apps` is a plural string; singular form (`1 hidden app`) not specified.
14. T04: only `Done` button is focusable, no help panel; focus `Done` (returns to previous Library page). Caption "Unhide all is omitted from Home" is garbled (see T04).
15. T05 caption says "Proposed entry: App menu -> Reorder in row", i.e. the app options menu (Hold OK) needs a new `Reorder in row` item; check home-and-menus.md for the menu spec. Home cancels and returns Home (also discarding?): caption says Back restores original order, Home cancels and returns Home (so both discard; only OK persists).
16. T05 key hint line stays the Home hint (`OK  Open     Hold OK  App options`) even though in move mode OK saves and Hold OK is not an options key; the banner's `Left / Right  Move     OK  Save     Back  Cancel` carries the move-mode keys. Suggest hiding or replacing the bottom hint while moving; design leaves it unchanged.
17. T05 row heading shows `Favorites · moving Plex` and `2 / 5` (position / total, replacing the usual `5 apps`). Whether the 'moving' item is visually lifted/scaled: no, it only has the focus ring. The banner box has square corners (no radius), unlike other panels (radius16/8).
18. T05/T06 clock block: wifi icon is at x0 of the 360 wide block (x1464) while the date is right-aligned, so the icon appears detached on the left. The 'Clock and connection' component on Home (see home-and-menus.md) should be cross-checked; the wifi icon probably belongs immediately left of the date text. Date format on Home = `EEE, MMM d` (`Mon, Nov 16`); screensaver date = `EEEE, MMMM d`.
19. Screensaver: C01 `Clock mode` value is `Hold Back`, not On/Off; the cycle values are unknown (assume Off / Hold Back; maybe more). `Idle start` value `After 5 minutes`; other options unknown (e.g. 1, 5, 10, 15, 30 minutes, Never). `System screensaver` opens Android settings. `Preview` starts the idle screensaver immediately (C03/C02 style) - not stated in design; infer. Toggle off state is not drawn.
20. Manual clock mode (C02) vs idle screensaver: both share the design (C01 help). C02 = solid Ink bg; C03 = wallpaper + 80% black scrim + opaque Ink reading surface (1040x360, radius24); C04 = night dim colors on #0b0910 (for solid bg; whether dim also applies on the wallpaper variant, and how the reading surface looks dimmed, is not drawn). Night window hours not defined.
21. Drift: ±24 px max offset horizontally and vertical shown as -16 (only the +24/-16 sample is drawn); implement as slow translation (~60 s) between positions within ±24 px (x) and an unspecified ±16 px (y; the only evidence is -16). Safe area 5% = 96 px x / 54 px y. Burn-in avoidance, not interaction.
22. Wake behavior: any navigation key wakes to Home without launching an app (key is consumed). Short Back on Home stays no-op; Hold Back on Home enters manual clock mode (C01 caption, marked as proposed). No wifi/status element on the screensaver itself.
23. Frame titles in Figma wrappers (e.g. `B01  Backup and restore`) are labels for the design file only (48 SemiBold above each screen), not UI.
24. Dialog focus defaults: confirmation dialogs focus the primary action except B06 (destructive replace), where Cancel is focused. B09 has no focusable control.
25. Dim overlay: black at 0.82 over the entire screen (including the sidebar) behind dialogs.
