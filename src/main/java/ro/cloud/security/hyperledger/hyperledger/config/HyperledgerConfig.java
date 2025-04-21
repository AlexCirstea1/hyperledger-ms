package ro.cloud.security.hyperledger.hyperledger.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        // Convert classpath resources to actual files
        Resource mspResource = resourceLoader.getResource(walletPath);
        File mspDir = mspResource.getFile();

        // Load certificate
        File certFile = new File(mspDir, "signcerts/Admin@vaultx.example.com-cert.pem");
        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certFile.toPath()));

        // Load private key
        File keyDir = new File(mspDir, "keystore");
        File keyFile = keyDir.listFiles()[0]; // Get the first file in keystore
        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(keyFile.toPath()));

        // Create identity and set up wallet
        Identity identity = Identities.newX509Identity("VaultXMSP", certificate, privateKey);
        wallet.put(userName, identity);

        // Set up gateway
        Resource networkConfigResource = resourceLoader.getResource(networkConfigPath);
        File networkConfigFile = networkConfigResource.getFile();

        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(networkConfigFile.toPath())
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