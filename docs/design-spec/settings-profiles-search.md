# Settings, wallpaper, focus style, profiles, search: design spec

Source: Figma file `odpqy4Zqf0oNJBfsQCrT9l` ("TV launcher", palette "Ink & Iris"), pages "05 - Settings & wallpaper" and "06 - Profiles & search".
All sizes are design px on a 1920x1080 canvas. The TV runs at 2x density, so dp = px / 2.
Captions are the verbatim text under each screen (y about 1180 of the wrapper). Strings use the typographic apostrophe (’) and the multiplication sign (×) exactly as in Figma.

Method note: the Figma `figma-design-to-code` guidance resource could not be read (ReadMcpResourceTool was unavailable in this session), so `get_design_context` was called without `skillNames`. Only numbers and strings were extracted, so output is unaffected. No screenshots were taken; everything below comes from the generated code and metadata. Items marked (inferred) were not directly visible.

## 1. Tokens seen on these frames

| Token | Day (default) | Dim (W05/W06) |
| --- | --- | --- |
| bg | #14111C (Ink) | #0B0910 |
| panel | #282234 | #1B1725 |
| raised | #332C42 | (not shown) |
| text | #F4F0FA | #C6BBD7 |
| muted | #BDB0D0 | #B2A7C4 |
| focus (iris) | #C2ACF4 (gradient stop #C2ABF5) | #9E8DC5 |
| onfocus | #14111C | not shown |
| black | #000000 | #000000 |
| focuswidth | 6 px | 6 px |
| radius8 / 16 / 24 | 8 / 16 / 24 | same |
| gap12 / 16 / 24 / 32 | 12 / 16 / 24 / 32 | same |

Font: Inter. Title 48/600, section 30/600, label 24/500, body 24/400, line height 1.25 everywhere, letter spacing 0.

## 2. Shared Settings layout (S01 to S05, W06, F01 to F03)

Screen bg = `bg`. Layout is an absolute 1920x1080 stage:

| Element | x | y | w | h | Notes |
| --- | --- | --- | --- | --- | --- |
| Library sidebar | 96 | 54 | 296 | 972 | bg black, radius 16, padding 24, column gap 16 |
| Title | 448 | 64 | 1310 | 68 | 48/600, `text` |
| Subtitle | 448 | 140 | 1310 | 60 | 24/400, `muted` |
| Settings list | 448 | 236 | 1000 | 728 | column, gap 12 |
| Context help panel | 1496 | 236 | 328 | 620 | bg `panel`, radius 16, padding 24, column gap 20 |
| Key-hint line | 448 | 986 | 1376 | 36 | 24/400, `muted` |

Sidebar content (identical on every Settings/Search/Add-profile frame, not focused on them):
- "LIBRARY" 30/600 in `focus` colour, 248x40.
- Profile nav item "Allan" (user icon 28px + label), 248x60, padding 16, gap 12, radius 8. Label 24/500, `muted`, label width 176.
- Primary navigation column (248x436, gap 12): Home (tv icon), Search, Favorites (star), Video (circle-play), Games (gamepad-2), each 248x60. Default text colour `muted`.
- Flexible space 158 tall.
- Button "Launcher settings", Secondary, 248x72, bg `panel`, label 24/500 `text` centred.
- In none of the Settings frames is a sidebar item shown focused or selected (nothing highlighted); focus is in the list.

Context help panel: heading 30/600 `text`, 280x90 (text box fixed 90 tall, but headings are one line); body 24/400 `muted`, 280x400, paragraphs separated by one empty line (blank paragraph). Heading is usually "For Allan" (the current profile name, so heading = "For {profile}").

Row heights:
- Section heading row: 48 tall, 1000 wide, no background, text 30/600 `text`, not focusable.
- Settings row: 76 tall, 1000 wide, bg `panel`, radius 8, padding 16, gap 16, children vertically centred.
  - Label: 24/500 `text`, 570 wide.
  - Value: 24/400, right aligned, 270 wide, `muted` (note: Action rows reference token `muted` with fallback black in generated code, treat as `muted`).
  - Trailing: chevron-right icon 32x32 for Cycle and Action rows, or Toggle (below).
  - Focused state: 6 px solid `focus` border added (box includes border: the row stays 76 tall, content padding stays 16; border is inside as the Figma auto layout is border-box). No scale, no glow on the Outline preset.
- Toggle: track 64x32 radius 16; on = `focus` fill with thumb 24x24 `onfocus` at left 36 top 4 (square thumb in Figma, no radius); off = `raised` fill, thumb `text` at left 4. Value text "On"/"Off" sits left of the track in the 270 value column.
- Row types: Toggle (text "On"/"Off" + switch), Cycle (value + chevron; OK cycles the value), Action (value + chevron; OK opens a page or runs an action).
- Row gap 12 means pitch 88 for rows after a 76 row, and 60 for 48 section headings (48+12).

Key-hint line text uses two spaces after the key name and five spaces between pairs (e.g. `OK  Select     Back  Return`).

## 3. Settings frames

### S01 Settings · profiles and clock (18:743)
- Title: "Launcher settings". Subtitle: "Allan · Profile settings".
- Rows, in order (y relative to list top 236):
  1. Section "Profiles" (48)
  2. Switch profile, Action, value "Allan · 4 profiles", FOCUSED (first focusable row has initial focus)
  3. Add profile, Action, no value
  4. Delete profile, Action, no value
  5. Section "Clock and date" (48)
  6. Clock, Toggle, "On" (label is the component default "Clock"; the label is not overridden in the frame)
  7. Time format, Cycle, "24-hour"
  8. Date, Toggle (value "On"; rendered as the toggle-on variant with label "Date")
- Context help: heading "For Allan". Body:
  - "Clock and date settings apply to Allan."
  - (blank)
  - "Profile changes do not provide security."
  - (blank)
  - "OK changes a value or opens a page."
- Key hints: `OK  Select     Back  Return`
- Caption: "One scrolling Settings list, shown at its first position. Section headings are not focusable. Profile and clock values apply to the current profile."

### S02 Settings · date formats (18:854)
Continuation (list scrolled so Clock/Time/Date rows lead and a fourth row appears).
- Title: "Clock and date". Subtitle: "Allan · Live examples".
- Rows: Clock (Toggle On), Time format (Cycle "24-hour"), Date (Toggle On), Date format (Cycle, FOCUSED, value "Mon, Nov 16").
- Date format cycle values (four, in cycle order as listed in the help panel): "Mon, Nov 16" -> "Monday, November 16" -> "16 November 2026" -> "2026-11-16". Time format values: "12-hour" and "24-hour" (12-hour only implied by help text "12-hour clock: 8:41 PM", "24-hour clock: 20:41").
- Context help: heading "For Allan". Body:
  - "Four date examples:"
  - (blank)
  - "Mon, Nov 16"
  - "Monday, November 16"
  - "16 November 2026"
  - "2026-11-16"
  - (blank)
  - "12-hour clock: 8:41 PM"
  - "24-hour clock: 20:41"
- Key hints: `OK  Select     Back  Return`
- Caption: "Continuation of Clock and date. OK cycles four formats, updating the example immediately. Clock/date off removes the corresponding Home text."

### S03 Settings · rows (18:949)
- Title: "Rows". Subtitle: "Allan · Choose what appears on Home".
- Rows, all Toggle, in order (no section headings in this view, six rows):
  1. Other apps (On)
  2. Favorites (On)
  3. Most used (OFF, FOCUSED)
  4. Recently launched (OFF)
  5. HDMI inputs (On)
  6. Quick settings (On)
- Context help: heading "For Allan". Body:
  - "Most used and recently launched use counts stored on this TV, separately for each profile."
  - (blank)
  - "Category row order follows the profile configuration."
- Key hints: `OK  Select     Back  Return`
- Caption: "Continuation of the same list. Each optional row is per profile. Empty rows stay hidden even when enabled."

### S04 Settings · appearance and launch (18:1052)
- Title: "Appearance and launch". Subtitle: "Profile appearance and global behavior".
- Rows in order:
  1. Section "Appearance"
  2. Wallpaper, Action, value "Ink", FOCUSED (opens W01)
  3. Screensaver, Action, value "Clock"
  4. Focus style, Action, value "Outline" (opens F01)
  5. Backup and restore, Action, no value
  6. Hidden apps, Action, value "3 apps"
  7. Section "Launch · all profiles"
  8. On launch, Cycle, value "Ask who’s watching"
- On launch values: "Ask who’s watching" (S04) and "Open Home" (S05). Two values are shown; any third value is not in these frames (inferred: only two).
- Context help: heading "For Allan". Body:
  - "Wallpaper and hidden apps apply to Allan."
  - (blank)
  - "Screensaver, focus style, and launch behavior apply to every profile."
- Key hints: `OK  Select     Back  Return`
- Caption: "The scrolling list includes Wallpaper, Screensaver, Focus style, Backup and restore, Hidden apps, and the global launch choice."

### S05 Settings · done (18:1165)
- Title: "Launcher settings". Subtitle: "Changes save automatically".
- List (scrolled to end): Section "Launch · all profiles"; On launch (Cycle, "Open Home"); Backup and restore (Action); Hidden apps (Action, "3 apps").
- Done button: Primary, FOCUSED, absolute at x 448, y 800, 288x72, bg `focus`, text `onfocus` 24/500, border 6 px `text` (focused primary = iris fill + light border).
- Context help: heading "All saved" (not "For Allan"). Body:
  - "No extra Save step."
  - (blank)
  - "Done returns to Home."
  - (blank)
  - "The Settings key always opens Android system Settings."
- Key hints: `OK  Select     Back  Return`
- Sidebar "Launcher settings" button still default in this frame.
- Caption: "End of the scrolling Settings list. Done and Back both return to Home and restore the last focused app."

### Full Settings row order (derived across S01 to S05)
Order is contiguous in one scrolling list; the order S01 -> S03 -> S04 -> S05 is inferred, since S03's "Rows" has its own title and S04's content is a different heading ("Appearance and launch"). Titles change per frame, so treat each as a separate page/segment or a single list whose header text updates with scroll (ambiguity, see section 8).
1. Profiles (section): Switch profile (Action, "{name} · {n} profiles"), Add profile (Action), Delete profile (Action)
2. Clock and date (section): Clock (toggle), Time format (cycle: 12-hour / 24-hour), Date (toggle), Date format (cycle of four)
3. Rows (S03; no section heading shown): Other apps, Favorites, Most used, Recently launched, HDMI inputs, Quick settings (all toggles)
4. Appearance (section): Wallpaper (action, shows preset name), Screensaver (action, "Clock"), Focus style (action, "Outline"), Backup and restore (action), Hidden apps (action, "{n} apps")
5. Launch · all profiles (section): On launch (cycle: "Ask who’s watching" / "Open Home")
6. Done button (below the list)

Per-profile vs global (from help texts): per profile = clock/date settings, rows toggles, wallpaper, hidden apps, day/dim behaviour (W06), "Most used"/"Recently launched" counts. Global (every profile) = Screensaver, Focus style, On launch.

## 4. Wallpaper (W01 to W06)

### W01 Wallpaper · presets and selection (18:1260)
- Title "Wallpaper"; subtitle "Allan · Focus to preview, OK to select".
- No sidebar change; same sidebar as Settings.
- Grid: x 448, y 236, 1192 wide, 552 tall, column, row gap 32. Each row 1192x244, column gap 32. Thumbnails 360 wide, 244 tall (196 image + gap 12 + 36 label), 3 per row (3*360 + 2*32 = 1144, row container 1192).
- Thumbnail: image 360x196 radius 8; label below 24/500 `text`, 360 wide. Selected check: 32x32 black square radius 8 with check icon, at left 312 top 16 inside the image (top-right). Focus: 6 px `focus` border around the image box (label outside the outline). Selected and focused are independent states.
- Presets, in order: Row 0: Ink (flat bg #14111C, SELECTED check shown), Raised Ink (flat `panel` #282234), Iris dusk (horizontal gradient left #C2ABF5 to right #14111C, FOCUSED). Row 1: Night gradient (same gradient #C2ABF5 -> #14111C), Alpine lake (photo), Night sky (photo).
- Wallpaper sources row: x 448, y 842, 1192x72, gap 24, three Secondary buttons 288x72: "Bundled images", "TV storage", "Done".
- Key hints: `OK  Select wallpaper     Back  Return`
- Caption: "Selected has a check mark; focused has a 6 px outline. These states are independent. Presets include solids, gradients and bundled images."

### W02 Wallpaper · bundled images (18:1375)
- Same title/subtitle/geometry. One row: Alpine lake (photo, SELECTED check), Night sky (photo), "From TV storage" (FOCUSED; flat `raised` #332C42 box with 6 px focus border, no image).
- Sources row identical: "Bundled images", "TV storage", "Done".
- Key hints: `OK  Select wallpaper     Back  Return`
- Caption: "Photo previews are still thumbnails. No animated or video backgrounds. Bundled images and storage are separate sources."
- Reading: "From TV storage" is a tile that launches the system picker (W03), not an image.

### W03 Wallpaper · choose from storage (18:1471)
- Title "Choose an image"; subtitle "TV storage · Pictures · Sample files".
- Standard Settings list of Action rows: "Alpine-lake.jpg" (value "1920 × 1080", FOCUSED), "Family-photo.png" (value "3840 × 2160"), "Night-sky.jpg" (value "1920 × 1080"), "Cancel" (no value).
- Context help: heading "System picker". Body:
  - "Android owns file access and permission prompts."
  - (blank)
  - "Return the selected image to the wallpaper preview."
  - (blank)
  - "Cancel keeps the existing wallpaper."
- Key hints: `OK  Select     Back  Return`
- Caption: "Use Android’s system document picker in implementation. This handoff layout shows sample files, selection, cancellation, and the required image-only filter."

### W04 Wallpaper · apply preview (18:1568)
- Real Home screen drawn over the candidate wallpaper (a bright light image). Layers: wallpaper image 1920x1080 at 0,0; black scrim 1920x1080 at 70% opacity; opaque reading surface bg `bg` at x 416, y 54, 1408x972 (square corners); then UI.
- Sidebar as in Home: Home item selected style (bg `panel`, text colour `text`), other items `muted`; "Launcher settings" button.
- Home content: title "Your library" (448,64), subtitle "Allan’s apps, exactly where you left them." (448,140). Clock block right aligned at x 1464, y 64, 360 wide: time "20:41" 48/600 right aligned; below, wifi icon 28 + date "Mon, Nov 16" 24/400 `muted` (right aligned, 320 wide).
- Row "Favorites" at y 212 with count "5 apps" (row heading: label 30/600 1100 wide, count 24/400 right aligned 212 wide, gap 24, 44 tall). Cards: Netflix, Plex (with favorite star badge 24x24 black at left 210 top 10), YouTube, Crunchyroll, Twitch; card 244x209 (artwork box 244x137 `panel`, radius 8, icon 72; label 24/500 `muted` 244 wide, height 60), gap 24.
- Row "Games" at y 476 with "2 apps": Steam Link, Twitch.
- Actions panel: x 448, y 814, 1336x124, bg `panel`, padding 24, gap 24. Buttons: "Apply wallpaper" (Primary FOCUSED, 288x72, bg `focus`, border 6 px `text`, text `onfocus`) and "Cancel" (Secondary 288x72).
- Key hints: `OK  Open     Hold OK  App options`
- Caption: "Bright preview demonstrates the opaque reading surfaces. Apply changes the current profile only; Cancel preserves its previous wallpaper."
- Flow (derived): choose a preset/image in W01-W03 -> W04 preview over real Home, with scrim 70% and an opaque 1408x972 reading surface so text is readable on any image -> Apply wallpaper (saves for the current profile) or Cancel (reverts to previous wallpaper).

### W05 Home · automatic dim mode (18:1749)
- Same Home layout as W04 but in the dim palette and a dark wallpaper image: bg #0B0910, scrim black 70%, reading surface #0B0910 (x 416, y 54, 1408x972), panel #1B1725, text #C6BBD7, muted #B2A7C4, focus #9E8DC5.
- Rows: Favorites (5 apps; Netflix, Plex FOCUSED with 6 px #9E8DC5 border on the artwork box and star badge at left 204 top 4, YouTube, Crunchyroll, Twitch), Video (5 apps; Plex, Netflix, YouTube, Crunchyroll, Sony TV) at y 476, Games (2 apps; Steam Link, Twitch) at y 740. Clock "20:41", date "Mon, Nov 16".
- Focused app card label colour = `text`; others `muted`.
- No key-hint line on this frame.
- Caption: "Dim mode reduces wallpaper luminance and swaps the semantic color mode. Text remains readable. Light sensor changes fade for 150 ms with hysteresis to prevent flicker."

### W06 Wallpaper · day and dim settings (18:1982)
- Title "Day and dim mode"; subtitle "Allan · Wallpaper and brightness".
- Rows in order:
  1. Use light sensor, Toggle, "On", FOCUSED
  2. Current mode, Cycle, value "Automatic"
  3. Dim wallpaper at night, Toggle (shown as "On")
  4. Night preview, Action, no value
- Context help: heading "For Allan". Body:
  - "Dark: Ink #14111C"
  - "Dim: Ink #0B0910"
  - (blank)
  - "Dim text: #C6BBD7"
  - "Dim focus: #9E8DC5"
  - (blank)
  - "No full-screen blur or video."
- Key hints: `OK  Select     Back  Return`
- Caption: "Per-profile day/dim behavior. The light sensor switches between the Dark and Dim palettes; a fixed Night choice is available if the sensor is unavailable."
- Rules (from caption and W05):
  - Per profile (caption says "Per-profile day/dim behavior"; subtitle "Allan · ...").
  - "Use light sensor" On: ambient light sensor switches between Dark (#14111C palette) and Dim (#0B0910 palette); changes cross-fade 150 ms with hysteresis (no flicker).
  - "Current mode": cycle value shown "Automatic". Other values are not drawn; the caption implies a fixed "Night" choice when the sensor is unavailable. Likely cycle: Automatic / Day (or Dark) / Night (inferred; only "Automatic" and "Night" are named).
  - "Dim wallpaper at night": when in dim mode, wallpaper luminance is reduced (scrim stays 70% in W05; exact extra dim amount is not specified).
  - "Night preview": Action that previews dim mode (inferred, no value or page shown).
  - Palettes: Dark ink #14111C / panel #282234 / text #F4F0FA / muted #BDB0D0 / focus #C2ACF4; Dim ink #0B0910 / panel #1B1725 / text #C6BBD7 / muted #B2A7C4 / focus #9E8DC5.

## 5. Focus style (F01 to F03), global

Layout as Settings (title "Focus style", subtitle "Applies to every profile"), list of three Action rows, plus a noninteractive preview panel.

Preview panel (all three): x 1496, y 236, 328x500, bg `panel`, padding 32, column gap 24. Label "PREVIEW" 24/500 `muted`, 264 wide. Sample is an App card "Plex" (244x209; artwork box 244x137 with `focus` 6 px border, Plex icon 72; label 24/500 `text`). Then a 24/400 `muted` description, 264 wide. The panel is not a focus target. Key hints (all): see each frame.

### F01 Focus style · three presets (18:2077)
- Rows: "Outline" (Action, FOCUSED, value "Selected ✓"), "Scale" (value "1.04× + underline"), "Glow" (value "Outline + soft glow").
- Preview description (3 lines): "6 px iris outline" / "120 ms transition" / "No color-only focus".
- Key hints: `OK  Apply style     Back  Return`
- Caption: "Global setting. The right panel is a noninteractive preview, not another focus target. Outline is the Library default."

### F02 Focus style · Scale (18:2179)
- Rows: Outline (no value), Scale (FOCUSED, "Selected ✓"), Glow (no value).
- Preview: same card scaled 1.04x (253.76x217.36, border 6.24, radius 8.32, label 24.96 px). Description: "1.04× plus outline" / "120 ms transition".
- Key hints: `OK  Select     Back  Return`
- Caption: "Global preset. Right-hand preview is not interactive. Scale adds 1.04× enlargement; Glow adds a 16 px glow. Both retain a 6 px outline."

### F03 Focus style · Glow (18:2281)
- Rows: Outline, Scale, Glow (FOCUSED, "Selected ✓").
- Preview: card at 1.0x scale with drop shadow `0 0 8px rgba(194,171,245,0.32)` plus the 6 px outline. Description: "Outline plus glow" / "150 ms transition".
- Key hints: `OK  Select     Back  Return`
- Caption: same as F02.

### Focus preset summary
| Preset | Outline | Scale | Glow | Duration | Row value text |
| --- | --- | --- | --- | --- | --- |
| Outline (default) | 6 px `focus` | 1.0 | none | 120 ms | "Selected ✓" when selected; list text in F01: "6 px iris outline" |
| Scale | 6 px `focus` | 1.04 | none | 120 ms | "1.04× + underline" (F01 row text), preview "1.04× plus outline" |
| Glow | 6 px `focus` | 1.0 | 16 px glow per caption; preview is `drop-shadow(0 0 8px rgba(194,171,245,0.32))` | 150 ms | "Outline + soft glow" |
Conflict: F01 row value for Scale says "1.04× + underline", but F02 preview and its description say "1.04× plus outline" with no underline visible. Caption says glow 16 px but the preview shadow is 8 px blur at 32% (could be 8 px blur radius, 16 px spread-diameter). Rule from F01: "No color-only focus" (focus must be shape-visible, i.e. outline always present).
Selection indicator in these rows: the right-hand text "Selected ✓" replaces the value; unselected rows show no value and keep a chevron.

## 6. Profiles (P01 to P09)

Profile picker screen (no sidebar): bg `bg`, stage 1920x1080.
- Heading "Who’s watching?" centred, x 96, y 156, w 1728, h 80, 48/600 `text`.
- Subheading "Choose your own Home." centred, y 252, w 1728, h 44, 30/600 `muted`.
- Grid y 368. Tile 244x272, column gap 40 (x pitch 284), row gap 40 (y pitch 312, second row at 312). Grid is centred horizontally (x = (1920 - width) / 2).
- Key hints centred, y 984, w 1728, 24/400 `muted`: `OK  Select profile     Back  Open current profile`

Profile tile (244x272):
- bg `panel`, radius 8, padding 24, column gap 16, items centred.
- Avatar 112x112, bg `raised` #332C42, radius 24, containing the profile initial 48/600 in `focus` colour, centred (text box 112x68).
- Name 30/600 `text`, centred, 196 wide, 40 tall.
- Selected (= the current profile) shows a 24x24 check icon below the name (in P01/P02 the check appears on Allan only in P01; in P02 the lone Default tile has no check).
- Focused: 6 px `focus` border (outline preset). No other focus visual shown (scale/glow depend on the global Focus style).
- Avatars are initials only, no images (first letter of the name: D, A, K, G, M, W inferred for Movie night and Weekend).

| Frame | Profiles (order) | Grid x, w, h | Focus |
| --- | --- | --- | --- |
| P01 four (18:2383) | Default, Allan (selected check, focused), Kids, Guest | x 412, w 1096, h 272, one row | Allan |
| P02 one (18:2424) | Default | x 838, w 244, h 272 | Default (focused, no check) |
| P07 two (23:375) | Default, Allan | x 696, w 528, h 272 | Allan |
| P08 three (23:400) | Default, Allan, Kids | x 554, w 812, h 272 | Allan |
| P09 five (23:430) | row 0: Default, Allan, Kids; row 1: Guest, Movie night | x 554, w 812, h 584 | Allan |
| P03 six (18:2440) | row 0: Default, Allan, Kids; row 1: Guest, Movie night, Weekend | x 554, w 812, h 584 | Allan |

Row 1 tiles in P09/P03 start at x 0 (left aligned, not centred) when the final row is short.

Captions (verbatim):
- P01: "Shown at launch only when Ask who’s watching is enabled. Back opens the current profile; it never exits the launcher. Profiles are convenience, not access control."
- P02: "One profile stays centered. OK enters Home. Back enters the same profile. There is no empty placeholder tile."
- P03: "Six profiles use a 3 × 2 grid with stable vertical D-pad alignment. Extra sample profile names illustrate the six-profile layout."
- P07: "Two tiles centered as one group. D-pad focus is stable; Back opens the current profile."
- P08: "Three tiles centered as one row. Same component size and remote behavior as the other counts."
- P09: "Five profiles keep a three-column grid. Down maps to the closest available tile in the final row."

Grid rules: 1 to 3 profiles are one centred row; 4 profiles is one row of four (width 1096); 5 and 6 profiles use three columns (max 3 across in two rows, though the 4-profile case is a single row of 4, so the column count is 4 for exactly four, otherwise 3 for 5 and 6). D-pad: left/right moves between tiles in a row; down from a row with a taller grid goes to the same column, or the closest available tile when the final row is short (P09: from column 3 down is the closest, i.e. Movie night). Maximum 6 profiles (inferred: "six profiles" is the largest drawn; Add profile in Settings should be disabled/hidden at 6, not shown in the frames). No add tile in the picker.
Initial focus on open: the current profile (Allan), except when only one profile exists.
Back opens the current profile and never exits the launcher. Picker shows only when "Ask who’s watching" is on.

### P04 Add profile · text entry (18:2492)
- Sidebar as in Settings. Title "Name your profile" (448,64). Subtitle "Use the D-pad to type. Back closes the keyboard."
- Text entry field: x 448, y 236, 1000x92, bg `panel`, radius 8, padding 24, text 30/600 `text`; value "Movie night".
- Android TV keyboard (representative): x 448, y 370, 1000x460, bg black, padding 24, gap 12 (rows at y 24, 100, 176, 252 inside; pitch 76).
  - Row 0: Q W E R T Y U I O P (10 keys). Row 1: A S D F G H J K L (9). Row 2: Z X C V B N M (7). Editing row (gap 16): Space, Delete, 123 (each 280x64, Secondary button style, label 24/500).
  - Key: 72x64, bg `panel`, radius 8, label 30/600 `text`, key pitch 84 (gap 12). Focused key (Q initially): 6 px `focus` border.
  - Layout is representative only; the real keyboard is Android's.
- Entry actions: x 448, y 872, gap 24: "Cancel" (Secondary 288x72), "Create profile" (Primary 288x72, bg `focus`, text `onfocus`).
- Key hints: `Back  Close keyboard     OK  Enter character`
- Context help: heading "Your own setup". Body:
  - "Profiles can have their own clock, wallpaper, rows and hidden apps."
  - (blank)
  - "They do not restrict access to system apps."
- Caption: "Name is required. Cancel and Back preserve existing profiles. Save creates the profile and opens its Home."

### P05 Add profile · empty name (18:2634)
- Same as P04 with: subtitle "Enter a profile name to continue."; field placeholder "Enter a name" (30/600 in `muted`); "Create profile" is disabled (same Primary-label-styled button but bg `panel`, opacity 45%, label colour `onfocus`).
- Context help: heading "Name required". Body:
  - "A blank name cannot be saved."
  - (blank)
  - "Back closes the keyboard first; another Back cancels profile creation."
- Key hints: `Back  Close keyboard     OK  Enter character`
- Caption: "Keep the user’s entry and show a plain-text validation message. Create remains unavailable until at least one non-space character is entered."
- Validation: only message strings are the subtitle "Enter a profile name to continue." and the help "A blank name cannot be saved." No duplicate-name, length or character-limit message exists in the frames. Whitespace-only names are invalid.

### P06 Delete profile · confirmation (18:2776)
- Background is the Settings-style screen (title "Profiles", subtitle "Allan · 4 profiles", sidebar) under a full-screen black dim overlay at 82% opacity.
- Dialog: x 460, y 260, 1000x560, bg `panel`, radius 16, padding 40, column gap 24.
  - user icon 48x48
  - Title "Delete Allan’s profile?" 48/600 `text`, 920x80
  - Body 24/400 `muted`, 920x136: "This removes Allan’s rows, wallpaper choices, and usage counts from this TV. Installed apps are kept." / (blank) / "This action cannot be undone."
  - Actions (gap 24, 920x72): "Cancel" (Secondary, FOCUSED by default: bg `panel` + 6 px `focus` border) and "Delete profile" (Primary bg `focus`, text `onfocus`, not focused).
- Title format: "Delete {name}’s profile?".
- No key-hint line.
- Caption: "Deletion affects this profile’s settings only. Default or the last remaining profile cannot be deleted; explain this in Settings rather than offering a destructive action."
- Initial focus on Cancel (safe default).

## 7. Search (Q01 to Q03)

### Q01 Search · remote keyboard (18:2857)
- Sidebar as Settings. Title "Search apps". Subtitle "Use the D-pad to type. Back closes the keyboard."
- Text entry field (448,236, 1000x92) placeholder "Search app names" (muted 30/600).
- Keyboard: identical to P04 (Q focused).
- Entry actions: "Cancel" (Secondary) and "Search" (Primary), 288x72, gap 24, y 872.
- Key hints: `Back  Close keyboard     OK  Enter character`
- Context help: heading "Search installed apps". Body:
  - "Search filters app names for the current profile."
  - (blank)
  - "Hidden apps stay hidden."
  - (blank)
  - "OK on Search closes the keyboard and focuses the first result."
- Caption: "Search opens from the sidebar. Android owns the on-screen keyboard; this representative layout specifies focus, field, actions, and Back behavior."

### Q02 Search · matching results (18:3004)
- Title "Search apps". Subtitle "1 app found" (pluralisation: "{n} app(s) found", inferred).
- Search field: x 448, y 236, 1336x80, bg `panel`, padding 24, gap 16: search icon 32x32 and query text "plex" 30/600 `text`.
- Results row: x 448, y 382, 1336x205; App card (244x209) "Plex" FOCUSED (first result).
- No keyboard visible (it is closed). Key hints: `OK  Open     Up  Edit search     Back  Clear search`
- Caption: "The keyboard closes before focus enters results. Up returns to the query field; Back clears search and returns to the previous Home focus."

### Q03 Search · no results (18:3091)
- Title "Search apps". Subtitle "Query: weather".
- Empty state at x 448, y 270, 1100x400: padding 32, column gap 24; tv icon 64x64; heading "No apps found" 48/600 `text` (1036 wide); body "Try a shorter name or check the spelling. Your installed apps are unchanged." 24/400 `muted`.
- "Edit search" button at x 448, y 726, 288x72, FOCUSED (Secondary style with 6 px focus border).
- Key hints: `OK  Select     Back  Return`
- Caption: "No empty app row is shown. Edit search returns to the existing query in the keyboard."

Search behaviour (derived): opens from the Search sidebar item; matches app names against the current profile's visible apps (hidden apps excluded); OK on the "Search" button closes the keyboard and focuses the first result; Up from results returns to the query/edit; Back clears search and returns to the previous Home focus; empty result shows the empty panel, no empty row.

## 8. Implementation notes: ambiguities and conflicts

1. The Settings page is "one scrolling list", but title/subtitle change per frame ("Launcher settings", "Clock and date", "Rows", "Appearance and launch"). Decide: either one list with a fixed "Launcher settings" header (S01/S05) and the other titles being sub-pages, or a title that tracks the scroll position. S05's subtitle "Changes save automatically" appears only at the end. The S01 and S05 titles agree ("Launcher settings"); S02, S03, S04 are continuation frames and probably illustrate scroll positions.
2. S03 "Rows" has no section heading in the shown rows, and there is no "Rows" section row in S01/S02. The Rows toggles list also has no explicit order relative to S04's Appearance section; the sequence Profiles -> Clock and date -> Rows -> Appearance -> Launch is inferred from the captions ("Continuation of the same list").
3. S01 row 6 "Clock" and row 8 "Date" are toggles; the label of S01's first clock row is the component default and not overridden. Treat as "Clock".
4. "Time format" values: only "24-hour" appears as a value; "12-hour" appears in S02's help text. Cycle order assumed 12-hour <-> 24-hour.
5. Date format list order is taken from the help panel (4 examples). The default selected is "Mon, Nov 16".
6. S04 subtitle says "Profile appearance and global behavior", S05 help says Done returns to Home and "restore the last focused app". The caption states "Done and Back both return to Home", so there is no separate unsaved state.
7. "The Settings key always opens Android system Settings" (S05 help): the TV remote's Settings key is not remapped.
8. "On launch" has two values drawn: "Ask who’s watching", "Open Home". W05 shows the picker is shown at launch only if "Ask who’s watching" is chosen (P01 caption).
9. Wallpaper "Alpine lake" and "Night sky" are labelled as presets in W01 but appear as "bundled images" in W02; W01's caption says "Presets include solids, gradients and bundled images". Bundled images source = W02 screen; "TV storage" = system picker (W03 and the "From TV storage" tile). The "Bundled images" and "TV storage" buttons switch the visible grid (W01 default shows mixed presets; W02 shows only photos plus the storage tile).
10. W01 shows "Night gradient" and "Iris dusk" with the same gradient colours (#C2ABF5 -> #14111C); likely a design placeholder, treat as two entries with the same or near-same gradient (confirm with design).
11. W04 preview Home is different from other mocks only by the scrim: scrim 70% + opaque reading surface. Home row content here ("Favorites", "Games") differs from W05 ("Favorites", "Video", "Games"); the profile content is sample data.
12. W05 vs W06: help palette values are exact tokens for Dim; bg #0B0910 differs from `bg` (#14111C). W05's `black` stays #000000. Dim mode is described as swapping "semantic color mode", so implement as a second theme, not an opacity overlay.
13. Dim rules are explicitly per profile (W06 caption), yet the light sensor is a device-level input. "Night preview" behaviour and "Current mode" cycle values are unspecified beyond "Automatic" and a fixed "Night" choice.
14. F01 row text "1.04× + underline" for Scale conflicts with F02's "1.04× plus outline" (no underline). Glow: caption "16 px glow" vs preview shadow 8 px blur at 32% alpha. Duration differs (120 ms Outline/Scale, 150 ms Glow). W05's 150 ms light-sensor fade is a different transition.
15. Focus style is global ("Applies to every profile") and does not change per profile; the Settings row focus itself in all frames uses the Outline look (6 px border), even F02/F03 (so the preset affects app cards in preview, not necessarily settings rows).
16. Profile picker: grid for 4 profiles is one row of four (P01) while 5 and 6 use 3 columns (P09, P03). D-pad rule for the short final row (P09): "Down maps to the closest available tile." P02: one profile is focused, no check; in P01 the check marks the current profile (Allan) and also takes initial focus.
17. Add profile limits: no maximum-profile validation or Add disabled state is shown. P03 implies a six-profile maximum only by example.
18. P04's keyboard is a mock ("representative layout"); the real keyboard is Android's. Behaviour contract: Back closes the keyboard first, a second Back cancels creation (P05 help). "Save creates the profile and opens its Home" (P04 caption) while the button reads "Create profile".
19. Delete confirmation: Default or the last remaining profile cannot be deleted, "explain this in Settings" (no copy provided; needs a string for that disabled/blocked state).
20. Sidebar selected state: none of the Settings/Search/profile frames show the sidebar item focused or selected (Q02 and Q03 do not highlight Search); W04/W05 highlight Home with the `panel` bg.
21. Row subtitle uses the "·" separator (U+00B7); "Allan · 4 profiles" pattern is "{name} · {count} profiles" (singular form for one profile is not drawn).
22. Colour token note: Figma code shows `muted` with a black fallback on Action rows' value text; values are `muted` (#BDB0D0) in the screenshots context, so do not use black.
