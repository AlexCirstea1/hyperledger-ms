#!/usr/bin/env bash
set -euo pipefail

# 1) Find the mounted Vars folder
# Try /opt/fabric/vars first, otherwise /opt/fabric/fabric-mini/vars
for BASE in /opt/fabric/vars /opt/fabric/fabric-mini/vars; do
  if [ -d "$BASE" ]; then
    VARS_DIR="$BASE"
    break
  fi
done

if [ -z "${VARS_DIR:-}" ]; then
  echo "ERROR: Cannot locate the Minifab vars folder" >&2
  exit 1
fi

# 2) Discover the certs archive
CERT_ARCHIVE=$(find "$VARS_DIR" -maxdepth 1 -type f -name '*.tgz' | head -n1)
if [ ! -f "$CERT_ARCHIVE" ]; then
  echo "ERROR: No *.tgz found in $VARS_DIR" >&2
  ls -l "$VARS_DIR"
  exit 1
fi

echo "Using cert archive: $CERT_ARCHIVE"

# 3) Unpack into /opt/fabric/organizations
mkdir -p /opt/fabric/organizations
tar -xzf "$CERT_ARCHIVE" \
    --strip-components=1 \
    -C /opt/fabric/organizations \
    keyfiles

# 4) (Optional) Expose the connection profiles directly
#   You can either copy or just point your app at $VARS_DIR/profiles
# mkdir -p /opt/fabric/profiles
# cp -R "$VARS_DIR/profiles"/* /opt/fabric/profiles/

# 5) Finally, launch your Spring Boot JAR
exec java -jar /app/app.jar
