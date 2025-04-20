#!/usr/bin/env bash
set -e

# 1) Unpack the Minifab cert bundle into the organizations/ layout
mkdir -p /opt/fabric/organizations
tar -xzf /opt/fabric/certs.tgz \
    --strip-components=1 \
    -C /opt/fabric/organizations \
    keyfiles

# 2) Launch your Spring Boot JAR
exec java -jar /app/app.jar
