# Mobile enhancement acceptance checklist

## Required devices

- Current iPadOS Safari, portrait and landscape
- Current Android Chrome, portrait and landscape
- Desktop Chrome or Edge as a regression check

## Project flow

- [ ] Tracker loads without a blank frame or JavaScript error.
- [ ] A slow or failed ComPADRE startup probe never displays a blocking alert
      and does not prevent the mobile command bar from initializing.
- [ ] The ComPADRE Identify endpoint can take longer than one second without
      being reported as offline; retry and on-demand access remain available.
- [ ] A local MP4, TRK, and TRZ can be opened using existing Tracker controls.
- [ ] File, Edit, Video, Track, Coordinate System, View, and Help open reliably.
- [ ] Tapping `Track > New` opens the submenu without a hover gesture.
- [ ] Tapping `Calibration Tools` reveals complete, unclipped item labels.
- [ ] Existing Tracker toolbar and save actions remain available.
- [ ] Audit every main-toolbar action: Open, Save, Clip Settings, Coordinate
      Tools, Calibration Tools, Measure, Track, Autotracker, Display, Zoom,
      Drawing, Notes, Refresh, and Maximize must invoke its matching native
      menu, dialog, or state change rather than an adjacent control.
- [ ] At phone width, horizontally scroll to Display, Zoom, and the compact
      overflow control; verify their actions still match the selected icon.
- [ ] The phone overflow menu exposes Drawings, Support Documents, Notes,
      Refresh (including Refresh Now and Auto-refresh), and Maximize.
- [ ] Rapidly dragging the video timeline between early and late frames at
      least 20 times updates the frame readout without a JavaScript alert.
- [ ] Timeline scrubbing does not report `Long.div is not a function` in a
      dialog or the browser console.
- [ ] In `Collections > Shared Library > Singapore Tracker Collection`, select
      `01_measurement > trz > dollar_drop_activity.trz` and click `Open`.
- [ ] The dollar-drop video element reaches a playable state and Tracker loads
      its `autotracked with cg compensation` track without a null `dialog`
      exception.
- [ ] Double-clicking `BallTossOut.trz` invokes the same load workflow as the
      Library Browser Open button.
- [ ] Double-clicking `GB Droplookang.trz` loads the project without attempting
      to open a literal `/TEMP/.../GB%20Droplookang.trz` filename.
- [ ] The downloaded project reopens with video metadata, coordinates, and
      point-mass data intact.

## Responsive behavior

- [ ] No document-level horizontal scrolling at 390 CSS px.
- [ ] No extra application header or bottom navigation is rendered.
- [ ] Rotation preserves a usable Tracker frame size.
- [ ] Popup rows are at least 48 CSS px high.
- [ ] Main-toolbar icons are visually enlarged without overlapping adjacent
      controls; their wrappers remain inside the native 34 px toolbar row.
- [ ] Every toolbar action has a visible button boundary, and vertical dividers
      clearly separate Tracker's native toolbar groups.
- [ ] At widths up to 820 CSS px, the full toolbar forms one horizontally
      scrollable row instead of stacking or clipping the right-side tools.
- [ ] Nested menus stay within the viewport and can scroll vertically.
- [ ] `Open Library Browser...` displays the Library Browser welcome page rather
      than an empty white panel.
- [ ] The Library Browser `Collections` menu includes ComPADRE, Tracker Home and
      Shared Library entries, and selecting a collection displays its contents.
- [ ] `Collections > Shared Library` displays an unclipped child menu, and a
      child such as `Singapore Tracker Collection` can be selected and loaded.
- [ ] Long Shared Library choices such as `High Point University Collection`
      are fully readable rather than cut off by the child-menu edge.
- [ ] On a compact/coarse touch viewport, every SwingJS combo box opens the
      same large native choice sheet with rows at least 48 CSS px high.
- [ ] Selecting another item in the Plot track, Table track/column, and other
      combo sheets changes the underlying Java selection and closes the sheet.
- [ ] Desktop mouse combo boxes continue to use their original SwingJS popup.

## Accessibility

- [ ] Keyboard focus is visible.
- [ ] The workflow remains usable at 200% browser zoom.

## Regression boundaries

- [ ] `TrackerMobile.html`, `TrackerCore.html`, and desktop Java launch paths are
      unchanged.
- [ ] The long-division compatibility change is loaded only by
      `TrackerStudentMobile.html`; other Tracker pages still use the shared
      `/swingjs/swingjs2.js` runtime.
- [ ] Calling `TrackerIO.save(null, panel)` still uses the original chooser.
- [ ] Desktop mouse-hover submenu behavior remains available.
