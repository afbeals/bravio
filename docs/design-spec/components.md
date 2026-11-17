# Component library spec (Figma 08 - Library / Components, node 11:50)

Source: file odpqy4Zqf0oNJBfsQCrT9l, 1920x1080 design px; TV runs 2x so 1 design px = 0.5 dp. All numbers below are design px unless "dp" is stated. Read-only extraction from get_design_context of every component set plus K01-K05 specimens. Screenshots taken for App card (12:56) and Overlay menu (21:95) only.

## Tokens (as used by the components)

| Token | Hex | Used for |
|---|---|---|
| bg | #14111C | screen background |
| panel | #282234 | default card/row/tile fill, overlay menu fill |
| raised | #332C42 | Pressed fill, toast fill, toggle-off track, avatar fill |
| text | #F4F0FA | primary text, Pressed outline, toggle-off thumb |
| muted | #BDB0D0 | secondary text, unfocused card/nav labels |
| focus | #C2ACF4 | focus outline, Primary button fill, toggle-on track, avatar initial |
| onfocus | #14111C | text/thumb on a focus-colored fill (same hex as bg) |
| black | #000000 | favorite / selected badge fill |

Spacing tokens: gap8=8, gap12=12, gap16=16, gap24=24, gap32=32. Radius tokens: radius8=8, radius16=16, radius24=24. focuswidth=6.
Only one palette (dark) appears in the library frames. There is NO "dim" variant anywhere in these frames; nothing to record for dim.

## Text styles (all Inter, line height 1.25 = 125%, letter spacing 0)

| Style | Weight | Size |
|---|---|---|
| Library/title | SemiBold 600 | 48 |
| Library/section | SemiBold 600 | 30 |
| Library/label | Medium 500 | 24 |
| Library/body | Regular 400 | 24 |
| (Settings Section row heading uses section style at 30 in master; K02 shows 24 only because K02 is scaled 0.8) | | |

## Global state rules (every focusable component)

- Remote-only. States: Default, Focused, Pressed. No hover.
- Focused: outline 6 px solid focus (#C2ACF4), drawn as a border INSIDE the node box (node size does not grow; content is inset by the border). No scale, no glow, no shadow.
- Pressed: outline 4 px solid text (#F4F0FA), also inside the box; fill becomes raised (#332C42) for panel-filled components (exceptions below).
- Corner radius 8 on every component unless stated.
- No drop shadows, blur or glows exist anywhere in the library.

## 1. App card (12:56)

Props: shape Banner|Square, state, label (default "Plex"), favorite (bool), artwork (default Asset/brand-plex 72x72).
Whole component: 244 x 209, column, gap 12, items centered, radius 8. Bottom label is always 244 x 60 (two lines max: 24 x 1.25 = 30 each), Medium 24, left aligned (no text-align set).

| Variant | Artwork container (panel #282234, radius 8) | Border | Label color |
|---|---|---|---|
| Banner Default | 244 x 137 | none | muted |
| Banner Focused | 244 x 137 | 6 focus (inside) | text |
| Banner Pressed | 232 x 129 (centered, 6 px inset left/right, 4 top/bottom) | 4 text (inside) | text |
| Square Default | 137 x 137 (centered in 244 col) | none | muted |
| Square Focused | 137 x 137 | 6 focus | text |
| Square Pressed | 125 x 129 | 4 text | text |

- Pressed container fill stays panel (not raised). Pressed is a shrink: 244x137 -> 232x129 (about 0.95).
- Artwork: centered, 72 x 72 icon.
- Favorite badge (optional, absolute, in container): 24 x 24, black fill, radius 8, top-right. Position (left, top) in container: Banner Default (210,10), Focused (204,4), Pressed (206,6); Square Default (103,10), Focused (97,4), Pressed (99,6). I.e. 10 px from top and 10 px from right edge of the unfocused box; stays 10 px from the right/top of the outer edge, adjusting for the border (Focused -6, Pressed -4 relative to the box). Icon is Asset/star (assumed from component node 11:210 named "favorite"; glyph not visible in render, treat as star, 24 px).
- Label: Default muted, Focused/Pressed text. The label does not move when pressed (label at y=149 in all states).

## 2. Button (12:69)

288 x 72, padding 16, radius 8, label centered, Medium 24, single line, default label "Continue". Text box h 36.

| Variant | Fill | Border | Text |
|---|---|---|---|
| Primary Default | focus #C2ACF4 | none | onfocus #14111C |
| Primary Focused | focus #C2ACF4 | 6 text #F4F0FA (inside) | onfocus |
| Primary Pressed | raised #332C42 | 4 text | text #F4F0FA |
| Secondary Default | panel #282234 | none | text |
| Secondary Focused | panel | 6 focus #C2ACF4 | text |
| Secondary Pressed | raised | 4 text | text |

Note: Primary Focused uses a text-colored (near-white) outline on the lavender fill, NOT a lavender outline.

## 3. Settings row (12:138)

Master width 1000 (K02 specimen scales it 0.8 to 800; use master). Height 76 for actionable rows, 48 for Section. Padding 16, gap 16, radius 8, row layout, items centered.

Props: type, state, label (default "Clock"), value.

| Type | Contents (left to right) |
|---|---|
| Toggle on | label (570 x 40, Medium 24, text) ; value text "On" (270 w, Regular 24, muted, right-aligned) ; toggle track |
| Toggle off | same, value text "Off", track off |
| Cycle | label ; value text (270 w, Regular 24, muted, right) e.g. "24-hour" ; chevron-right icon 32 x 32 |
| Action | label ; value text (empty string in specimen, 270 w) ; chevron-right 32 x 32. Value color token falls back to black in master, but value is empty, so ignore. |
| Section | single label, 1000 x 40, SemiBold 30, text color, no fill, no padding, not focusable, only Default state, row h 48 |

Row fill/border by state (Toggle on/off, Cycle, Action): Default panel, no border; Focused panel + 6 focus border; Pressed raised + 4 text border. Row text colors do not change with state.

Toggle: track 64 x 32, radius 16 (pill). Thumb 24 x 24 at top 4. NOTE thumb has no radius in the generated code (square 24x24, no rounded class); the radius is probably on the node but not emitted. Assume round thumb; flag to designer.
- On: track focus #C2ACF4, thumb onfocus #14111C, thumb left 36 (right).
- Off: track raised #332C42, thumb text #F4F0FA, thumb left 4 (left).
- Toggle looks identical across Default/Focused/Pressed (track color does not change). Pressed row fill is raised, same as the off track, so an off track vanishes into a pressed row (flag).
- Specimen label strings (K02): "Toggle on · Default" etc. (labels are placeholders); value strings "On", "Off", "24-hour", "".
- Caption in K02: "Section headings are not focusable. Toggle state uses position and On/Off text. All actionable row types include focus and pressed variants."

## 4. Menu item (12:145)

520 x 68, padding 16, gap 12, radius 8, single label (488 x 36, Medium 24, text, default "Open").
- Default: panel fill, no border. Focused: panel + 6 focus border. Pressed: raised + 4 text border.
- Inside the Overlay menu the Default item has no visible separate fill beyond panel (same hex as menu, so it looks transparent); Focused item shows the lavender 6 px outline.

## 5. Profile tile (12:167)

244 x 272, column, gap 16, padding 24, items centered, radius 8. Props: initial "A", label "Allan", selected (bool).
- Avatar: 112 x 112, radius 24, fill raised #332C42, initial centered: SemiBold 48 in focus #C2ACF4 (text box 112 x 68, center aligned).
- Name: 196 x 40, SemiBold 30, text color, centered, single line.
- Selected: 24 x 24 Asset/check icon below the name (selected indicator). In the specimen all three are shown with selected=true and labels "Default", "Focused", "Pressed".
- Fill/border: Default panel; Focused panel + 6 focus; Pressed raised + 4 text. Selection is independent of focus (K03 caption: "Profile selection and focus are independent.").
- With selected=true, content height = 24 pad + 112 + 16 + 40 + 16 + 24 + 24 pad = 256 < 272 (fits). Without it, content is top-aligned (empty space at bottom).

## 6. Wallpaper thumbnail (12:186)

360 x 244, column, gap 12, radius 8. Image box 360 x 196 (radius 8, placeholder fill panel; real image object-cover, rounded 8), label below 360 x 36, Medium 24, text color, default "Ink" (K04: "Default", "Focused", "Pressed").
- Default: no border. Focused: 6 focus border INSIDE the image box (image fills the box incl. border area per generated code). Pressed: 4 text border. Label color constant text.
- Selected badge (selected=true): 32 x 32, black fill, radius 8, containing Asset/check. Position (left, top) in the 360 x 196 image box: Default (312,16), Focused (306,10), Pressed (308,12). I.e. 16 px from top and right edge of the image, shifted in by the border.
- K04 caption: "Wallpaper check means selected; outline means focused."
- Wallpaper assets: Wallpaper asset / light (11:244) and / dark (11:245), both 480 x 270 (raster; K04 uses two PNGs).

## 7. Quick setting (12:205)

204 x 148, column, gap 12, padding 16, radius 8. Icon 36 x 36 (default Asset/clock), label 172 x 60 (two lines allowed), Medium 24, text, default "Sleep timer".
- Default panel; Focused panel + 6 focus; Pressed raised + 4 text.

## 8. HDMI input (12:260)

310 x 160, column, gap 12, padding 20, radius 8. Props: connection Connected|Empty, label "HDMI 1", detail.
- Header row 270 x 36: Asset/monitor 32 x 32 + gap 12 + label 226 x 36, Medium 24, text.
- Detail text 270 x 60 (two lines), Regular 24, muted.
- Connected detail: "Connected · Game console". Empty detail: "No device" (specimen override; the Empty master still holds the Connected string as default, so set "No device" explicitly).
- Connected and Empty are styled IDENTICALLY (same icon, colors). Distinction is text only (K03 caption: "Input status uses text as well as a device icon.").
- Default panel; Focused panel + 6 focus; Pressed raised + 4 text.

## 9. Toast (12:278)

760 x 88, row, gap 16, padding 24, radius 8, fill raised #332C42, no border, never focusable (K04: "Toasts never take focus"). Icon 32 x 32 + text 664 x 36 Medium 24 text color, single line.
- Success: Asset/check, default text "Added to Video".
- Error: Asset/x, text "Could not save changes. Try again."
- Info: Asset/bell, text "Plex hidden from Home".
- Icon color is the same for all kinds (no kind color is applied beyond the glyph). No shadow. Placement on screen not defined in the library (K04 specimens at left 96).

## 10. Row heading (12:282)

1336 x 44, row, gap 24, radius 8, no fill. Title 1100 x 44, SemiBold 30, text color, default "Favorites". Count 212 x 36, Regular 24, muted, right-aligned, default "5 apps". Only Default state, not focusable.

## 11. Navigation (12:307)

248 x 60, row, gap 12, padding 16, radius 8. Icon 28 x 28 (default Asset/tv) + label 176 x 36, Medium 24, default "Home".

| State | Fill | Border | Label |
|---|---|---|---|
| Default | none | none | muted |
| Selected | panel | none | text |
| Focused | none (transparent) | 6 focus | text |
| Pressed | panel | 4 text | text |

Selected and Focused are independent (selected+focused composition is not drawn; reasonable result: panel fill + focus border). Icon color does not differ by state in the render (icons are the same asset; assume it follows text/muted color if tinted, designer to confirm).

## 12. Keyboard key (12:314)

72 x 64, centered single character, radius 8, SemiBold 30 (text box 68 x 40), default label "A".
- Default: panel, text. Focused: panel + 6 focus border, text. Pressed: focus #C2ACF4 fill + 4 text border, label onfocus #14111C.
- Note: at 72 wide with a 6 px border the glyph box (68) overflows the inner area (60); single glyph fits, wider labels will not.

## 13. Header (12:339)

1336 x 104 (master), row, gap 24, radius 8. Props date (On/Off), dateText "Monday, November 16", time "20:41", status "Connected", networkIcon (default Asset/wifi).
- Time-and-date column 900 x 104: time SemiBold 48 (900 x 66), text color; date Regular 24 (900 x 36), muted.
- Network status 412 x 64, row gap 12: Asset/wifi 32 x 32 + status text 368 x 36, Regular 24, muted, default "Connected".
- Date=On: both lines shown. Date=Off: the node is the same size (104) and the same date text node exists with a black muted fallback token; the Off variant is only meant to hide the date line (intent; the generated code still emits the text, so hide it explicitly and let the time sit top-aligned).
- K05 specimen is scaled 0.7 (935 x 72.8; time 33.6, date 16.8). Use master. K05 caption: "Header variants include date on/off and network icon/text properties."
- Offline icon in the library: Asset/wifi-off (11:200) is available, text for offline is not defined in these frames.

## 14. Empty or error (12:354)

1100 x 400 (master), column, gap 24, padding 32, radius 8, no fill, left-aligned.
- Icon 64 x 64 (No apps = Asset/tv, Configuration error = Asset/x), Title 1036 x 70 SemiBold 48 text color, Body 1036 x 72 (2 lines) Regular 24 muted.
- No apps: title "A clean start", body "Add apps to a category or unhide an app to fill this profile."
- Configuration error: title "Configuration could not be read", body "Your saved configuration is unchanged. Try again or restore a backup."
- Recovery buttons are NOT in the component; K05 caption says "Empty/error states pair with the shared recovery buttons" (use Button Primary/Secondary beneath; labels not defined in this page).
- K05 specimen is scaled 0.75 (825 x 300, padding 24, gap 18, icon 48, title 36, body 18). Use master.

## 15. Overlay menu (21:95)

600 x 940, panel #282234, radius 16, padding 32, column, gap 16, top-aligned, no border, no shadow. Right-side panel (K05 places it at x=1224, y=70 of the screen i.e. right inset 96, top inset 70).
- App title row 536 x 64, gap 24: app icon 64 x 64 (Asset/brand-plex in specimen) + title 448 x 52 SemiBold 30 text color, default "Plex".
- Actions column 536 x 720, gap 8, nine Menu item slots (520 x 68 each, 9 x 68 + 8 x 8 = 676 <= 720). Remaining space stays empty at the bottom.
- Item labels in order: "Open" (shown Focused), "Add to Favorites", "Add to category…", "Remove from categories", "Rename", "Reorder in row", "Hide from home", "App info", "Cancel".
- Description in Figma: "Reusable side-panel app menu. Nine action slots. Hide irrelevant rows. Back returns focus to the app. App info opens Android system UI."
- Items are 520 wide in a 536 column (16 px unused right).

## Icons used (64 x 64 asset frames, Lucide ISC unless stated; sized down by each component)

search, volume-2, gamepad-2, user, power, keyboard, x, chevron-right, sliders-horizontal, circle-play, settings, download, tv, bell, plus, check, hard-drive, wifi, image, wifi-off, star, move-horizontal, clock, arrow-left, upload, monitor, chevron-left, moon. Brand (Simple Icons): brand-googleplay, brand-youtube, brand-steam, brand-netflix, brand-twitch, brand-crunchyroll, brand-plex (plex artwork is yellow/gold, keep original branding).
Sizes in use: chevron 32, toast icon 32, HDMI monitor 32, header wifi 32, nav icon 28, quick setting icon 36, empty/error icon 64, app card artwork 72, favorite star 24 (inside 24 badge), check badge 32 (wallpaper) / 24 (profile selected), overlay app icon 64.

## Implementation notes

- Focus outline is INSIDE the node box in the design (Figma inside stroke). In Compose use `Modifier.border` drawn inside the clip; do not add outer padding or let layout grow.- Dp conversion (x0.5): focus 6 -> 3 dp, pressed 4 -> 2 dp, radius 8 -> 4 dp, 16 -> 8 dp, 24 -> 12 dp; text 24 -> 12 sp, 30 -> 15 sp, 48 -> 24 sp (all sizes simply x0.5; verify on device).
- No blur, shadow or glow is used, so nothing here is unbuildable on Android TV Compose.
- Pressed on App card is a geometry change (container shrinks about 5 percent, label stays) plus 4 px white outline; implement as a graphicsLayer scale on the artwork container only, not the label (a uniform `scale` would also shrink the label, which stays fixed in the design).
- Contradictions / ambiguities found:
  1. Specimen frames are scaled copies (K02 0.8x, K05 header 0.7x, K05 empty 0.75x). Use the component masters, not specimen sizes.
  2. Empty HDMI master carries the Connected detail string; the specimen overrides with "No device". Empty has no unique styling.
  3. Settings row Action/Header Date=Off use `muted,black` fallback token (token unresolved in those nodes); treat as muted #BDB0D0.
  4. Toggle thumb radius is not emitted (renders square in code); presumably round.
  5. Pressed fill raised == toggle-off track fill, so off track is invisible when a row is pressed.
  6. Library description text on every component says "Default, Focused and Pressed states; no hover" even for Toast/Header/Row heading/Empty, which have no such states.
  7. Section row text is 30 in master but 24 in the K02 specimen only because of the 0.8 scaling of the 30 -> 24.
  8. Favorite badge icon (11:210) is not among the listed assets; star assumed.
  9. K03 Quick setting specimen strings and K01 strings were not separately extracted; they use master defaults ("Sleep timer", "Continue", "Open", "Plex"). K01 caption: "Component specimens: each column compares Default, Focused and Pressed. Multiple focus outlines here are examples, not one runtime screen." K03 caption: "Profile selection and focus are independent. Input status uses text as well as a device icon. These are state specimens."
  10. Not fully inspected via get_design_context: K01 21:197 and K03 21:374 as whole frames (covered through their component masters, the profile specimens 21:379 and HDMI specimens 21:437/21:465, and caption nodes). Screenshots viewed: only App card and Overlay menu.
