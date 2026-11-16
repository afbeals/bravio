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

Each profile has its own wallpaper. Five gradient presets (Midnight, Ocean, Dusk, Forest, Charcoal) are built in
and cost almost no memory. You can also use your own image.

### Choose a wallpaper

On the TV: **Launcher settings > Wallpaper** cycles through the options. Press Done to return home.
"Your image" only appears in the cycle while an image file exists on the TV.

### Set or change your own image

1. Pick a JPG or PNG. Any size works: it is scaled down to about 1920 px wide when loaded. Photos with dark or
   mid tones read best, because a dark overlay is added so the text stays legible.
2. Create the folder (first time only) and copy the image to the TV, **named exactly `wallpaper.jpg`**:

   ```bash
   adb -s <tv-ip>:5555 shell mkdir -p /sdcard/Android/data/dev.tvlauncher/files
   adb -s <tv-ip>:5555 push ~/path/to/photo.jpg /sdcard/Android/data/dev.tvlauncher/files/wallpaper.jpg
   ```

   A PNG can be pushed as `wallpaper.png` instead. The launcher looks for `wallpaper.jpg`, `wallpaper.jpeg`,
   then `wallpaper.png`, in that order, so remove the older file if you switch formats.
3. On the TV, open **Launcher settings** (leave and reopen it if it was already open, because the image is
   only looked for when Settings opens) and press **Wallpaper** until it reads **Your image**.

There is one image file for the whole TV: every profile that selects "Your image" shows the same picture.
Profiles can still differ by using a preset instead.

### Replace the image

Push a new file to the same path. The launcher reads the file when the home screen opens, so go to Settings
and back, or relaunch the launcher: `adb -s <tv-ip>:5555 shell am force-stop dev.tvlauncher`, then the
`am start` command above.

### Remove the image

```bash
adb -s <tv-ip>:5555 shell rm /sdcard/Android/data/dev.tvlauncher/files/wallpaper.jpg
```

A profile that was set to "Your image" then falls back to the first preset the next time Wallpaper is pressed
and shows the Midnight gradient until then. Uninstalling the app also deletes the folder.

### Troubleshooting

- **"Your image" never appears:** the file must be named `wallpaper.jpg` (or `.jpeg` / `.png`), be non-empty, and
  sit in exactly `/sdcard/Android/data/dev.tvlauncher/files/`. Check with
  `adb -s <tv-ip>:5555 shell ls -la /sdcard/Android/data/dev.tvlauncher/files`.
- **The picture is blank or the gradient shows:** the file may not be a valid image. Try re-exporting it as JPG.
- **Text is hard to read:** use a darker image; the overlay is fixed (see `WallpaperBackground` in
  `app/src/main/java/dev/tvlauncher/Wallpaper.kt`).
- **"adb: command not found":** use `/opt/homebrew/bin/adb` or add `/opt/homebrew/bin` to your PATH.

## Features

- **Rows:** Favorites, Recently used, Most used, your categories, Other apps, HDMI inputs and Quick settings.
  Each row can be turned on or off per profile in Launcher settings.
- **Long-press OK on an app:** open, add or remove favorite, add to or remove from a category, move left or
  right within a row, rename, hide, app info.
- **Search:** the Search button (top right) filters apps by name with the on-screen keyboard.
- **Profiles:** each has its own clock and date options, categories, favorites, hidden apps, renames, usage,
  wallpaper and row choices. Switch any time in Launcher settings, or choose "ask who's watching" at launch.
- **Look:** gradient or custom wallpaper, three focus styles (scale, outline, glow), an optional dark dim mode that
  follows the TV's light sensor.
- **Back button:** on the home screen it shows a full-screen drifting clock (can be turned off).

## Screensaver

A dim, drifting clock that uses almost no memory. It is opt-in, and the TV's stock screensaver stays as it is
until you switch:

```bash
adb -s <tv-ip>:5555 shell settings put secure screensaver_components dev.tvlauncher/.ClockDreamService
adb -s <tv-ip>:5555 shell settings put secure screensaver_enabled 1
```

To go back to the stock screensaver, first read the old value (`adb shell settings get secure screensaver_components`
before changing it, and keep it), then put it back with `settings put secure screensaver_components <old value>`.
The idle delay is the TV's own "Start now / Time" setting under Settings > Device Preferences > Screen saver.

## Backup and restore

**Launcher settings > Back up settings to a file** writes `launcher-backup.json` to the app folder. Copy it off the
TV with `adb pull /sdcard/Android/data/dev.tvlauncher/files/launcher-backup.json`, or push one back to the same
path and use **Restore settings from backup** (it asks twice, and an invalid file never replaces your settings).

## Testing

`docs/TEST_CHECKLIST.md` lists every on-device check. Unit tests (`./gradlew :app:testDebugUnitTest`) cover the row
layout, config storage, profile and app edits, the drift path and the dim and network logic.

## Configuration

All settings (profiles, categories, hidden apps, clock, wallpaper choice) live in one JSON file in the app's
private storage (`config.json`). It is rewritten atomically, and a missing or corrupt file falls back to defaults.
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
