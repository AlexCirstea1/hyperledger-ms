package ro.cloud.security.hyperledger.hyperledger.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
public class NetworkConnectivityTest {

    @Test
    void testPeerHostResolution() throws Exception {
        // Test if hosts can be resolved
        assertTrue(InetAddress.getByName("peer0.org1.vaultx.com").isReachable(1000),
                "peer0.org1.vaultx.com is not reachable");

        assertTrue(InetAddress.getByName("peer0.org2.vaultx.com").isReachable(1000),
                "peer0.org2.vaultx.com is not reachable");
    }

    @Test
    void testPeerPortConnectivity() {
        // Test if ports are open
        assertTrue(isPortOpen("peer0.org1.vaultx.com", 7051),
                "Cannot connect to peer0.org1.vaultx.com:7051");

        assertTrue(isPortOpen("peer0.org2.vaultx.com", 9051),
                "Cannot connect to peer0.org2.vaultx.com:9051");
    }

    private boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}