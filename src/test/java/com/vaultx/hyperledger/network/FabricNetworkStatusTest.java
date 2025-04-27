package com.vaultx.hyperledger.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("manual")
public class FabricNetworkStatusTest {

    @Test
    void testDockerContainersRunning() throws Exception {
        // Execute docker command to check running containers
        Process process = Runtime.getRuntime().exec("docker ps --format '{{.Names}}'");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        boolean peer0Org1Running = false;
        boolean peer0Org2Running = false;
        boolean ordererRunning = false;

        while ((line = reader.readLine()) != null) {
            if (line.contains("peer0.org1")) peer0Org1Running = true;
            if (line.contains("peer0.org2")) peer0Org2Running = true;
            if (line.contains("orderer")) ordererRunning = true;
        }

        assertTrue(peer0Org1Running, "peer0.org1 container is not running");
        assertTrue(peer0Org2Running, "peer0.org2 container is not running");
        assertTrue(ordererRunning, "orderer container is not running");
    }
}
