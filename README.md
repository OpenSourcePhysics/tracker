# Tracker video analysis and modeling

Tracker is an open-source video analysis and modeling tool built on the Open
Source Physics (OSP) framework. It helps learners turn motion in an ordinary
video into measurements, tables, graphs, mathematical models, and evidence for
scientific explanations.

This fork also contains **Tracker Student Mobile**, a touch-first launcher for
the existing SwingJS Tracker application. It keeps Tracker's scientific model
and project formats while making its menus, tools, dialogs, Library Browser,
marking gestures, and view switching usable on phones and tablets.

## Try it

| Resource | Link |
| --- | --- |
| Tracker Student Mobile, current verified build | [Open build 20260722-115](https://iwant2study.org/tracker/TrackerStudentMobile.html?v=20260722-115) |
| Why mobile Tracker matters for learning | [From Motion to Meaning](https://iwant2study.org/tracker/TrackerStudentMobilePedagogy.html?v=20260721-doc2#mobile-mission) |
| Mobile release history and verification evidence | [CHANGELOG.md](site-resources/mobile-enhancements/CHANGELOG.md) |
| Full mobile acceptance checklist | [TESTING.md](site-resources/mobile-enhancements/TESTING.md) |
| Original Tracker website | [opensourcephysics.github.io/tracker-website](https://opensourcephysics.github.io/tracker-website/) |
| Original Tracker Online | [opensourcephysics.github.io/tracker-online](https://opensourcephysics.github.io/tracker-online/) |
| Upstream source repository | [OpenSourcePhysics/tracker](https://github.com/OpenSourcePhysics/tracker) |

The cache-buster in the mobile URL is intentional. It makes phones load the
matching HTML, CSS, enhancement JavaScript, and generated Tracker core rather
than an older cached combination.

## What students can do

Tracker Student Mobile is not a separate physics engine. It exposes the same
Tracker actions through five context-oriented touch controls:

| Control | Purpose |
| --- | --- |
| **Project** | Create, open, save, import, export, print, and access the Digital Library Browser. |
| **New track** | Create Point Mass, Center of Mass, Vector, RGB, line-profile, kinematic, dynamic, external, measuring, and calibration tracks. |
| **Measure** | Reach Calibration Stick, Calibration Points, Tape Measure, Protractor, Circle Fitter, and Offset Origin quickly. |
| **Views** | Show the Video, Plot, Table, or all panels, and show or hide coordinate axes. |
| **More** | Open the complete recursive Tracker menu tree for advanced and less-frequent actions. |

No scientific feature is intentionally removed. Context-dependent items remain
disabled until a video, track, or other required state exists.

### Open a project from an online library

The Digital Library is an **Open** workflow, not an Import workflow:

1. Tap **Project > Open > Library Browser...**.
2. In the Library Browser tap **Collections**.
3. Choose either **ComPADRE Library**, or
   **Shared Library > Singapore Tracker Collection**.
4. Expand folders in the collection tree and select a `.trz` project.
5. Tap **Open** beside the URL field. Double-tap also uses the same load action,
   but select-then-Open is easier on a phone.

Use **Project > Import** when adding a video, Tracker file, or text data to the
current workspace. Use **Project > Open > File...** for a local project.

## Mobile work in this fork

The current implementation addresses limitations found during repeated phone,
tablet, portrait, landscape, and Digital Library testing:

- one persistent, safe-area-aware five-button command bar;
- 48 CSS-pixel menu and choice rows with recursive, tap-first submenus;
- direct Java menu invocation rather than a second implementation of Tracker;
- direct Java combo-model selection for Plot, Table, track, column, and other
  dropdowns, avoiding SwingJS hover-coordinate errors on touch;
- touch marking for Point Mass and touch dragging for calibration tools,
  coordinate axes, and shared Tracker handles;
- a shared 24 x 24 pixel scientific-handle hit region;
- responsive Library Browser menus, URL/search controls, collection tree,
  record selection, preview, Open action, and mobile keyboard handling;
- reliable `.trz` double-click/open behavior and decoded cache paths for names
  containing spaces;
- a launcher-local SwingJS long-arithmetic correction for timeline scrubbing;
- HTML5 video metadata preservation for Library Browser projects;
- a non-blocking ComPADRE startup probe with an eight-second timeout instead of
  a false offline alert after one second;
- responsive secondary windows, plots, tables, dialogs, rotation, and visual
  viewport changes;
- accessible attribution and the site's existing GA4 integration.

The detailed rationale, affected files, historical builds, and test evidence
are maintained in
[site-resources/mobile-enhancements/CHANGELOG.md](site-resources/mobile-enhancements/CHANGELOG.md).

## Architecture

The mobile variant has three deliberate layers:

```text
Tracker Java source
  scientific state, project I/O, actions, views, hit testing
          |
          v
SwingJS-generated Tracker core
  browser execution of Tracker and selected compatibility patches
          |
          v
Tracker Student Mobile launcher
  responsive layout, touch event bridges, command and choice sheets
```

Native Java actions remain the source of truth. The launcher calls them instead
of reimplementing analysis, project I/O, coordinate systems, plots, or tables
in JavaScript.

### Reviewable Java changes

- `Step.java`: enlarges the shared scientific hit region from 8 x 8 to
  24 x 24 pixels.
- `Tracker.java`: exposes stable SwingJS aliases for mobile project naming,
  browser-safe Save As, and primary-view switching.
- `TrackerIO.java`: preserves decoded cached filesystem paths so spaces do not
  become literal `%20` filenames under `/TEMP`.

### Browser launcher and compatibility layer

- `TrackerStudentMobile.html`: versioned application entry point.
- `tracker-student-mobile.js`: command bar, menu/choice sheets, Library Browser
  bridge, touch gestures, responsive windows, and view switching.
- `tracker-student-mobile.css`: touch targets and responsive presentation.
- `swingjs2-tracker-mobile.js`: launcher-local SwingJS long-arithmetic fix.
- `core_tracker.z.js`: generated Tracker core used by the verified deployment.
- `tools/patch-*.ps1`: deterministic post-processing for generated-core fixes.

See [UPSTREAM_INTEGRATION.md](UPSTREAM_INTEGRATION.md) for a change-by-change
review and adoption path for Tracker maintainers.

## Repository map

| Path | Purpose |
| --- | --- |
| `src/org/opensourcephysics/cabrillo/tracker/` | Tracker Java source. |
| `site-resources/TrackerStudentMobile.html` | Touch-first application launcher. |
| `site-resources/TrackerStudentMobilePedagogy.html` | Public pedagogy and project-goal article. |
| `site-resources/mobile-enhancements/` | Mobile CSS/JS, documentation, hero image, compatibility fixture, and isolated SwingJS runtime. |
| `cdn_cores/tracker6.1.6/core_tracker.z.js` | Generated core used by the public mobile build. |
| `tools/patch-*.ps1` | Idempotent or signature-checked generated-core patches. |
| `tools/upgrade-gif-trz-to-mp4.ps1` | Preserves Tracker data while upgrading a legacy GIF-backed TRZ project to MP4. |
| `MOBILE_COMPANION_BOUNDARY.md` | GPL and clean-room boundary for a future independent commercial companion. |
| `UPSTREAM_INTEGRATION.md` | Maintainer-oriented review, build, test, and adoption guide. |

## Build and local test

### Prerequisites

- a JDK compatible with the Tracker branch;
- Apache Ant;
- the OSP Core Library from
  [OpenSourcePhysics/osp](https://github.com/OpenSourcePhysics/osp);
- the SwingJS/libjs resources expected by this repository's existing build;
- optional video-engine support from
  [OpenSourcePhysics/video-engines](https://github.com/OpenSourcePhysics/video-engines).

Without a video engine, desktop Tracker opens images and animated GIFs but may
not open ordinary movie formats. The browser build uses its HTML5 video path.

### Assemble the web site

`build-site.xml` copies `site-resources/**` into the generated `site/` tree and
copies the prepared SwingJS resources into `site/swingjs/j2s`:

```powershell
ant -f build-site.xml tosite

New-Item -ItemType Directory -Force site\swingjs\j2s\core | Out-Null
Copy-Item cdn_cores\tracker6.1.6\core_tracker.z.js `
  site\swingjs\j2s\core\core_tracker.z.js -Force

python -m http.server 8000 --directory site
```

Then open:

```text
http://localhost:8000/TrackerStudentMobile.html?v=local
```

Do not use `file:///` as the final acceptance environment: browser media,
downloads, CORS, service access, and mobile keyboard behavior need an HTTP(S)
origin.

### Reapply generated-core patches

After regenerating the Tracker core, run the patch scripts against that exact
file. Each script checks its expected signature and stops rather than silently
patching an unknown build.

```powershell
powershell -ExecutionPolicy Bypass -File tools\patch-html5video-core.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js

powershell -ExecutionPolicy Bypass -File tools\patch-trackerio-cache-paths.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js

powershell -ExecutionPolicy Bypass -File tools\patch-touch-hit-region.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js

powershell -ExecutionPolicy Bypass -File tools\patch-compadre-startup-probe.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js
```

## Verification

Before publishing a new cache-buster build:

1. Run `node --check` on `tracker-student-mobile.js`.
2. Run every generated-core patch twice; the second run should report that an
   idempotent patch is already applied, or should make no unintended change.
3. Exercise the relevant sections of
   [TESTING.md](site-resources/mobile-enhancements/TESTING.md).
4. Test at least one current iPhone/iPad Safari and Android Chrome device in
   portrait and landscape.
5. Confirm local and deployed SHA-256 hashes for the versioned HTML, CSS,
   JavaScript, and generated core.
6. Record the build and evidence in
   [CHANGELOG.md](site-resources/mobile-enhancements/CHANGELOG.md).

Build `20260722-115` was live-tested with two Point Mass tracks. Plot and Table
track selectors opened through coordinate-level phone taps, selected the real
Java combo model, and closed cleanly in portrait and compact landscape. The
startup produced no ComPADRE alert and the browser console contained no
warnings or errors.

## Deployment

Publish the assembled site so these paths remain siblings:

```text
/TrackerStudentMobile.html
/TrackerStudentMobilePedagogy.html
/mobile-enhancements/**
/swingjs/j2s/core/core_tracker.z.js
/swingjs/j2s/**
/misc-assets.zip
/tracker-assets.zip
```

Use one new dated cache key consistently in the launcher, public links, and
documentation. Upload only changed files, then verify the real HTTPS URLs and
hashes. Do not replace the site's GA4 property when deploying to the current
`iwant2study.org` site; maintainers publishing elsewhere should remove or
replace that site-specific analytics configuration.

## Upstream adoption

The work is structured so maintainers can review it in layers rather than
accepting an opaque mobile rewrite. Start with
[UPSTREAM_INTEGRATION.md](UPSTREAM_INTEGRATION.md), then review the Java source,
launcher layer, deterministic core patches, and verification artifacts
separately.

The future paid companion application described by the project owner is not
included here. This repository remains the GPL Tracker fork. See
[MOBILE_COMPANION_BOUNDARY.md](MOBILE_COMPANION_BOUNDARY.md) for the intended
clean-room interoperability boundary.

## Contributing

- Preserve Tracker's existing project formats and scientific behavior.
- Prefer changes in canonical Java or generator sources over manual edits to a
  generated core; when an upstream dependency prevents that, keep a
  deterministic, signature-checked patch and document the ownership boundary.
- Keep mobile controls as adapters to native Tracker actions.
- Add regression coverage to `TESTING.md` and release evidence to
  `CHANGELOG.md`.
- Keep unrelated application, recording, browser, and deployment artifacts out
  of commits.

## License and credits

Tracker and the enhancements in this repository are distributed under the
existing [GNU General Public License v3](LICENSE).

Tracker is developed by Douglas Brown and the Open Source Physics community.
Tracker Student Mobile is built on that open-source project by
[lookang](https://iwant2study.org/). The mobile work should be treated as a
Tracker enhancement unless and until a separate clean-room companion repository
is created.

Tracker includes classes for Apple events that may require
`AppleJavaExtensions.jar` when compiling the corresponding desktop path.
