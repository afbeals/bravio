# On-device test checklist

Run through this once everything is installed. Tick items as you go and note anything odd.
Install first (see README), then open the launcher without changing Home:

```bash
adb -s <tv-ip>:5555 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <tv-ip>:5555 shell am start -n dev.tvlauncher/.MainActivity
```

## Home screen

- [ ] Clock and date show at the top left; the network badge (Wi-Fi / Ethernet / Offline) and a Search button at the top right.
- [ ] First focus lands on the first app card, not on the Search button.
- [ ] D-pad moves through cards and rows; OK on a card opens the app; Back inside the app returns to the launcher.
- [ ] Rows appear in order: Favorites, Recently used, Most used, categories, Other apps ("Apps" if nothing else), Inputs, Quick settings.
- [ ] A card with a long name does not break the layout.

## App menu (hold OK on a card)

- [ ] The menu opens and **stays open after releasing OK**; a quick press still opens the app.
- [ ] Add to favorites, then a Favorites row appears; Remove from favorites removes it.
- [ ] Add to category... lists the presets (Video, Music, Games, Kids, Tools, Family); picking one creates the row.
- [ ] Remove from categories only appears for an app that is in one.
- [ ] Move left / Move right (only from a Favorites or category row) reorders and the menu stays open.
- [ ] Rename...: the on-screen keyboard opens, Save changes the label, "Use the original name" restores it.
- [ ] Hide from home hides it; App info opens the Android app screen; Cancel and Back close the menu.
- [ ] The menu fits on screen and scrolls with the D-pad when it has many entries.

## Smart rows and search

- [ ] Open an app: it appears under Recently used. Open the same app 2+ times: it appears under Most used.
- [ ] Search button: keyboard opens, typing filters the apps live, OK on a result opens it and returns home.
- [ ] Back leaves the search screen.

## TV controls

- [ ] Inputs row lists HDMI 1-4 (and Composite). Connected ones say "Connected" or "Standby", the rest "No signal".
- [ ] OK on a connected input switches the TV to it; Home returns to the launcher.
- [ ] Settings > "Quick settings row: On": tiles show for Sleep timer, Picture, Sound, Bravia Sync, Notifications, Wi-Fi.
- [ ] Each tile opens the right Sony or Android screen. Note any tile that does nothing or is missing.

## Settings and profiles

- [ ] Launcher settings opens from the bottom of the home screen; Back returns home.
- [ ] Clock, time format, date and date format toggles change the header (per profile).
- [ ] Add profile creates a clean profile; Switch profile flips between them; each keeps its own categories, favorites, hidden apps and wallpaper.
- [ ] Row toggles (Favorites, Recently used, Most used, HDMI inputs, Quick settings, Other apps) show and hide those rows for the active profile only.
- [ ] Hidden apps (N) lists them and unhides one at a time.
- [ ] "On launch: ask who's watching" shows the picker when the launcher starts (`am force-stop dev.tvlauncher`, then the start command).
- [ ] Delete profile works and never deletes the last one.

## Look

- [ ] Wallpaper cycles through the presets; "Your image" appears after pushing `wallpaper.jpg` (see README) and fills the screen with readable text.
- [ ] Focus style cycles scale / outline / glow and each is clearly visible from the couch.
- [ ] Dim wallpaper in the dark: On, then cover the light sensor or wait until night; the wallpaper darkens and brightens again without flicker.

## Clock and screensaver

- [ ] Back on the home screen (with no menu open) shows the full-screen clock; any key returns home.
- [ ] The clock moves to a new spot each minute and never touches an edge.
- [ ] Settings > "Back on the home screen: do nothing" makes Back do nothing.
- [ ] Screensaver service: see README ("Screensaver"); a dim drifting clock shows after the idle timeout.

## Backup and restore

- [ ] "Back up settings to a file" shows a confirmation; `adb pull /sdcard/Android/data/dev.tvlauncher/files/launcher-backup.json` gets the file.
- [ ] Change something, then "Restore settings from backup" asks for a second press, then restores the saved state.
- [ ] A broken backup file (edit it so it is not valid JSON) is rejected and nothing changes.

## Memory (release build)

```bash
./gradlew :app:assembleRelease
adb -s <tv-ip>:5555 install -r app/build/outputs/apk/release/app-release.apk
adb -s <tv-ip>:5555 shell am start -n dev.tvlauncher/.MainActivity
adb -s <tv-ip>:5555 shell dumpsys meminfo dev.tvlauncher | grep "TOTAL PSS"
```

- [ ] Total PSS is 70 MB or less (stock launcher: about 120 MB). Above 90 MB is a no-go.

## Becoming the Home app (last, with the safety net)

- [ ] Disable only the stock launcher and set ours as Home (README, "Becoming the Home app").
- [ ] The Home button, Netflix button, Input button, Settings key and voice search all work.
- [ ] Cast is still advertised from a phone; reboot the TV and check Home, Cast and the inputs again.
- [ ] Reverting with the stock-launcher commands in the README restores the original home screen.
