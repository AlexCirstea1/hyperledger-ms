#!/usr/bin/env bash
set -e

# Unpack the Minifab cert bundle into the organizations tree
mkdir -p /opt/fabric/organizations
tar -xzf /opt/fabric/fabric-mini/vars/certs.tgz \
    --strip-components=1 \
    -C /opt/fabric/organizations \
    keyfiles

 mkdir -p /opt/fabric/profiles
 cp -R /opt/fabric/fabric-mini/vars/profiles/* /opt/fabric/profiles/

# Launch the Spring Boot app
exec java -jar /app/app.jar
