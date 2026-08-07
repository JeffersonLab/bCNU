# CED modernization

This reactor is the behavior-preserving Maven foundation for the CLAS12 event
display. It currently builds the existing `bCNU`, `bCNU3D`, and `ced` sources
with Java 21; application architecture and behavior have not been changed.
JOGL and GlueGen are resolved by Maven; their platform binaries are no longer
stored in this repository.

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

The CED artifact is written to `ced/target/ced-1.0.0-SNAPSHOT.jar`.

## Run during migration

Run without 3D views:

```bash
mvn -pl ced exec:java -Dexec.args=NO3D
```

Run with 3D views and Maven-managed JOGL 2.6.0:

```bash
mvn -pl ced exec:java -Dexec.args=YES3D
```

The generated CED JAR is currently a thin Maven artifact. Use the Maven launch
command above until application distribution packaging is added.
