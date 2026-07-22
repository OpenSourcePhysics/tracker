# Tracker enhancements and independent mobile companion

This repository remains the GPLv3 Tracker project. All changes committed here,
including `TrackerStudentMobile.html` and the Java bridge, are Tracker
enhancements and should be released under Tracker's existing licence.

The proposed paid mobile video-analysis companion should be developed in a
separate repository with a clean implementation boundary.

## The companion may interoperate through

- Documented TRK/TRZ import and export adapters
- CSV data exchange
- Ordinary video and image formats
- Publicly documented coordinate transforms and numerical methods
- Test fixtures whose redistribution terms are compatible

## The companion should not copy

- Tracker Java source
- Transpiled Tracker or SwingJS code
- Tracker UI classes, icons, help content, or internal resources without an
  appropriate licence
- Tracker implementation details where an independently specified interface is
  sufficient

## Recommended separation

```text
OpenSourcePhysics/tracker fork
  GPL Tracker fixes and interoperability validation

independent mobile companion repository
  original TypeScript application and analysis core
  internal JSON project model
  clean-room TRK/TRZ adapter
  compatibility tests against exported Tracker projects
```

Charging for GPL software is permitted, but keeping the companion independent
preserves freedom to choose its commercial and source-licensing model. Obtain
specific legal advice before distributing the companion if Tracker code or
assets are later considered for reuse.

## Related material

- Tracker Student Mobile application:
  <https://iwant2study.org/tracker/TrackerStudentMobile.html?v=20260722-115>
- Pedagogy and mobile mission:
  <https://iwant2study.org/tracker/TrackerStudentMobilePedagogy.html?v=20260721-doc2#mobile-mission>
- Maintainer review and adoption guide: `UPSTREAM_INTEGRATION.md`
- Mobile implementation details:
  `site-resources/mobile-enhancements/README.md`

The independent companion itself is intentionally not present in this
repository. This repository contains only GPL Tracker enhancements,
interoperability guidance, and compatibility fixtures.
