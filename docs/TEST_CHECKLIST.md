# On-device test checklist

Run through this once everything is installed. Tick items as you go and note anything odd.
Install first (see README), then open the launcher without changing Home:

```bash
adb -s <tv-ip>:5555 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <tv-ip>:5555 shell am start -n dev.tvlauncher/.MainActivity
```

## Home screen

- [ ] Sidebar on the left: LIBRARY, your profile name, Home, Search, one entry per row, Launcher settings at the bottom.
- [ ] Title "Your library" with a subtitle; clock at the top right with the Wi-Fi icon and date under it.
- [ ] Unplug or disable Wi-Fi: the icon becomes crossed out and the date turns into "No internet" (no dialog).
- [ ] First focus lands on the first app card. The focused card has a purple 6 px outline and a brighter name, and
      nothing is clipped at the row edges (try Scale and Glow too).
- [ ] D-pad moves through cards and rows; Left from the first card enters the sidebar; picking a row entry jumps to it.
- [ ] Row headings show counts ("5 apps", "1 app"); a long row shows "i / N"; Most used says "On this TV".
- [ ] A card whose name is longer than two lines shows the full name in the subtitle ("Focused: ...").
- [ ] Favorites show a small star badge. Apps without a banner show their icon centered in the slot.
- [ ] A profile with no apps shows "A clean start" with Hidden apps and Switch profile.

## App menu (hold OK on a card)

- [ ] The panel opens on the right and **stays open after releasing OK**; a quick press still opens the app.
- [ ] Add/Remove favorite shows a toast ("Added to Favorites") and the star badge appears.
- [ ] Add to category... lists categories with ✓ for current ones; picking one shows "Added to <name>".
- [ ] Reorder in row (Favorites and category rows only): the heading reads "<Row> · moving <app>" with "n / N";
      Left/Right move the app, OK saves ("<Row> order saved"), Back restores the old order.
- [ ] Rename...: keyboard opens; Save name changes the label, "Use original name" restores it.
- [ ] Hide from home toasts "<app> hidden from Home"; App info opens the Android screen; Cancel/Back closes the panel.

## Search

- [ ] Search entry: keyboard opens, typing filters live, the subtitle counts results ("3 apps found").
- [ ] A query with no match says "No apps found" and shows Edit search.

## TV controls

- [ ] Inputs row lists the HDMI inputs with their status; OK switches the TV to a connected input.
- [ ] Quick settings tiles (Sleep timer, Picture, Sound, Bravia Sync, Notifications, Wi-Fi) open the right screens.

## Settings, profiles and wallpaper

- [ ] Launcher settings: rows with switches or chevrons, a help panel that changes with focus, a key-hint line.
- [ ] Clock, time format, date and date format change the header (per profile).
- [ ] Switch profile opens the picker; the focused name is purple; **Create new** shows below 6 profiles and
      creates one (empty or duplicate names are refused).
- [ ] Delete profile asks first with Cancel focused; the last profile cannot be deleted.
- [ ] Row toggles show/hide rows for the active profile only; Row order: OK picks up a row, Up/Down move it, OK drops.
- [ ] Hidden apps lists them with Unhide, Unhide all and Done; with none it says "Nothing is hidden".
- [ ] Navigation sounds (per profile): on, a click plays when focus moves; off, silence.
- [ ] On launch: "Ask who's watching" shows the picker at start (`am force-stop dev.tvlauncher`, then start).
- [ ] Wallpaper: six presets with check on the current one; OK shows the Home preview; Apply saves, Cancel does not.
- [ ] Photo wallpapers: scrim plus solid reading surface, text readable. From TV storage lists pushed images.
- [ ] Dim at night: cover the light sensor (or wait for night); the palette darkens and recovers without flicker.
- [ ] Focus style cycles Outline / Scale / Glow.

## Clock and screensaver

- [ ] Holding Back on Home shows the large clock; any key returns. A short Back on Home does nothing.
- [ ] The clock moves slightly each minute and stays within 24 px.
- [ ] Screensaver wallpaper On: the clock sits on a panel over the picture. Dim at night uses the dark palette.
- [ ] Preview screensaver in Settings shows the clock. System screensaver: see README ("Screensaver").

## Backup, restore and errors

- [ ] Back up settings toasts "Saved launcher-backup.json"; `adb pull` gets the file.
- [ ] Change something, Restore settings asks first, then restores; a broken backup is rejected, nothing changes.
- [ ] Corrupt config: `adb shell run-as dev.tvlauncher sh -c 'echo x > files/config.json'` (debug build) then open the
      launcher: "Configuration could not be read" with Try again / Restore backup / Start fresh.
- [ ] A failed save shows "Could not save changes. Try again." and changes nothing (hard to trigger; code-reviewed).

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
