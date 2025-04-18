#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"/..
export PATH="$ROOT_DIR/fabric/bin:$PATH"           # binaries you already downloaded

NETWORK_DIR="$ROOT_DIR/fabric/vaultx-network"
CC_DIR="$ROOT_DIR/chaincode/vaultx-event"

pushd "$NETWORK_DIR" > /dev/null

case "${1:-}" in
  up)
      ./network.sh up createChannel -ca -c mychannel -s couchdb
      ;;
  down)
      ./network.sh down
      ;;
  deploy)
      ./network.sh deployCC \
         -ccn vaultx-event \
         -ccp "$CC_DIR" \
         -ccl javascript
      ;;
  *)
      echo "Usage: $0 {up|deploy|down}"
      exit 1
      ;;
esac

popd > /dev/null
