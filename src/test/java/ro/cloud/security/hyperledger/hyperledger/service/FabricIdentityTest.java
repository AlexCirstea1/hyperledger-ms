package ro.cloud.security.hyperledger.hyperledger.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;
import org.hyperledger.fabric.gateway.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("manual")
public class FabricIdentityTest {

    @Value("${fabric.wallet.path}")
    private String walletPath;

    @Value("${fabric.network.config}")
    private String connectionProfilePath;

    @Value("${fabric.user-name}")
    private String userName;

    @Value("${fabric.channel-name}")
    private String channelName;

    @Test
    void testIdentityConnection() throws Exception {
        // Path to the user's MSP directory
        Path userMspPath = Paths.get(walletPath, "msp");

        // Create a temporary wallet for testing
        Path walletDir = Paths.get("target/test-wallet");
        Files.createDirectories(walletDir);

        System.out.println("User MSP path: " + userMspPath);
        System.out.println("Temporary wallet path: " + walletDir);

        // Create wallet
        Wallet wallet = Wallets.newFileSystemWallet(walletDir);

        // Check if we need to import the identity
        if (!wallet.list().contains(userName)) {
            System.out.println("Importing identity to wallet...");

            // Read certificate
            Path certPath = userMspPath.resolve("signcerts").resolve("cert.pem");
            X509Certificate cert = Identities.readX509Certificate(Files.newBufferedReader(certPath));

            // Read private key (first file in keystore directory)
            Path keyPath = null;
            try (Stream<Path> keyFiles = Files.list(userMspPath.resolve("keystore"))) {
                keyPath = keyFiles.findFirst()
                        .orElseThrow(() -> new RuntimeException("No key files found in keystore directory"));
            }
            PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(keyPath));

            // Create identity and add to wallet
            Identity identity = Identities.newX509Identity("Org1MSP", cert, privateKey);
            wallet.put(userName, identity);
            System.out.println("Identity imported successfully");
        }

        // Print available identities
        System.out.println("Available identities in wallet:");
        for (String id : wallet.list()) {
            System.out.println(" - " + id);
        }

        // Connect using the wallet with imported identity
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(Paths.get(connectionProfilePath))
                .discovery(false);

        try (Gateway gateway = builder.connect()) {
            System.out.println("Connected to gateway");
            Network network = gateway.getNetwork(channelName);
            System.out.println("Connected to channel");
        }
    }
}
