package com.vaultx.hyperledger.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric.sdk.helper.Config;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@Data
@RequiredArgsConstructor
public class HyperledgerConfig {

    // CA properties
    @Value("${fabric.ca.url}")
    private String caUrl;

    @Value("${fabric.ca.admin.name}")
    private String caAdmin;

    @Value("${fabric.ca.admin.secret}")
    private String caAdminSecret;

    @Value("${fabric.ca.tlsCert.path}")
    private String caTlsCertPath;

    @Value("${fabric.ca.allowAllHostNames}")
    private boolean allowAllHostNames;

    // Wallet identity + MSP
    @Value("${fabric.msp.id}")
    private String mspId;

    @Value("${fabric.user-name}")
    private String userName;

    // Network YAML
    @Value("${fabric.network.config}")
    private String networkConfigPath;

    // Channel / Contract
    @Value("${fabric.channel-name}")
    private String channelName;

    @Value("${fabric.contract-name}")
    private String contractName;

    private final ResourceLoader resourceLoader;

    @Bean
    public Gateway gateway() throws Exception {
        // 1) Prepare the CA client
        Resource caCertRes = resourceLoader.getResource(caTlsCertPath);
        Path tmpCaCert = Files.createTempFile("ca-cert-", ".pem");
        try (InputStream is = caCertRes.getInputStream()) {
            Files.copy(is, tmpCaCert, StandardCopyOption.REPLACE_EXISTING);
        }

        Properties caProps = new Properties();
        caProps.put("pemFile", tmpCaCert.toString());
        caProps.put("allowAllHostNames", Boolean.toString(allowAllHostNames));

        HFCAClient caClient = HFCAClient.createNewInstance(caUrl, caProps);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // 2) Enroll the admin user
        Enrollment enrollment = caClient.enroll(caAdmin, caAdminSecret);

        // 3) Create wallet identity
        Wallet wallet = Wallets.newInMemoryWallet();

        // Convert certificate string to X509Certificate
        String certPEM = enrollment.getCert();
        ByteArrayInputStream certInputStream = new ByteArrayInputStream(certPEM.getBytes(StandardCharsets.UTF_8));
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(certInputStream);

        Identity adminIdentity = Identities.newX509Identity(mspId,
                x509Certificate,
                enrollment.getKey());
        wallet.put(userName, adminIdentity);

        // 4) Copy connection-profile YAML (with inline PEMs) to a temp file
        Resource netCfgRes = resourceLoader.getResource(networkConfigPath);
        Path tmpYaml = Files.createTempFile("conn-profile-", ".yaml");
        try (InputStream is = netCfgRes.getInputStream()) {
            Files.copy(is, tmpYaml, StandardCopyOption.REPLACE_EXISTING);
        }

        // 5) Build and return the Gateway
        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(tmpYaml)
                .discovery(false)
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
