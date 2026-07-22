# Upstream integration guide: Tracker Student Mobile

This document is for Tracker maintainers who want to review, pull, or selectively
adopt the touch-first work from `lookang/tracker` into another Tracker branch.
It separates canonical source changes from launcher code, generated artifacts,
and site-specific content so each layer can be evaluated independently.

## Published reference

- Application: <https://iwant2study.org/tracker/TrackerStudentMobile.html?v=20260722-115>
- Pedagogy article: <https://iwant2study.org/tracker/TrackerStudentMobilePedagogy.html?v=20260721-doc2#mobile-mission>
- Fork: <https://github.com/lookang/tracker>
- Upstream: <https://github.com/OpenSourcePhysics/tracker>

The current verified cache key is `20260722-115`. See
`site-resources/mobile-enhancements/CHANGELOG.md` for earlier test builds and
their known limitations.

## Fetch the review branch

From an existing Tracker checkout:

```bash
git remote add lookang https://github.com/lookang/tracker.git
git fetch lookang codex/tracker-student-mobile
git switch -c review/tracker-student-mobile \
  --track lookang/codex/tracker-student-mobile
```

If a `lookang` remote already exists, omit the first command. A maintainer can
also inspect or cherry-pick the commit from the draft pull request in the fork.

## Review map

### 1. Canonical Tracker Java changes

These changes are small and suitable for source-level review:

| File | Change | Reason |
| --- | --- | --- |
| `src/org/opensourcephysics/cabrillo/tracker/Step.java` | Shared hit rectangle grows from 8 x 8 to 24 x 24 pixels. | Fingers and styluses can acquire point, calibration, measuring, and axes handles without changing their visible footprint. |
| `src/org/opensourcephysics/cabrillo/tracker/Tracker.java` | Adds stable `@j2sAlias` methods for project naming, Save As, and primary-view switching. | Keeps the HTML launcher independent of minified/transpiled method names and delegates state changes to Tracker. |
| `src/org/opensourcephysics/cabrillo/tracker/TrackerIO.java` | Uses decoded cache paths for downloaded TRZ/video resources. | Prevents SwingJS from looking for a literal `%20` filename under `/TEMP`. |

The hit-region change affects desktop Tracker as well as the web build. It was
tested with mouse and touch in the mobile workflow, but upstream maintainers
may prefer to make the size conditional on input modality or a preference.

### 2. Mobile launcher layer

These files add a new entry point without changing the existing Tracker HTML
launchers:

```text
site-resources/TrackerStudentMobile.html
site-resources/mobile-enhancements/tracker-student-mobile.css
site-resources/mobile-enhancements/tracker-student-mobile.js
site-resources/mobile-enhancements/swingjs2-tracker-mobile.js
```

The launcher waits for Tracker, then adds responsive presentation and event
adapters. It does not maintain a second physics model. Important bridges call
the real Java menu items, combo models, Tracker panels, and view actions.

The five touch groups are:

- **Project**: the native File menu;
- **New track**: the native `Track > New` tree;
- **Measure**: shortcuts to native measuring/calibration actions;
- **Views**: native Video/Plot/Table/All and axes actions;
- **More**: the complete native menu hierarchy.

### 3. Library Browser and touch behavior

The launcher provides:

- large tap rows for all SwingJS popup levels;
- recursive sheets for hover-only submenus;
- direct Java combo-box selection instead of hover-coordinate selection;
- exact tree-row mapping in the responsive Library Browser;
- a reliable `.trz` double-click path that reuses the native Open action;
- visual-viewport sizing for mobile keyboards and rotation;
- a touch-to-mouse bridge with Shift marking only when the selected track needs
  a new step in the current frame.

The intended Digital Library route is:

```text
Project > Open > Library Browser...
  Collections > ComPADRE Library > ...
  Collections > Shared Library > Singapore Tracker Collection
```

### 4. Generated core and dependency-owned fixes

`cdn_cores/tracker6.1.6/core_tracker.z.js` is included because it is the exact
core deployed with the verified public build. It contains generated forms of
the Java changes plus compatibility fixes whose canonical source belongs to a
different OSP/SwingJS dependency.

Do not treat hand-edited minified JavaScript as the preferred long-term source.
The following scripts make the current deployment reproducible and fail when
their expected generated signature is absent:

| Script | Generated-core correction |
| --- | --- |
| `tools/patch-touch-hit-region.ps1` | Mirrors the `Step.java` 24 x 24 hit rectangle. |
| `tools/patch-trackerio-cache-paths.ps1` | Mirrors decoded `TrackerIO` cache paths. |
| `tools/patch-html5video-core.ps1` | Preserves the `jsvideo` ImageIcon description for File/String HTML5 videos. |
| `tools/patch-compadre-startup-probe.ps1` | Extends one-second ComPADRE checks to eight seconds and makes startup failure non-blocking. |

For upstream integration, move dependency-owned corrections into their
canonical OSP/SwingJS source repositories where possible, regenerate the core,
and use these scripts only to compare the generated result during transition.

### 5. Compatibility fixture

`site-resources/mobile-enhancements/compatibility-projects/dollar_drop_activity.trz`
is a test/publishing copy of the Singapore Collection project whose embedded
legacy GIF was upgraded to H.264 MP4. Its Tracker measurements, coordinate
system, timing, and frame count are preserved.

`tools/upgrade-gif-trz-to-mp4.ps1` performs that narrowly scoped conversion and
requires `ffmpeg`.

### 6. Documentation and public article

- `site-resources/mobile-enhancements/README.md`: implementation and smoke test;
- `site-resources/mobile-enhancements/TESTING.md`: full acceptance checklist;
- `site-resources/mobile-enhancements/CHANGELOG.md`: build-by-build evidence;
- `site-resources/TrackerStudentMobilePedagogy.html`: public-facing learning
  rationale and mobile mission;
- `site-resources/mobile-enhancements/tracker-mobile-pedagogy-hero.png`: original
  responsive article artwork;
- `MOBILE_COMPANION_BOUNDARY.md`: licence and clean-room product boundary.

The public launcher and article contain the `iwant2study.org` GA4 property and
lookang attribution. A different publisher should replace or remove that
site-specific analytics configuration while retaining the Tracker licence and
appropriate credit.

## Suggested adoption sequence

1. Review the three Java source files independently.
2. Build and test ordinary desktop Tracker to establish the regression boundary.
3. Add the new mobile launcher and CSS/JavaScript without replacing existing
   Tracker Online entry points.
4. Regenerate the SwingJS core from canonical sources.
5. Compare and, where still necessary, apply the signature-checked core patches.
6. Run the complete mobile checklist against HTTP(S), not `file:///`.
7. Test iPhone/iPad Safari and Android Chrome in both orientations.
8. Test local files and ComPADRE, Tracker Home, and Shared Library projects.
9. Record the resulting core/launcher hashes and use a new cache key.

## Validation commands

```powershell
node --check site-resources\mobile-enhancements\tracker-student-mobile.js

powershell -ExecutionPolicy Bypass -File tools\patch-touch-hit-region.ps1
powershell -ExecutionPolicy Bypass -File tools\patch-compadre-startup-probe.ps1

powershell -ExecutionPolicy Bypass -File tools\patch-html5video-core.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js

powershell -ExecutionPolicy Bypass -File tools\patch-trackerio-cache-paths.ps1 `
  -CorePath cdn_cores\tracker6.1.6\core_tracker.z.js
```

The first two scripts are idempotent. The latter two are intentionally
signature-checked one-time transformations and should be tested against a
freshly generated core, not repeatedly applied to an already patched file.

## Verified behavior in build 20260722-115

- Tracker initialized without the false ComPADRE offline alert.
- Two Point Mass tracks were created through the mobile command sheet.
- Plot and Table track combos opened from real coordinate-level taps.
- `mass A` and `mass B` invoked the corresponding native Java selection.
- Choice sheets closed after selection.
- Portrait and compact-landscape layouts were exercised.
- Portrait showed one command bar and no document-level horizontal overflow.
- The browser console contained no warnings or errors.
- Deployed HTML, CSS, JavaScript, and generated core matched local SHA-256
  fingerprints.

The full evidence matrix remains in `CHANGELOG.md` and `TESTING.md`; physical
device checks should still be repeated for any newly generated core or browser
release.

## Licence and product boundary

All files in this Tracker repository remain under Tracker's existing GPLv3
licence unless a file states otherwise. A future paid companion may exchange
documented project/data formats, but it should not copy Tracker Java,
transpiled code, UI assets, or implementation details without complying with
their licences. See `MOBILE_COMPANION_BOUNDARY.md`.
