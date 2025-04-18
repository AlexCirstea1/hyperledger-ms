### VaultX‑event chain‑code (JavaScript)

```bash
# one‑time: install deps
cd vaultx-event
npm ci

# package+deploy from test‑network root
../test-network/network.sh deployCC \
    -ccn vaultx-event \
    -ccp ../chaincode/vaultx-event \
    -ccl javascript
