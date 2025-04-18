package ro.cloud.security.hyperledger.hyperledger.config;

import lombok.Data;
import org.hyperledger.fabric.gateway.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

@Configuration
@ConfigurationProperties(prefix = "fabric")
@Data
public class HyperledgerConfig {
    private String walletPath;
    private String networkName;
    private String channelName;
    private String contractName;
    private String userName;
    private String connectionProfilePath;

    @Bean
    public Gateway gateway() throws IOException {
        Path walletDirectory = Paths.get(walletPath);
        Wallet wallet = Wallets.newFileSystemWallet(walletDirectory);

        Path connectionProfile = Paths.get(connectionProfilePath);
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(connectionProfile)
                .discovery(true);

        return builder.connect();
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