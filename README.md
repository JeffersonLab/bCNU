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

The coatjava aggregate JAR is intentionally not published to Maven Central.
Install the repository's copy under the coordinates used by CED before the
first build:

```bash
mvn install:install-file \
  -Dfile=coatjava/lib/clas/coat-libs-13.7.1.jar \
  -DgroupId=org.jlab.clas \
  -DartifactId=coat-libs \
  -Dversion=13.7.1 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

The current code also uses the cnuphys libraries built by the adjacent
coatjava checkout. Install those artifacts before the first build. This
prerequisite will disappear as the legacy libraries are replaced by MDI.

```bash
cd /Users/davidheddle/coatjava/common-tools/cnuphys
mvn install -DskipTests
```

At present, coatjava's `swimmer` module compiles but its strict dependency
analysis reports an existing POM error. Until that POM is corrected, install
the resulting JAR directly if the command above stops at `swimmer`:

```bash
mvn install:install-file \
  -Dfile=swimmer/target/swimmer-13.7.1-SNAPSHOT.jar \
  -DpomFile=swimmer/pom.xml
```

## Build

From this repository's root:

```bash
mvn clean install -DskipTests
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
