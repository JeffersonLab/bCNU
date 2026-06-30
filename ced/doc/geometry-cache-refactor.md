# Geometry Cache Refactor Notes

## Current situation

CED geometry is initialized from CCDB/JLab geometry services and cached using Kryo.
The cache currently serializes relatively complex Java object graphs, including detector
geometry objects, arrays, transformations, and collection internals. This makes the cache
difficult to maintain because changes in implementation classes can require changes in
Kryo registration.

## Goal

Replace object-graph serialization with an explicit, stable cache format.

The new cache should store only the geometry facts CED needs at runtime:
coordinates, detector indices, transformations, strip/paddle/wire endpoints, and other
primitive or simple data values.

## Proposed direction

Introduce a geometry cache abstraction that is independent of Kryo. During transition,
the existing Kryo cache can remain available while individual detector geometries are
migrated to a new explicit cache representation, possibly backed by SQLite.

## Migration strategy

1. Introduce a cache abstraction that does not expose Kryo in the public interface.
2. Choose one simple detector geometry as a pilot.
3. Define explicit DTO/cache records for that detector.
4. Read from CCDB when the cache is absent or stale.
5. Write the simplified cache format.
6. Rebuild CED runtime geometry structures from the simplified cache.
7. Repeat detector by detector.

## Non-goals for first pass

- Do not convert the whole project to Maven at the same time.
- Do not replace all local project dependencies at once.
- Do not migrate all detectors in one commit.
- Do not remove Kryo until at least one detector has been converted and validated.