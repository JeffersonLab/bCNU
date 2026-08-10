#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
JARNAME="$SCRIPT_DIR/target/ced.jar"
CLAS12_DIR="${CLAS12DIR:-$SCRIPT_DIR/../coatjava}"

echo "CED jar: $JARNAME"
echo "CLAS12DIR used by CED: $CLAS12_DIR"
exec java -Dsun.java2d.pmoffscreen=false -Xmx1024M -Xss512k \
  -DCLAS12DIR="$CLAS12_DIR" -cp "$JARNAME:$SCRIPT_DIR/target/lib/*" \
  cnuphys.ced.frame.Ced YES3D
