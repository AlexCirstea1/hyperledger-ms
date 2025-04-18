package ro.cloud.security.hyperledger.hyperledger.config;

import java.io.IOException;
import java.nio.file.Paths;
import lombok.Data;
import org.hyperledger.fabric.gateway.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fabric")
@Data
public class HyperledgerConfig {

    private String walletPath;
    private String channelName;
    private String contractName;
    private String userName;
    private String connectionProfilePath;

    @Bean
    public Gateway gateway() throws IOException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get(walletPath));
        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(Paths.get(connectionProfilePath))
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
