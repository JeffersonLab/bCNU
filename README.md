# CED modernization

This reactor is the Java 21 and Maven foundation for the CLAS12 event display.
It builds the existing `bCNU`, `bCNU3D`, and `ced` sources while the user
interface is migrated incrementally toward MDI. JOGL and GlueGen are resolved
by Maven; their platform binaries are no longer stored in this repository.

Detector event data is read through typed, detector-specific accessors in
`cnuphys.ced.alldata`. Drawing code no longer copies banks into parallel data
containers or queries the current `DataEvent` to determine bank availability.
The event pipeline publishes a coherent event snapshot before repainting the
views, which prevents mixed old/new trajectory frames during event changes.

Maven is the authoritative build system for CED. The former CED Ant scripts
were removed because they depended on deleted binary projects and historical
Eclipse output directories. The reactor modules no longer carry Ant build
files or nested Eclipse project metadata; import the root Maven reactor into
an IDE instead.

Geometry startup uses a per-user SQLite cache at
`~/.ced/geometry-cache.sqlite`. Detector payloads use explicit primitive data
rather than serialized Java object graphs. CED automatically recreates the
cache when the CED version or requested geometry variation changes; it can
also be removed manually with **Options > Delete Geometry Cache**. Every
geometry registered by CED has an explicit cache representation; a cache miss
initializes that detector from its authoritative source and stores the
primitive payload.

## Migration status

Completed foundations include:

- Java 21 Maven reactor, tests, packaging, and CI;
- Maven-managed native and Java dependencies;
- typed detector-bank access without the former data-container hierarchy;
- phase-ordered event notification and atomic trajectory publication;
- isolated experimental Commons Math swimmer integration; and
- versioned SQLite geometry caching for all registered detectors.

The principal remaining architectural step is replacing the legacy `bCNU`,
`bCNU3D`, and sPlot UI layers with MDI. Until that migration is complete, the
existing view classes remain the production presentation layer and should be
changed in small, behavior-preserving increments.

## Prerequisite

Build CED with Java 21 and Maven 3.9 or newer. The Maven validation phase
checks both versions and stops with a clear error before compilation when the
wrong toolchain is active.

The official coatjava aggregate JAR is intentionally not published to Maven
Central. This repository bundles `coat-libs-14.1.2.jar`, including the
production cnuphys libraries. Install it and the experimental Commons Math
swimmer before the first build with:

```bash
./scripts/bootstrap-maven-dependencies.sh
```

The script expects the `CLAS12Swim-commons-math` coatjava checkout beside this
repository. That branch supplies only the additive experimental
`cnuphys:clas12-swimmer` artifact; the production cnuphys classes come from the
bundled official aggregate JAR:

```bash
./scripts/bootstrap-maven-dependencies.sh
```

Override the experimental checkout location when necessary:

```bash
COATJAVA_EXPERIMENTAL_HOME=/path/to/coatjava-experimental \
./scripts/bootstrap-maven-dependencies.sh
```

The experimental artifact is additive and its transitive production swimmer
and magnetic-field dependencies are excluded so they do not duplicate classes
already present in `coat-libs`.

## Build

From this repository's root:

```bash
mvn clean install
```

Run the characterization tests with:

```bash
mvn test
```

The self-contained CED distribution is written to `ced/target`: the application
`ced.jar` and its Maven-managed dependencies in `lib/`. Keep those together
and use the launch scripts below when copying or distributing the application.

## Run during migration

After packaging, run without 3D views:

```bash
./ced/cedNO3D.sh
```

Run with 3D views and Maven-managed JOGL 2.6.0:

```bash
./ced/ced.sh
```

For development, the existing Maven launch form remains available:

```bash
mvn -pl ced exec:java -Dexec.args=NO3D
```
