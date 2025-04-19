package ro.cloud.security.hyperledger.hyperledger.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.Data;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class HyperledgerConfig {

    @Value("${fabric.wallet.path}")
    private String walletPath; // e.g. .../users/appUser

    @Value("${fabric.user-name}")
    private String userName; // “appUser”

    @Value("${fabric.network.config}")
    private String networkConfigPath; // connection‑org1.yaml

    @Value("${fabric.channel-name}")
    private String channelName; // “mychannel”

    @Value("${fabric.contract-name}")
    private String contractName; // “vaultx‑event”

    @Bean
    public Gateway gateway() throws IOException, CertificateException, InvalidKeyException {
        // 1) load the wallet (in‑memory) and import your MSP‐issued identity
        Wallet wallet = Wallets.newInMemoryWallet();
        Path mspDir = Paths.get(walletPath, "msp");

        // cert.pem
        Path certPath = mspDir.resolve("signcerts").resolve("cert.pem");
        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certPath));

        // private key (there’s only one file in keystore/)
        Path keyDir = mspDir.resolve("keystore");
        Path keyPath = Files.list(keyDir).findFirst().orElseThrow(() -> new IOException("No private key in " + keyDir));
        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(keyPath));

        Identity identity = Identities.newX509Identity(/* mspId = */ "Org1MSP", certificate, privateKey);
        wallet.put(userName, identity);

        // 2) build and connect the gateway
        Path networkConfig = Paths.get(networkConfigPath);
        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(networkConfig)
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
