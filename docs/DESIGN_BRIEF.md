# TV Launcher: Design Brief

A custom Android TV home screen for a Sony BRAVIA 55" 4K TV (Android 12). It replaces the stock launcher.
It is driven only by a TV remote (D-pad, OK, Back, Home, Settings key), from about 3 metres away.
Use this brief to produce Figma designs for every screen and state below.

Legend: **[built]** exists today with a plain placeholder look, **[later]** is a possible follow-up.

Everything marked built works on the TV today with plain placeholder styling, and the design restyles it.
Design-only (not built yet, so design them freely): a wallpaper thumbnail picker (the build cycles with a button),
a per-card favorite indicator, confirmation toasts, avatars on profile tiles, and device names on HDMI cards.

---

## 1. Hard constraints (design within these)

| Topic | Rule |
| --- | --- |
| Canvas | 1920 x 1080 (1080p, 16:9). Design at 1x, build in dp at 2x density, so 960 x 540 dp. |
| Safe area | Keep all content 5% inside each edge (96 px left/right, 54 px top/bottom). TVs crop edges. |
| Input | Remote only: D-pad (4 directions), OK (press and **long-press**), Back, Home, Settings key. No touch, mouse or hover. |
| Focus | Exactly one element is focused at a time. **Every focusable element needs a focused state** that reads clearly from 3 m (scale, border, glow or fill, never color alone). |
| Text size | Body 18 px or larger, labels 14 px at the very minimum, headings 28 px or larger. Contrast at least 4.5:1 on any wallpaper. |
| Back | Back never exits the launcher. It closes a menu, then leaves Settings, then does nothing. |
| Performance | Tight RAM budget (target 70 MB or less, stock launcher uses about 120 MB). Avoid full-screen blurs, video backgrounds, large animated images. Subtle motion (focus scale, fades under 200 ms) is fine. |
| Fonts and art | Custom fonts and icons are fine if bundled and small. |
| Languages | English only for now. Leave room for text up to 30% longer. |

---

## 2. Sample content to use in mockups

Real apps on this TV: Netflix, Plex, Crunchyroll, YouTube, Steam Link, Twitch, Google Play Store, myTuner (a
custom internet radio app), plus system apps (Settings, Sony TV, Input).
HDMI inputs: HDMI 1 to HDMI 4. Profiles: "Default", "Allan", "Kids", "Guest".
Category presets: Video, Music, Games, Kids, Tools, Family. (Favorites is its own row, not a category.)

App icon shapes vary: most apps give a square icon, TV apps often give a 16:9 banner. **Design both card
shapes** (square icon card and 16:9 banner card) and say which is the default.

---

## 3. Screens

### 3.1 Home **[built]**

The screen shown after every Home press.

- **Header**: clock and date (per-profile on/off, see 4.1), optional status icons [built] (network).
- **Rows**: vertically stacked, each row has a title and a horizontally scrolling list of app cards.
  - Category rows, in the order the profile defines.
  - A trailing "Other apps" row for apps in no category (title is "Apps" when it is the only row).
  - [built] Optional rows, each switchable per profile: Favorites, Most used, Recently launched,
    HDMI inputs, Quick settings.
- **Footer controls** (end of the list): "Unhide all (N)" (only when apps are hidden) and "Launcher settings".
- **Wallpaper** behind everything [built], see 3.6.

States to design: default, focused card, focused card at row start and row end (scroll position), empty
row (should never show), very long app name, 40 or more apps in one row, no icon available (fallback).

### 3.2 App card **[built]**

App icon or banner plus the app name. States: default, **focused** (most important), pressed.
Optional [built]: small indicator for "Favorite". Long-press OK opens the app menu (3.3).

### 3.3 App menu (long-press OK on a card) **[built]**

A dim overlay with a vertical list of actions for one app, with the app name as its title.

- Main: Open, Add to category..., Remove from categories (only if it is in one), Hide from home, App info,
  Cancel. [built] Add: Rename, Add to Favorites.
- Category picker: all of the profile's categories plus the presets, then Back.

Design: how the overlay looks (centered list vs side panel), the focused item, and how the app's icon is
shown in the title.

### 3.4 Settings **[built]**

A scrolling list of items, reached via the "Launcher settings" button. Items, grouped:

- **Profiles**: Switch profile (shows current and count), Add profile, Delete profile.
- **Clock and date**: Clock on/off, Time format 12/24-hour, Date on/off, Date format (cycles through 4
  examples, shown live).
- **Rows**: Show "Other apps" row on/off. [built] Toggles for Favorites, Most used, Recent, HDMI inputs, Quick settings.
- **Launch**: "On launch: open the home screen / ask who's watching".
- [built] Wallpaper, Screensaver, Focus style, Backup and restore, Hidden apps list.
- Done.

Design a reusable **settings row** with these variants: toggle (on/off), cycle (value text, press to
advance), action (button), and a section header. Focus state needed for each.

### 3.5 Profile picker **[built]**

"Who's watching?" with one large focusable tile per profile (name, optional avatar color or initial).
Shown at launch only if the setting says so. Design for 1 to 6 profiles.

### 3.6 Wallpaper **[built]**

Per profile. Options: solid or gradient presets, a bundled image set, an image from the TV's storage.
Design the **wallpaper picker** (grid of thumbnails, focus, selected check) and show how rows, header and
text stay legible on a bright and a dark image (scrim or gradient behind rows).
[built] Day/dim mode: darker wallpaper at night, using the TV's light sensor.

### 3.7 TV controls rows **[built]**

- **HDMI inputs row**: one card per input (HDMI 1 to 4, with a connected or empty indicator and the device
  name if known). OK switches the TV to that input.
- **Quick settings row**: tiles for Sleep timer, Picture, Sound, Bravia Sync, Notifications, Wi-Fi. Each is
  an icon plus a label.

### 3.8 Smart rows and search **[built]**

- **Most used** and **Recently launched** rows (counts stay on the TV, per profile).
- **App search**: a way to type or filter the app list (design the entry point and results, using the TV's
  on-screen keyboard).

### 3.9 Screensaver **[built]**

- Back-button "clock mode": full-screen large clock that drifts slowly to prevent burn-in.
- A system screensaver (idle) with the same clock and optional wallpaper.
Design both, including a dim night variant.

### 3.10 Layout tools **[built]**

- **Backup and restore**: export or import the whole configuration as one file. Design the confirm and
  success or error states.
- **Rename app**: on-screen keyboard text entry.
- **Hidden apps list**: lets the user unhide apps one by one (today there is only "Unhide all").
- **Reorder** apps in a row (move left/right with the D-pad).

### 3.11 Status and polish **[built]**

- Network status icon in the header (connected, no internet).
- **Focus style presets**: let the user pick how focus looks (for example: scale, outline, glow). Design 2 or 3.

---

## 4. Per-profile settings (what changes between profiles)

### 4.1 Per profile

Clock on/off, 12/24-hour, date on/off, date format, category rows and their members, hidden apps, whether
the "Other apps" row shows, wallpaper, and which optional rows are enabled.

### 4.2 Global

Launch behavior (ask who's watching, or open home), focus style, screensaver.

Profiles are a convenience, not security: hidden apps are still launchable from system screens.

---

## 5. Reusable components to design

1. App card: square icon and 16:9 banner, all states.
2. Row with title (plus an optional "see all" or count).
3. Header: clock, date, status icons.
4. Button (primary, secondary), with focus state.
5. Settings row: toggle, cycle, action, section header.
6. Overlay menu with list items.
7. Profile tile.
8. Wallpaper thumbnail with selected state.
9. Quick-settings tile and HDMI input card.
10. Toast or snackbar for confirmations ("Added to Video", "Hidden").
11. Empty and error states (no apps in a profile, config could not be read).

---

## 6. Navigation map

```text
Home press
  -> Profile picker (only if "ask at launch") -> Home
Home
  -> OK on card: opens the app
  -> long-press OK on card: App menu -> category picker / app info / hide
  -> "Launcher settings" -> Settings -> Wallpaper / Backup / ...
  -> Back: nothing (menu closes first if open)
Settings key (remote): opens Android system Settings
```

---

## 7. Motion

Keep motion short and purposeful: focus scale or highlight (100 to 150 ms), menu fade in or out (150 ms),
row scroll that follows focus. No auto-playing animation on Home. Respect that the TV is always on.

---

## 8. Deliverables requested

1. Home screen at 1920 x 1080 with 3 rows, with the focus on a card, on both a dark wallpaper and a light one.
2. The component set from section 5, with default, focused and pressed states.
3. Every screen in section 3, using the mockup content from section 2.
4. A short style sheet: colors (with dark and dim variants), type scale, spacing grid, corner radii, focus style.
5. Open question for the designer: icon cards or banner cards by default, and how the two mix in one row.
