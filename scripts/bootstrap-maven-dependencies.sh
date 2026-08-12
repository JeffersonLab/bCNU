#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." >/dev/null && pwd)"
EXPERIMENTAL_COATJAVA_SOURCE="${COATJAVA_EXPERIMENTAL_HOME:-${COATJAVA_HOME:-$REPO_DIR/../coatjava}}"
COATJAVA_VERSION="14.1.2"
COAT_LIBS_JAR="$REPO_DIR/coatjava/lib/clas/coat-libs-${COATJAVA_VERSION}.jar"
EXPERIMENTAL_POM="$EXPERIMENTAL_COATJAVA_SOURCE/pom.xml"
EXPERIMENTAL_SWIMMER_POM="$EXPERIMENTAL_COATJAVA_SOURCE/common-tools/cnuphys/clas12-swimmer/pom.xml"

if [[ ! -f "$COAT_LIBS_JAR" ]]; then
  echo "Missing bundled coat-libs jar: $COAT_LIBS_JAR" >&2
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
  -DgroupId=org.jlab.coat \
  -DartifactId=coat-libs \
  -Dversion="$COATJAVA_VERSION" \
  -Dpackaging=jar \
  -DgeneratePom=true

echo "Installing the experimental clas12-swimmer from $EXPERIMENTAL_COATJAVA_SOURCE..."
mvn -f "$EXPERIMENTAL_POM" -pl common-tools/cnuphys/clas12-swimmer -am install -DskipTests

echo "CED Maven prerequisites are installed."
