# Tracker Student Mobile change log

This is the deployment history for `TrackerStudentMobile.html` and its
launcher-local mobile enhancement files. The values after `?v=` are dated
cache-buster build identifiers, not semantic versions. A higher number means a
newer deployed asset, but skipped numbers do not necessarily represent public
releases.

## Evidence and scope

- **Verified** means the behavior was exercised against that build, or the
  deployed file was compared with the local source.
- **Recorded** means the build and issue are supported by the project work log
  and screenshots, but that historical file is no longer present locally.
- **Reconstructed** means several closely related iterations were consolidated
  because only the final source and public build URLs remain.
- The mobile launcher files were not committed during the early iterations, so
  a line-by-line source diff for every cache-buster value cannot be recovered.
  Unknown intermediate values are deliberately not assigned invented changes.

## Version history

### 2026-07-18

| Build | Evidence | Changes and outcome |
| --- | --- | --- |
| `20260718-3` | Recorded | First public deployment of `TrackerStudentMobile.html`. Established a separate mobile launcher around Tracker Online. The first remote upload returned an empty/unfinished page and exposed that the launcher assets and deployed paths needed to be handled together. |
| `20260718-6` | Recorded | Added an experimental `Tracker Student` header, top Save action, and bottom Video/Graph/Table navigation. This proved that large touch controls were possible, but duplicated Tracker's own controls and reduced workspace height. These extra controls were subsequently removed. |
| `20260718-7` to `20260718-39` | Reconstructed | Iterative menu sizing, toolbar sizing, viewport, and deployment corrections. Individual public artifacts were not retained, so these values are intentionally not described separately. |
| `20260718-40` | Recorded | Removed the duplicate app header and bottom navigation. Enlarged Tracker's native menu rows and added tap-based submenu support. Began responsive handling for dialogs and the Library Browser. Known limitations at this point: some toolbar icons were still clipped or reverted to desktop size, Shared Library submenus were difficult to enter, and rapid timeline scrubbing could raise `Long.div is not a function`. |
| `20260718-41` to `20260718-50` | Reconstructed | Iterations on native popup positioning, submenu activation, toolbar reflow, Library Browser visibility, and icon hit areas. No retained per-build source snapshots. |
| `20260718-51` | Recorded | Library Browser records could be selected and the toolbar Open action was available. This build exposed two deeper compatibility failures: loading some TRZ projects could stop with a null `dialog` exception, and filenames containing spaces could be rewritten as literal `%20` paths under `/TEMP`. Double-click was not yet equivalent to Open for every record. |

### 2026-07-19

| Build | Evidence | Changes and outcome |
| --- | --- | --- |
| `20260719-52` to `20260719-82` | Reconstructed | System-level Library Browser and SwingJS compatibility work. Added launcher-local runtime isolation, corrected large-long modulo calls used by timeline scrubbing, repaired HTML5-video metadata during generated-core post-processing, preserved decoded cached paths, and added an MP4 compatibility upgrade path for legacy GIF-backed projects. These changes are represented in the current helper scripts and README, but the exact cache-buster for each sub-change was not preserved. |
| `20260719-83` | Recorded | First substantially responsive Library Browser layout: the browser was kept within the viewport and the collection tree/details region became usable on a narrow display. Remaining problems included cramped URL controls, excessive horizontal scrolling, and row selection that could land below the finger. |
| `20260719-84` to `20260719-86` | Reconstructed | Iterative Library Browser layout and event-coordinate adjustments. No retained per-build source snapshots. |
| `20260719-87` | Recorded | Reworked the Library Browser menu bar, URL/search toolbar, collection tree, and details pane. Added larger tree rows, clearer hierarchy, compact tree/details switching, and mobile stacking. This was a major readability improvement, although exact touch selection still required a Java-coordinate bridge. |
| `20260719-88` to `20260719-89` | Reconstructed | Final Library Browser layout refinements and runtime integration before the compatibility baseline. |

### 2026-07-20

| Build | Evidence | Changes and outcome |
| --- | --- | --- |
| `20260720-90` | Confirmed in current launcher | Established the current SwingJS/core compatibility baseline. `TrackerStudentMobile.html` loads the isolated `swingjs2-tracker-mobile.js` and versioned `core_tracker.z.js`. Toolbar controls retain separate visual boundaries and logical hit mapping. Core-generation helper scripts preserve HTML5-video descriptions and decoded cache paths. The converted dollar-drop compatibility project is also retained. |
| `20260720-91` to `20260720-100` | Reconstructed | Touch-selection and library record-opening diagnostics. These were test iterations rather than separately documented releases. |
| `20260720-101` | Verified | Added the exact Library Browser tree-row bridge. A touch selects the Java tree node corresponding to the visible row instead of an offset row. Double-clicking a `.trz` record reuses the same native load workflow as the Library Browser Open action. |
| `20260720-102` to `20260720-104` | Reconstructed | Preparation and instrumentation for scientific-canvas touch interaction. No retained per-build source snapshots. |

### 2026-07-21

| Build | Evidence | Changes and outcome |
| --- | --- | --- |
| `20260721-105` | Verified | Added scientific-canvas touch marking for Point Mass. The bridge sends Tracker the hover state it requires before a press and supplies Shift only when the selected track needs a new step in the current frame. Existing steps and handles remain normal drags. |
| `20260721-106` | Verified | Made Calibration Stick endpoints visible and draggable by touch. Verified that dragging one endpoint changes its Tracker coordinate while the opposite endpoint remains fixed. |
| `20260721-107` | Verified | Made coordinate-system manipulation touch-ready. Verified dragging the origin and rotating the x-axis handle; the test changed the axis angle from `0` to `1.5707963267948966` radians. |
| `20260721-108` | Verified | Added the recursive mobile command sheet backed directly by Tracker's Java menus. Deep paths can be navigated without hover, including `Track > New > Dynamic Model > Cartesian/Polar/Two-Body System`. Added Back and Close controls and suppressed obsolete Swing popup remnants. A recursive audit found 20 menu containers and 92 enabled executable leaf actions with no missing action binding. |
| `20260721-109` | Verified | Applied the grouped command bar to coarse-pointer devices as well as narrow viewports, covering landscape phones and tablets. Fixed a class collision that could cause the new command bar to be mistaken for Tracker's old toolbar after rotation. |
| `20260721-110` | Verified and current | Current release. Presents `Project`, `New track`, `Measure`, `Axes`, and `More` in the space formerly occupied by the two cramped desktop rows. Keeps all scientific features through progressive disclosure while hiding only redundant desktop chrome on touch devices. Verified Point Mass marking, Calibration Stick dragging, coordinate-origin movement, x-axis rotation, deep menu navigation, portrait/landscape resizing, no document-level horizontal overflow, and zero browser-console errors. Local and deployed HTML, JavaScript, and CSS matched byte-for-byte after upload. |
| `20260721-doc1` | Documentation release | Published this version history and `TrackerStudentMobilePedagogy.html`, an accessible mobile-first article explaining Tracker's evidence-to-model learning cycle and the goal of the touch-first student variant. Added an original responsive hero image and primary-source links to Tracker, Open Source Physics and the Singapore secondary-school performance-task account. The Tracker application assets remain at `20260721-110`. |
| `20260721-111` | Verified and current | Added a compact, safe-area-aware attribution footer beneath the Tracker workspace and wired the application to the site's existing GA4 property (`G-S9EWRY1CPJ`). The footer reserves layout space, so it does not cover the timeline or scientific controls. Added a homepage card linking to both the mobile workspace and its pedagogy article. Live phone QA at 360 × 732 confirmed a 36 px footer, a zero-pixel gap/overlap with Tracker, no horizontal overflow and zero browser-console errors. |
| `20260721-doc2` | Documentation release | Added the lookang attribution link to the pedagogy article footer, updated launch links to build `20260721-111`, and added the same site-wide GA4 property. |

### 2026-07-22

| Build | Evidence | Changes and outcome |
| --- | --- | --- |
| `20260722-112` | Verified and current | Fixed duplicate mobile command bars by retaining one persistent main-toolbar owner and removing stale duplicates on every enhancement pass. Replaced the standalone Axes command with a grouped Views sheet for full-width Video, Plots and graphs, Data table, All panels, and coordinate-axes access. Expanded Tracker's shared Step hit target from 8 × 8 to 24 × 24 pixels in both canonical Java source and the deployed SwingJS core, improving finger acquisition for point masses, calibration sticks, tape measures, axes, protractors, line profiles, and related scientific handles. A live 360 × 732 portrait test acquired and moved a Calibration Stick endpoint from a touch starting 10 pixels above the visible handle. Live portrait and coarse-pointer 844 × 390 landscape tests each showed exactly one command bar, no document-level horizontal overflow, full-width Plot and Table views, and zero console errors. Calibration Points, Tape Measure, Protractor, Circle Fitter, and Offset Origin were also created successfully; Tape, Protractor, and Circle Fitter populated their native plot/table data controls. Added `tools/patch-touch-hit-region.ps1` so the generated-core adjustment can be reapplied deterministically. |
| `20260722-113` | Deployed test build | Corrected false ComPADRE-offline reports caused by Tracker's one-second connectivity timeout: both startup and on-demand tests now allow eight seconds, and a failed startup probe no longer blocks application initialization with an alert. Added `tools/patch-compadre-startup-probe.ps1` so this generated-core correction is deterministic and idempotent. Introduced the first native combo choice sheet. Live testing found that a tap could land on an inner SwingJS wrapper instead of the wrapper holding the Java combo model, so this cache key was superseded before public handoff. |
| `20260722-114` | Deployed test build | Completed the systematic combo-box bridge by walking nested SwingJS wrappers to the element that owns the real Java model. A real mobile tap opened the native choice sheet and exposed both Java track choices. Testing then showed that track choices are stored as `[icon, name]` pairs and the first sheet rendered the internal icon object's string; superseded before public handoff. |
| `20260722-115` | Verified and current | Normalized compound SwingJS combo values so students see clean names such as `mass A` and `mass B` while retaining the direct native Java selection path. Applies the same model-backed touch choice sheet to track, column, plot, and other combo boxes. Retains the resilient ComPADRE checks from build 113 and leaves desktop mouse behavior unchanged. Live Chrome tests created two Point Mass tracks, opened the choice sheet with coordinate-level taps, and changed both Plot and Table selectors in 390 × 844 portrait and 820 × 390 compact landscape. Startup produced no connectivity alert, selected sheets closed cleanly, portrait had one command bar and no horizontal overflow, and the console contained no warnings or errors. Local and deployed HTML, JavaScript, CSS, and generated core matched byte-for-byte. |

## Current mobile design decisions

### Kept

- Tracker's existing scientific model, project format, data, graph, table,
  calibration, coordinate-system, drawing, notes, Library Browser, and export
  capabilities.
- Tracker's original desktop menu bar and toolbar on mouse/desktop devices.
- Native Java actions as the source of truth. Mobile controls invoke those
  actions instead of maintaining a second implementation.

### Grouped on touch devices

- **Project**: open, save, import/export, and other file workflows.
- **New track**: Point Mass and Tracker's other track types.
- **Measure**: Calibration Stick, Calibration Points, Tape Measure,
  Protractor, Circle Fitter, and Offset Origin.
- **Views**: switch Video, Plot, Table, or All Panels to the available width;
  coordinate axes remain available in the same sheet.
- **More**: the complete recursive Tracker menu tree, including advanced and
  context-dependent commands.

### Hidden only on touch devices

- The duplicate fixed desktop menu and crowded icon toolbar are hidden after
  the grouped command bar is ready. Their Java actions remain available.
- The discarded experimental `Tracker Student` header, top Save button, and
  Video/Graph/Table bottom navigation are not rendered.

No scientific feature has been intentionally removed. Items that require an
open video, an active track, or another Tracker state remain disabled until
their native prerequisite is satisfied.

## Files introduced or changed

| File | Purpose |
| --- | --- |
| `../TrackerStudentMobile.html` | Mobile launcher and versioned asset selection. |
| `tracker-student-mobile.js` | Responsive layout, recursive menu sheets, Library Browser bridge, touch marking/dragging, and viewport management. |
| `tracker-student-mobile.css` | Touch targets, grouped controls, popup/dialog layout, toolbar boundaries, Library Browser layout, and responsive rules. |
| `swingjs2-tracker-mobile.js` | Launcher-local SwingJS runtime with the large-long modulo compatibility correction. |
| `compatibility-projects/dollar_drop_activity.trz` | MP4-backed compatibility copy of the legacy dollar-drop project. |
| `../TrackerStudentMobilePedagogy.html` | Public article on Tracker pedagogy and the purpose of the mobile variant. |
| `tracker-mobile-pedagogy-hero.png` | Original article hero showing video, tracked evidence and graph together. |
| `../../tools/patch-html5video-core.ps1` | Reapplies the generated-core HTML5-video description correction. |
| `../../tools/patch-trackerio-cache-paths.ps1` | Reapplies decoded cached TRZ/video path handling. |
| `../../tools/upgrade-gif-trz-to-mp4.ps1` | Converts GIF-backed TRZ video while preserving Tracker analysis data. |
| `../../tools/patch-touch-hit-region.ps1` | Reapplies the shared 24 × 24 scientific-handle hit region to a regenerated SwingJS core. |
| `../../tools/patch-compadre-startup-probe.ps1` | Reapplies the non-blocking eight-second ComPADRE connectivity checks to a regenerated SwingJS core. |
| `../../cdn_cores/tracker6.1.6/core_tracker.z.js` | Deployed Tracker core containing the enlarged shared Step hit target and resilient ComPADRE connectivity checks. |

## Current release fingerprint

Current public URL:

`https://iwant2study.org/tracker/TrackerStudentMobile.html?v=20260722-115`

SHA-256 values for the verified `20260722-115` deployment:

| Asset | SHA-256 |
| --- | --- |
| `TrackerStudentMobile.html` | `6dce12372aac6c74de58fc6067fddd09c0f435b2c216497322ce856ab18dbdb4` |
| `tracker-student-mobile.js` | `4c6ba247e729cb56840d9a3a368f553ecbd3543a40ae8b6e818af0facaf28332` |
| `tracker-student-mobile.css` | `c9bc8b54d9c313b26801d9aaf632a55792d9e1ce7ad70590585a90a4828ea9e3` |
| `swingjs2-tracker-mobile.js` | `b39bc8a2891e2883e0ffcd0be8dc0fba87fa048b3e8de806ec69898d4dca631c` |
| `core_tracker.z.js` | `69dd87c587d9a34dc323f8afc5fab4afa6da68d2927a022dea860a2cb5d93feb` |

## Logging future versions

For every future deployment:

1. Create one new dated build identifier and use it consistently for the HTML,
   CSS, and enhancement JavaScript query strings.
2. Add an entry here before uploading, including the user-visible behavior,
   affected files, regression boundary, and test evidence.
3. Run `node --check` on `tracker-student-mobile.js` and the checklist in
   `TESTING.md` appropriate to the change.
4. Upload only changed files and compare local and live SHA-256 hashes.
5. Keep failed or superseded public builds in this log with their limitations;
   do not silently rewrite their history.
