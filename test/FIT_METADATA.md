# Fit report metadata adapter

This companion branch requires the OSP changes in OpenSourcePhysics/osp PR #9.
It connects Tracker's existing unit and ImageCoordSystem calibration metadata to
Data Tool through FitMetadataProvider. It does not change tracking or fitting.

The adapter follows source dataset ID and source column ID through Data Tool
column names. Pixel uncertainty is available only for direct PointMass x/y with
known length units and a constant conversion. It uses the norm of the relevant
image-to-world transform row, accounting for rotation and anisotropic scale.
A varying scale, or varying rotation with anisotropic scale, is unavailable.
Derived data quantities never inherit a position conversion.

The data-table copy path uses metadata even when displayed units are hidden.
Project serialization is unchanged; the provider is attached when Tracker opens
Data Tool from a plot or table. Generic OSP callers can continue without a provider.

Build against the new OSP classes, then compile and run
`test/org/opensourcephysics/cabrillo/tracker/FitDataMetadataTest.java` with the
built Tracker/OSP classpath on a graphical desktop. The fixture contains 17 checks
of fractional/custom pixel values, physical conversion, rotation, and exclusions.
The four changed Tracker source files also pass SwingJS transpilation; the actual
Tracker calibration fixture was run on macOS, not inside a browser.

The optional position-component callback identifies x(t) and y(t) through the same source-column metadata. OSP uses it to label motion results in copied fit reports. Position-versus-position and velocity-versus-time pairs are excluded; no derivative uncertainty propagation or tracking algorithm changes are introduced.
