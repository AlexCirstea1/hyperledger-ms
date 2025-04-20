#!/usr/bin/env bash
set -euo pipefail

# 1) Look for the mounted vars folder
for BASE in /opt/fabric/vars /opt/fabric/fabric-mini/vars; do
  if [ -d "$BASE" ]; then
    VARS_DIR="$BASE"
    break
  fi
done

if [ -z "${VARS_DIR:-}" ]; then
  echo "❌ ERROR: Cannot locate the Minifab vars folder." >&2
  exit 1
fi

# 2) Mount the wallet/MSP folder (keyfiles → /opt/fabric/organizations)
if [ -d "$VARS_DIR/keyfiles" ]; then
  echo "🔐 Copying MSP wallet (keyfiles/) to /opt/fabric/organizations…"
  rm -rf /opt/fabric/organizations
  mkdir -p /opt/fabric/organizations
  cp -r "$VARS_DIR/keyfiles/"* /opt/fabric/organizations/
else
  echo "❌ ERROR: Missing keyfiles/ folder inside $VARS_DIR" >&2
  exit 1
fi

# 3) Mount connection profiles
if [ -d "$VARS_DIR/profiles" ]; then
  echo "📡 Copying connection profiles to /opt/fabric/profiles…"
  rm -rf /opt/fabric/profiles
  mkdir -p /opt/fabric/profiles
  cp -r "$VARS_DIR/profiles/"* /opt/fabric/profiles/
else
  echo "❌ ERROR: Missing profiles/ folder inside $VARS_DIR" >&2
  exit 1
fi

# 4) Launch your app
echo "🚀 Launching Spring Boot app..."
exec java -jar /app/app.jar
