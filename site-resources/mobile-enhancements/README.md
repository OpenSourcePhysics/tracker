# Tracker mobile enhancements

This folder contains the mobile-facing support files for
`../TrackerStudentMobile.html`. It improves the existing GPL Tracker Online
application without turning this repository into the future commercial mobile
companion.

Current verified application:
<https://iwant2study.org/tracker/TrackerStudentMobile.html?v=20260722-115>

Pedagogy and project mission:
<https://iwant2study.org/tracker/TrackerStudentMobilePedagogy.html?v=20260721-doc2#mobile-mission>

## Included

- A full-screen launcher that preserves Tracker's native menus, toolbar, views,
  and save workflow without adding a second header or navigation bar.
- Larger native popup menus with 48 px touch targets and readable first-level
  widths.
- Tap activation for SwingJS submenus that otherwise require mouse hover.
- Double-clicking a Library Browser `.trz` record reuses the same download and
  open action as the toolbar Open button instead of following SwingJS's stale
  cached-file branch.
- Left-aligned, viewport-width submenu sheets on phones so labels and nested
  choices are not clipped off-screen.
- Library Browser-safe sizing: touch targets are enlarged locally without
  changing SwingJS's global font scale, so online catalogues render normally.
- Coordinated toolbar reflow enlarges icon canvases and their native wrappers
  together, preventing overlapping or clipped controls; narrow screens use a
  single horizontally scrollable toolbar row.
- The isolated SwingJS runtime maps clicks on reflowed or horizontally scrolled
  controls back into their original Java bounds. Each enlarged icon therefore
  invokes its own native popup, dialog, or action instead of a neighbouring
  control.
- Subtle button boundaries distinguish each toolbar action, while Tracker's
  native separator slots render as stronger vertical group dividers.
- An isolated SwingJS runtime copy fixes the large-`long` modulo fallback used
  while scrubbing some video timelines. The upstream fallback called the stale
  `Long.div`, `Long.mul`, and `Long.sub` names even though this runtime exposes
  the `$`-prefixed operators.
- The generated Tracker core is post-processed with
  `tools/patch-html5video-core.ps1`. This preserves the required `jsvideo`
  ImageIcon description when HTML5 videos originate from File or String paths,
  preventing Library Browser projects from stopping at a null video dialog.
- The generated core is also post-processed with
  `tools/patch-trackerio-cache-paths.ps1`. Cached TRZ and video paths remain
  decoded filesystem paths, so resources whose filenames contain spaces do not
  become nonexistent literal `%20` files under `/TEMP`.
- Legacy animated-GIF Tracker projects can be upgraded with
  `tools/upgrade-gif-trz-to-mp4.ps1`. It replaces only the embedded video with
  an H.264 MP4 while retaining the TRK measurements, coordinate system, frame
  count, and timing. This is used for the Singapore Collection dollar-drop
  project, whose 17-frame GIF otherwise enters the browser movie loader and
  hangs before its video dialog is created.
- Visual Viewport resizing so the Tracker frame responds to rotation.
- A single five-button touch command bar: Project, New track, Measure, Views,
  and More. The controls progressively disclose Tracker's real Java actions;
  they do not replace or duplicate the analysis model.
- Direct Java combo-model selection for track, plot, table, column, and other
  dropdowns. Touch devices receive a 48 px choice sheet instead of SwingJS's
  hover-coordinate popup, while desktop mouse behavior is unchanged.
- Touch marking and dragging on scientific canvases, including Point Mass,
  calibration tools, shared Tracker handles, coordinate origin, and x-axis.
- A shared 24 x 24 px Step hit target, maintained in `Step.java` and mirrored
  into the generated core with `tools/patch-touch-hit-region.ps1`.
- Resilient ComPADRE connectivity checks. The generated core allows eight
  seconds instead of one and does not block application startup with a false
  offline alert; `tools/patch-compadre-startup-probe.ps1` reapplies the fix.

## Build surface

`build-site.xml` already copies all of `site-resources/**` into `site/`, so the
launcher and this folder are included without another Ant target.

## Expected deployed paths

```text
/TrackerStudentMobile.html
/mobile-enhancements/swingjs2-tracker-mobile.js
/mobile-enhancements/tracker-student-mobile.css
/mobile-enhancements/tracker-student-mobile.js
/swingjs/j2s/...
```

`swingjs2-tracker-mobile.js` is the upstream `swingjs2.js` runtime vendored for
this launcher. Its intentional runtime change is limited to the three
`$`-prefixed long operators in `Long.$mod`; keeping it launcher-local avoids
changing the desktop Tracker pages.

After regenerating `core_tracker.z.js`, reapply the HTML5 video correction:

```powershell
powershell -ExecutionPolicy Bypass -File tools/patch-html5video-core.ps1 `
  -CorePath site/swingjs/j2s/core/core_tracker.z.js
powershell -ExecutionPolicy Bypass -File tools/patch-trackerio-cache-paths.ps1 `
  -CorePath site/swingjs/j2s/core/core_tracker.z.js
powershell -ExecutionPolicy Bypass -File tools/patch-touch-hit-region.ps1 `
  -CorePath site/swingjs/j2s/core/core_tracker.z.js
powershell -ExecutionPolicy Bypass -File tools/patch-compadre-startup-probe.ps1 `
  -CorePath site/swingjs/j2s/core/core_tracker.z.js
```

Upgrade an older GIF-backed library project before publishing it:

```powershell
powershell -ExecutionPolicy Bypass -File tools/upgrade-gif-trz-to-mp4.ps1 `
  -InputTrz path/to/dollar_drop_activity.trz `
  -OutputTrz site-resources/mobile-enhancements/compatibility-projects/dollar_drop_activity.trz
```

The mobile launcher supplies the core as a full, versioned path. SwingJS loads
that exact URL and the version query prevents a browser from retaining an older
broken core after deployment.

## Manual smoke test

1. Open the current `TrackerStudentMobile.html` over HTTP(S) on iPadOS Safari
   and Android Chrome.
2. Confirm exactly one Project/New track/Measure/Views/More command bar appears.
3. Tap `New track`, then create two Point Mass tracks without hovering.
4. Open Plot and Table track selectors. Confirm their 48 px choice sheets show
   clean track names and selecting each track changes the native Java combo.
5. Tap `Measure`, create Calibration Stick, and drag both endpoints. Show axes,
   then move the origin and rotate the x-axis.
6. Use `More` to traverse at least four menu levels and confirm Back/Close work.
7. Load a known Tracker project and exercise the native toolbar and save flow.
8. Drag the video timeline rapidly between early and late frames at least 20
   times; confirm frames change and no `Long.div is not a function` alert opens.
9. Use `Project > Open > Library Browser...`. Confirm application startup did
   not display a false ComPADRE-offline alert.
10. In Library Browser open `Collections > Shared Library > Singapore Tracker
   Collection`, select `01_measurement > trz > dollar_drop_activity.trz`, then
   click `Open`; confirm the video and tracked data load without a null-dialog
   error.
11. Rotate portrait to landscape and confirm the Tracker frame resizes.

See `TESTING.md` for the full acceptance checklist.

See `CHANGELOG.md` for the reconstructed deployment history from the first
mobile launcher through the current verified release, together with the rules
for recording future builds.

`../TrackerStudentMobilePedagogy.html` is the public-facing article explaining
Tracker's inquiry-learning value and the purpose of the touch-first student
variant. Its hero image is `tracker-mobile-pedagogy-hero.png`.
