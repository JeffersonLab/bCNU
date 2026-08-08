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

CED currently keeps its established coatjava 13.7.1-compatible swimmer while
loading the experimental Commons Math swimmer from a separate Maven artifact.
The script therefore expects two coatjava checkouts beside this repository.
Pin the legacy worktree to the last CED-compatible baseline; the upstream
13.7.1 tag does not contain the trajectory-drawing API used by this CED tree:

```bash
git -C ../coatjava worktree add --detach ../coatjava-13.7.1 7a073f9a4
./scripts/bootstrap-maven-dependencies.sh
```

By default, `../coatjava-13.7.1` supplies the production cnuphys artifacts and
`../coatjava` supplies `cnuphys:clas12-swimmer` from the
`CLAS12Swim-commons-math` branch. Override those locations when necessary:

```bash
COATJAVA_LEGACY_HOME=/path/to/coatjava-13.7.1 \
COATJAVA_EXPERIMENTAL_HOME=/path/to/coatjava-experimental \
./scripts/bootstrap-maven-dependencies.sh
```

The experimental artifact is additive; it does not replace the production
`cnuphys:swimmer` dependency. This bootstrap will disappear as the legacy
libraries are replaced by MDI.

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
