package ro.cloud.security.hyperledger.hyperledger.config;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HyperledgerConfigTest {

    @Mock
    private Gateway gateway;

    @Mock
    private Network network;

    @Mock
    private Contract contract;

    @Mock
    private Wallet wallet;

    @Mock
    private Gateway.Builder gatewayBuilder;

    private HyperledgerConfig config;

    @BeforeEach
    void setUp() {
        config = new HyperledgerConfig();
        config.setWalletPath("blockchain/fabric/vaultx-network/organizations/wallet/wallet");
        config.setConnectionProfilePath("blockchain/fabric/vaultx-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");
        config.setChannelName("mychannel");
        config.setContractName("vaultx-event");
        config.setUserName("appUser");
    }

    @Test
    void gatewaySetup_shouldCreateGatewayWithCorrectConfig() throws IOException {
        try (MockedStatic<Wallets> walletsMock = mockStatic(Wallets.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Gateway> gatewayMock = mockStatic(Gateway.class)) {

            // Arrange
            Path walletPath = mock(Path.class);
            Path connectionPath = mock(Path.class);

            walletsMock.when(() -> Wallets.newFileSystemWallet(any(Path.class))).thenReturn(wallet);
            pathsMock.when(() -> Paths.get(config.getWalletPath())).thenReturn(walletPath);
            pathsMock.when(() -> Paths.get(config.getConnectionProfilePath())).thenReturn(connectionPath);

            gatewayMock.when(Gateway::createBuilder).thenReturn(gatewayBuilder);
            when(gatewayBuilder.identity(wallet, config.getUserName())).thenReturn(gatewayBuilder);
            when(gatewayBuilder.networkConfig(connectionPath)).thenReturn(gatewayBuilder);
            when(gatewayBuilder.discovery(true)).thenReturn(gatewayBuilder);
            when(gatewayBuilder.connect()).thenReturn(gateway);

            when(gateway.getNetwork(config.getChannelName())).thenReturn(network);
            when(network.getContract(config.getContractName())).thenReturn(contract);

            // Act
            Gateway result = config.gateway();
            Network networkResult = config.network(result);
            Contract contractResult = config.eventContract(networkResult);

            // Assert
            assertNotNull(result);
            assertNotNull(networkResult);
            assertNotNull(contractResult);

            // Verify
            verify(gatewayBuilder).identity(wallet, "appUser");
            verify(gatewayBuilder).networkConfig(connectionPath);
            verify(gateway).getNetwork("mychannel");
            verify(network).getContract("vaultx-event");
        }
    }
}