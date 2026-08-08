#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." >/dev/null && pwd)"
LEGACY_COATJAVA_SOURCE="${COATJAVA_LEGACY_HOME:-$REPO_DIR/../coatjava-13.7.1}"
EXPERIMENTAL_COATJAVA_SOURCE="${COATJAVA_EXPERIMENTAL_HOME:-${COATJAVA_HOME:-$REPO_DIR/../coatjava}}"
COATJAVA_VERSION="13.7.1"
COAT_LIBS_JAR="$REPO_DIR/coatjava/lib/clas/coat-libs-${COATJAVA_VERSION}.jar"
LEGACY_CNUPHYS_POM="$LEGACY_COATJAVA_SOURCE/common-tools/cnuphys/pom.xml"
LEGACY_SWIMMER_POM="$LEGACY_COATJAVA_SOURCE/common-tools/cnuphys/swimmer/pom.xml"
LEGACY_SWIMMER_JAR="$LEGACY_COATJAVA_SOURCE/common-tools/cnuphys/swimmer/target/swimmer-${COATJAVA_VERSION}-SNAPSHOT.jar"
EXPERIMENTAL_POM="$EXPERIMENTAL_COATJAVA_SOURCE/pom.xml"
EXPERIMENTAL_SWIMMER_POM="$EXPERIMENTAL_COATJAVA_SOURCE/common-tools/cnuphys/clas12-swimmer/pom.xml"

if [[ ! -f "$COAT_LIBS_JAR" ]]; then
  echo "Missing bundled coat-libs jar: $COAT_LIBS_JAR" >&2
  exit 1
fi

if [[ ! -f "$LEGACY_CNUPHYS_POM" ]]; then
  echo "Missing coatjava 13.7.1 checkout: $LEGACY_COATJAVA_SOURCE" >&2
  echo "Set COATJAVA_LEGACY_HOME to the coatjava 13.7.1 checkout." >&2
  exit 1
fi

if [[ ! -f "$EXPERIMENTAL_POM" || ! -f "$EXPERIMENTAL_SWIMMER_POM" ]]; then
  echo "Missing experimental clas12-swimmer checkout: $EXPERIMENTAL_COATJAVA_SOURCE" >&2
  echo "Set COATJAVA_EXPERIMENTAL_HOME to the CLAS12Swim-commons-math checkout." >&2
  exit 1
fi

echo "Installing bundled coat-libs ${COATJAVA_VERSION}..."
mvn install:install-file \
  -Dfile="$COAT_LIBS_JAR" \
  -DgroupId=org.jlab.clas \
  -DartifactId=coat-libs \
  -Dversion="$COATJAVA_VERSION" \
  -Dpackaging=jar \
  -DgeneratePom=true

echo "Installing production cnuphys ${COATJAVA_VERSION} dependencies from $LEGACY_COATJAVA_SOURCE..."
if ! mvn -f "$LEGACY_CNUPHYS_POM" install -DskipTests; then
  if [[ ! -f "$LEGACY_SWIMMER_JAR" ]]; then
    echo "The legacy build failed before producing $LEGACY_SWIMMER_JAR" >&2
    exit 1
  fi

  echo "Installing the legacy swimmer JAR produced before dependency analysis failed..."
  mvn install:install-file \
    -Dfile="$LEGACY_SWIMMER_JAR" \
    -DpomFile="$LEGACY_SWIMMER_POM"
fi

echo "Installing the experimental clas12-swimmer from $EXPERIMENTAL_COATJAVA_SOURCE..."
mvn -f "$EXPERIMENTAL_POM" -pl common-tools/cnuphys/clas12-swimmer -am install -DskipTests

echo "CED Maven prerequisites are installed."
