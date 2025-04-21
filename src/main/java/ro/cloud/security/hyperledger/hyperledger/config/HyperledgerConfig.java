package ro.cloud.security.hyperledger.hyperledger.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Value;
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
    private String walletPath;                         // e.g. "classpath:fabric/crypto-config/.../msp"
    @Value("${fabric.user-name}")
    private String userName;                           // e.g. "Admin@vaultx.example.com"
    @Value("${fabric.network.config}")
    private String networkConfigPath;                  // e.g. "classpath:fabric/connection-profiles/connection-profile-vaultx.yaml"
    @Value("${fabric.channel-name}")
    private String channelName;
    @Value("${fabric.contract-name}")
    private String contractName;

    private final ResourceLoader resourceLoader;

    @Bean
    public Gateway gateway() throws IOException, CertificateException, InvalidKeyException {
        // 1) In‑memory wallet
        Wallet wallet = Wallets.newInMemoryWallet();

        // 2) Load the X.509 cert
        Resource certRes = resourceLoader.getResource(
                walletPath + "/signcerts/Admin@vaultx.example.com-cert.pem");
        X509Certificate certificate;
        try (BufferedReader certReader = new BufferedReader(
                new InputStreamReader(certRes.getInputStream()))) {
            certificate = Identities.readX509Certificate(certReader);
        }

        // 3) Find the single private‑key file in keystore/
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver(resourceLoader);

        Resource[] keyResources = resolver.getResources(
                walletPath + "/keystore/*");
        if (keyResources.length == 0) {
            throw new IOException("No key file found in " + walletPath + "/keystore");
        }
        Resource keyRes = keyResources[0];
        PrivateKey privateKey;
        try (BufferedReader keyReader = new BufferedReader(
                new InputStreamReader(keyRes.getInputStream()))) {
            privateKey = Identities.readPrivateKey(keyReader);
        }

        // 4) Put identity into wallet
        Identity identity = Identities.newX509Identity("VaultXMSP", certificate, privateKey);
        wallet.put(userName, identity);

        // 5) Copy your inline‑pem connection‑profile YAML to a temp file
        Resource netCfgRes = resourceLoader.getResource(networkConfigPath);
        Path tmpYaml = Files.createTempFile("connection-profile-", ".yaml");
        try (InputStream is = netCfgRes.getInputStream()) {
            Files.copy(is, tmpYaml, StandardCopyOption.REPLACE_EXISTING);
        }

        // 6) Build the Gateway
        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(tmpYaml)    // reads the PEM blobs inline
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
