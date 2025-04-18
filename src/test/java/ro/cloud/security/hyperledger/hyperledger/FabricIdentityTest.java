package ro.cloud.security.hyperledger.hyperledger;

import org.hyperledger.fabric.gateway.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
@Tag("manual")
public class FabricIdentityTest {

    @Value("${fabric.wallet-path}")
    private String walletPath;

    @Value("${fabric.connection-profile-path}")
    private String connectionProfilePath;

    @Test
    void testIdentityConnection() throws Exception {
        // Load wallet
        Path wallet_path = Paths.get(walletPath);
        Wallet wallet = Wallets.newFileSystemWallet(wallet_path);

        // Print available identities
        System.out.println("Available identities in wallet:");
        for (String id : wallet.list()) {
            System.out.println(" - " + id);
            Identity identity = wallet.get(id);
            System.out.println("   MSP ID: " + identity.getMspId());
        }

        // Try explicit connection with discovery off
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, "admin") // Try with admin instead of appUser
                .networkConfig(Paths.get(connectionProfilePath))
                .discovery(false); // Turn off discovery for simplified test

        try (Gateway gateway = builder.connect()) {
            System.out.println("Successfully connected to gateway!");
            Network network = gateway.getNetwork("mychannel");
            System.out.println("Successfully connected to channel!");
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}