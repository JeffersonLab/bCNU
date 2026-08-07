# CED modernization

This reactor is the behavior-preserving Maven foundation for the CLAS12 event
display. It currently builds the existing `bCNU`, `bCNU3D`, and `ced` sources
with Java 21; application architecture and behavior have not been changed.
JOGL and GlueGen are resolved by Maven; their platform binaries are no longer
stored in this repository.

Maven is the authoritative build system for CED. The former CED Ant scripts
were removed because they depended on deleted binary projects and historical
Eclipse output directories. The reactor modules no longer carry Ant build
files or nested Eclipse project metadata; import the root Maven reactor into
an IDE instead.

Geometry startup uses the authoritative CCDB initializers directly. The legacy
Kryo cache and its checked-in binary artifacts have been removed pending a
maintainable SQLite replacement.

## Prerequisite

Build CED with Java 21 and Maven 3.9 or newer. The Maven validation phase
checks both versions and stops with a clear error before compilation when the
wrong toolchain is active.

The coatjava aggregate JAR is intentionally not published to Maven Central.
The cnuphys libraries also come from the coatjava source checkout. Install all
of these prerequisites before the first build with:

```bash
./scripts/bootstrap-maven-dependencies.sh
```

The script expects the coatjava source checkout beside this repository. Set
`COATJAVA_HOME` when it is elsewhere:

```bash
COATJAVA_HOME=/path/to/coatjava ./scripts/bootstrap-maven-dependencies.sh
```

It also handles the current swimmer dependency-analysis failure by installing
the successfully produced swimmer artifact directly. This bootstrap will
disappear as the legacy libraries are replaced by MDI.

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
