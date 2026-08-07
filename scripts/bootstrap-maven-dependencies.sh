#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." >/dev/null && pwd)"
COATJAVA_SOURCE="${COATJAVA_HOME:-$REPO_DIR/../coatjava}"
COATJAVA_VERSION="13.7.1"
CNUPHYS_VERSION="${COATJAVA_VERSION}-SNAPSHOT"
COAT_LIBS_JAR="$REPO_DIR/coatjava/lib/clas/coat-libs-${COATJAVA_VERSION}.jar"
CNUPHYS_DIR="$COATJAVA_SOURCE/common-tools/cnuphys"
SWIMMER_JAR="$CNUPHYS_DIR/swimmer/target/swimmer-${CNUPHYS_VERSION}.jar"

if [[ ! -f "$COAT_LIBS_JAR" ]]; then
  echo "Missing bundled coat-libs jar: $COAT_LIBS_JAR" >&2
  exit 1
fi

if [[ ! -f "$CNUPHYS_DIR/pom.xml" ]]; then
  echo "Missing coatjava cnuphys checkout: $CNUPHYS_DIR" >&2
  echo "Set COATJAVA_HOME to the coatjava source checkout." >&2
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

echo "Installing cnuphys dependencies from $CNUPHYS_DIR..."
if ! mvn -f "$CNUPHYS_DIR/pom.xml" install -DskipTests; then
  if [[ ! -f "$SWIMMER_JAR" ]]; then
    echo "The cnuphys build failed before producing $SWIMMER_JAR" >&2
    exit 1
  fi

  echo "Installing the swimmer artifact produced before dependency analysis failed..."
  mvn install:install-file \
    -Dfile="$SWIMMER_JAR" \
    -DpomFile="$CNUPHYS_DIR/swimmer/pom.xml"
fi

echo "CED Maven prerequisites are installed."
