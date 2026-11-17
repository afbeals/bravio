# TV launcher design assets

Source: Figma file odpqy4Zqf0oNJBfsQCrT9l. Icons are Lucide (ISC license). Photos are described in the design as Unsplash imagery (license/attribution not verified here).

## Icons (28 non-brand, in `icons/`)

All files: 64x64 viewBox, `fill="none"`, stroke `#F4F0FA` (the Figma text token), stroke-width 2, round caps and joins, no background rect. Brand icons skipped as requested.

Gotcha: the paths are Lucide's 24-grid geometry scaled to 64 (x2.667) but the stroke stayed 2 px, so on the 64 box the line is thin (about 0.75 on the 24 grid, versus Lucide's native 2). For Lucide-weight strokes set stroke-width to about 5.33 on the 64 box, or re-scale the viewBox to 24 and use width 2. Colour is hard-coded `#F4F0FA`; swap for `currentColor` when recolouring. Each SVG has `preserveAspectRatio="none"` and `overflow="visible"` from the export.

| name | node | viewBox | stroke width | paint |
|---|---|---|---|---|
| arrow-left | 11:221 | 0 0 64 64 | 2 | stroke |
| bell | 11:163 | 0 0 64 64 | 2 | stroke |
| check | 11:171 | 0 0 64 64 | 2 | stroke |
| chevron-left | 11:238 | 0 0 64 64 | 2 | stroke |
| chevron-right | 11:129 | 0 0 64 64 | 2 | stroke |
| circle-play | 11:143 | 0 0 64 64 | 2 | stroke |
| clock | 11:217 | 0 0 64 64 | 2 | stroke |
| download | 11:151 | 0 0 64 64 | 2 | stroke |
| gamepad-2 | 11:93 | 0 0 64 64 | 2 | stroke |
| hard-drive | 11:174 | 0 0 64 64 | 2 | stroke |
| image | 11:189 | 0 0 64 64 | 2 | stroke |
| keyboard | 11:114 | 0 0 64 64 | 2 | stroke |
| monitor | 11:230 | 0 0 64 64 | 2 | stroke |
| moon | 11:241 | 0 0 64 64 | 2 | stroke |
| move-horizontal | 11:212 | 0 0 64 64 | 2 | stroke |
| plus | 11:167 | 0 0 64 64 | 2 | stroke |
| power | 11:107 | 0 0 64 64 | 2 | stroke |
| search | 11:84 | 0 0 64 64 | 2 | stroke |
| settings | 11:147 | 0 0 64 64 | 2 | stroke |
| sliders-horizontal | 11:132 | 0 0 64 64 | 2 | stroke |
| star | 11:209 | 0 0 64 64 | 2 | stroke |
| tv | 11:156 | 0 0 64 64 | 2 | stroke |
| upload | 11:225 | 0 0 64 64 | 2 | stroke |
| user | 11:100 | 0 0 64 64 | 2 | stroke |
| volume-2 | 11:88 | 0 0 64 64 | 2 | stroke |
| wifi | 11:183 | 0 0 64 64 | 2 | stroke |
| wifi-off | 11:200 | 0 0 64 64 | 2 | stroke |
| x | 11:125 | 0 0 64 64 | 2 | stroke |

## Wallpapers

The two sample photos are the original uploaded fills, fetched via download_assets rawImages (not thumbnail renders).

| file | design name | pixel size | format | verdict |
|---|---|---|---|---|
| `wallpapers/alpine-lake.jpg` | Alpine lake (identical bytes to 'Wallpaper asset / light' 18:1572) | 1920x1080 | JPEG (progressive, ~465 KB) | Usable as a 1920x1080 wallpaper; no upscale headroom for 4K |
| `wallpapers/night-sky.jpg` | Night sky (identical bytes to 'Wallpaper asset / dark' 18:1753) | 1920x1080 | JPEG (progressive, ~416 KB) | Usable as a 1920x1080 wallpaper; no upscale headroom for 4K |

The files are JPEG, not PNG, so they are saved as `.jpg` (md5 matches the Figma source; converting would only bloat them). The source is not thumbnail sized, so it is fine for 1080p. A 4K TV would upscale it.

## Solid and gradient presets (frame W01 18:1260, 360x196 swatch, radius 8)

| preset | fill |
|---|---|
| Ink | solid `#14111C` (token bg) |
| Raised Ink | solid `#282234` (token panel) |
| Iris dusk | linear gradient, left to right (to-r), from `#C2ABF5` to `#14111C` |
| Night gradient | same as Iris dusk in the exported thumbnail: left to right, `#C2ABF5` to `#14111C` |

The code export gives only two stops and a horizontal direction; Iris dusk and Night gradient came out identical, so the design may differ by a stop position or angle that the export does not show. Check the Figma paint if they should differ. Other tokens: text `#F4F0FA`, muted `#BDB0D0`, focus ring `#C2ACF4` (6 px).

Selected thumbnail has a 32x32 black check badge (Asset/check) at top 16, left 312.

## Missing-artwork card (H01 13:95, 'App card / Sony TV')

Not a separate asset. It is the `Asset/tv` icon (node 11:156, `icons/tv.svg`) at 72x72, centred in a 244x137 `#282234` container with radius 8, label in muted `#BDB0D0` below.
