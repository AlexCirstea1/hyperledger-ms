#!/usr/bin/env bash
set -e

mkdir -p /opt/fabric/organizations
tar -xzf /opt/fabric/vars/certs.tgz \
    --strip-components=1 \
    -C /opt/fabric/organizations \
    keyfiles

mv /opt/fabric/vars/profiles /opt/fabric/profiles

exec java -jar /app/app.jar
