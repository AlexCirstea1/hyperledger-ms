package ro.cloud.security.hyperledger.hyperledger.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


@Configuration
@Data
@RequiredArgsConstructor
public class HyperledgerConfig {

    @Value("${fabric.wallet.path}")
    private String walletPath;

    @Value("${fabric.user-name}")
    private String userName;

    @Value("${fabric.network.config}")
    private String networkConfigPath;

    @Value("${fabric.channel-name}")
    private String channelName;

    @Value("${fabric.contract-name}")
    private String contractName;

    private final ResourceLoader resourceLoader;

    @Bean
    public Gateway gateway() throws IOException, CertificateException, InvalidKeyException {
        // Load wallet and identity
        Wallet wallet = Wallets.newInMemoryWallet();

        // Load resources using input streams instead of files
        Resource mspResource = resourceLoader.getResource(walletPath);

        // Load certificate using input stream
        Resource certResource = resourceLoader.getResource(walletPath + "/signcerts/Admin@vaultx.example.com-cert.pem");
        X509Certificate certificate;
        try (var reader = new BufferedReader(new InputStreamReader(certResource.getInputStream()))) {
            certificate = Identities.readX509Certificate(reader);
        }

        // Find keystore file in resources
        Resource keystoreDir = resourceLoader.getResource(walletPath + "/keystore");
        // We need to find the key file - assuming there's a single key file
        String keyFileName = null;

        // If running from JAR, we'll need to search for the key file
        if (keystoreDir.exists()) {
            // For resource directories in JAR we need a different approach
            // This is a simplified example - you might need to adapt based on your actual resource structure
            Resource[] keyResources = new PathMatchingResourcePatternResolver(resourceLoader)
                    .getResources(walletPath + "/keystore/*");
            if (keyResources.length > 0) {
                // Get the first key file
                keyFileName = keyResources[0].getFilename();
            }
        }

        if (keyFileName == null) {
            throw new IOException("Private key file not found in keystore directory");
        }

        // Load private key using input stream
        Resource keyResource = resourceLoader.getResource(walletPath + "/keystore/" + keyFileName);
        PrivateKey privateKey;
        try (var reader = new BufferedReader(new InputStreamReader(keyResource.getInputStream()))) {
            privateKey = Identities.readPrivateKey(reader);
        }

        // Create identity and set up wallet
        Identity identity = Identities.newX509Identity("VaultXMSP", certificate, privateKey);
        wallet.put(userName, identity);

        // Set up gateway using input stream for network config
        Resource networkConfigResource = resourceLoader.getResource(networkConfigPath);

        // Load connection profile as a YAML file from input stream
        Path tempNetworkConfigPath = Files.createTempFile("connection-profile", ".yaml");
        try (InputStream is = networkConfigResource.getInputStream()) {
            Files.copy(is, tempNetworkConfigPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(tempNetworkConfigPath)
                .discovery(true)
                .connect();
    }

    @Bean
    public Network network(Gateway gateway) {
        return gateway.getNetwork(channelName);
    }

    @Bean
    public Contract eventContract(Network network) {
        return network.getContract(contractName);
    }
}