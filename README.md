# TV Launcher

A lightweight custom Android TV home screen (Kotlin + Compose for TV), built for a Sony BRAVIA XBR-55X900H
(Android 12). The design brief is in [docs/DESIGN_BRIEF.md](docs/DESIGN_BRIEF.md).

## Build and install

Needs JDK 17 and the Android SDK (`local.properties` holds the SDK path and is not committed).

```bash
./gradlew :app:testDebugUnitTest      # unit tests
./gradlew :app:assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
```

Install over network ADB (the TV's Developer options must have network debugging on). If `adb` is not on your
PATH, use `/opt/homebrew/bin/adb`. Replace `<tv-ip>` with the TV's address (port 5555):

```bash
adb connect <tv-ip>:5555
adb -s <tv-ip>:5555 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <tv-ip>:5555 shell am start -n dev.tvlauncher/.MainActivity   # open it without changing Home
```

Installing does not change the Home button. The stock launcher keeps priority until it is disabled
(see "Becoming the Home app" below).

## Wallpaper

Each profile has its own wallpaper. Choose it in **Launcher settings > Wallpaper**: six presets (Ink, Raised Ink,
Iris dusk, Night gradient, and the bundled photos Alpine lake and Night sky) plus **From TV storage** for your own
pictures. Choosing one shows a preview over a mock Home; **Apply wallpaper** saves it for the current profile and
**Cancel** keeps the old one. Photos get a 70% black scrim and Home draws an opaque reading surface over them, so
text is always readable. Screens without a sidebar show plain Ink instead of a photo.

The bundled photos are the two sample images from the Figma design (`docs/design-spec/wallpapers/`). The design
file describes them as Unsplash images; their license and attribution have not been checked.

### Add your own image

The TV has no file picker, so the launcher lists the images in its own folder. Copy any `.jpg`, `.jpeg` or `.png`
there (any file name, any size; it is scaled to about 1920 px wide when loaded):

```bash
adb -s <tv-ip>:5555 shell mkdir -p /sdcard/Android/data/dev.tvlauncher/files
adb -s <tv-ip>:5555 push ~/path/to/photo.jpg /sdcard/Android/data/dev.tvlauncher/files/
```

Then open **Wallpaper > From TV storage**: every image in the folder is listed with its size. Several profiles can
use the same file. To remove an image, `adb shell rm` it; a profile that used it falls back to Ink.

### Troubleshooting

- **The image is not listed:** it must be a non-empty `.jpg`, `.jpeg` or `.png` directly in
  `/sdcard/Android/data/dev.tvlauncher/files/`. Check with `adb -s <tv-ip>:5555 shell ls -la` on that folder, then
  leave and reopen the list (it is read when the list opens).
- **"adb: command not found":** use `/opt/homebrew/bin/adb` or add `/opt/homebrew/bin` to your PATH.

## Features

- **Design:** the Ink & Iris design from Figma: a library sidebar (profile, Home, Search, one entry per row,
  Launcher settings), banner cards, row headings with counts, a clock with the Wi-Fi indicator and date at the top
  right, and toasts for confirmations. Fonts are Inter (OFL, `docs/licenses`).
- **Rows:** Favorites, Recently used, Most used, your categories, Other apps, HDMI inputs and Quick settings.
  Each can be turned on or off and **reordered per profile** (Launcher settings > Row order: OK picks a row up,
  Up/Down moves it).
- **Long-press OK on an app:** a panel on the right with open, add or remove favorite, add to a category (with ✓ for
  the ones it is in), remove from categories, **Reorder in row** (Left/Right moves the app, OK saves, Back
  cancels), rename, hide, app info.
- **Search:** the Search entry in the sidebar filters apps by name with the on-screen keyboard.
- **Profiles:** up to six, each with its own clock and date options, categories, favorites, hidden apps, renames,
  usage, wallpaper, row choices and navigation-sound setting. Pick one from the profile name in the sidebar,
  or choose "Ask who's watching" at launch; **Create new** is offered while there are fewer than six.
- **Look:** wallpaper presets and photos, three focus styles (outline is the default, scale, glow), an optional dim
  palette that follows the TV's light sensor, optional navigation clicks per profile.
- **Hold Back on Home:** shows the full-screen clock (a short Back does nothing on Home).
- **Errors:** if the saved settings cannot be read, Home shows "Configuration could not be read" with Try again,
  Restore backup and Start fresh; nothing is overwritten until you choose. A failed save shows a toast and changes
  nothing.

## Screensaver

A drifting clock (180 px time, 30 px date, moves up to 24 px) that uses almost no memory. It uses the dim palette
between 22:00 and 07:00 and can sit over the profile's wallpaper (Launcher settings > Screensaver wallpaper). It is
opt-in; the TV's stock screensaver stays as it is until you switch:

```bash
adb -s <tv-ip>:5555 shell settings put secure screensaver_components dev.tvlauncher/.ClockDreamService
adb -s <tv-ip>:5555 shell settings put secure screensaver_enabled 1
```

To go back to the stock screensaver, first read the old value (`adb shell settings get secure screensaver_components`
before changing it, and keep it), then put it back with `settings put secure screensaver_components <old value>`.
The idle delay is the TV's own "Start now / Time" setting under Settings > Device Preferences > Screen saver.
**Launcher settings > Preview screensaver** shows the in-app clock; the same clock opens with a long Back on Home.

## Backup and restore

**Launcher settings > Back up settings** writes `launcher-backup.json` to the app folder. Copy it off the TV with
`adb pull /sdcard/Android/data/dev.tvlauncher/files/launcher-backup.json`, or push one back to the same path and
use **Restore settings** (it asks first, and an invalid file never replaces your settings).

## Testing

`docs/TEST_CHECKLIST.md` lists every on-device check. Unit tests (`./gradlew :app:testDebugUnitTest`) cover the row
layout, config storage, profile and app edits, the drift path and the dim and network logic.

## Configuration

All settings (profiles, categories, hidden apps, clock, wallpaper choice) live in one JSON file in the app's
private storage (`config.json`). It is rewritten atomically. A missing file means a first run (defaults); an
unreadable one shows the error screen and is kept as `config.corrupt.json` if you start fresh.
A debug build lets you read it with `adb shell run-as dev.tvlauncher cat files/config.json`.

## Becoming the Home app

Android picks the Home app with the highest priority, and the stock launcher outranks any normal app. The
tested way, with the safety net, is to disable only `com.google.android.tvlauncher`:

```bash
adb -s <tv-ip>:5555 shell pm disable-user --user 0 com.google.android.tvlauncher
adb -s <tv-ip>:5555 shell cmd package set-home-activity --user 0 dev.tvlauncher/.MainActivity
```

To go back to the stock home screen:

```bash
adb -s <tv-ip>:5555 shell pm enable com.google.android.tvlauncher
adb -s <tv-ip>:5555 shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
```

Check Home, Cast, Input and the Settings key after any change, and again after a reboot or a TV firmware update.
